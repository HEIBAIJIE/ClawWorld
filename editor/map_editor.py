#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
地图编辑器
"""

import tkinter as tk
from tkinter import ttk, messagebox, simpledialog
from csv_utils import read_csv, write_csv, get_fieldnames
from constants import (TERRAIN_TYPES, TERRAIN_COLORS, ENTITY_TYPES,
                       ENTITY_COLORS, PASSABLE_TERRAINS)


class MapEditor:
    def __init__(self, parent):
        self.frame = ttk.Frame(parent)
        self.current_map = None
        self.cell_size = 30
        self.current_tool = 'terrain'
        self.current_terrain = 'GRASS'
        self.current_entity_type = 'WAYPOINT'

        self.create_widgets()
        self.load_data()

    def create_widgets(self):
        """创建界面组件"""
        # 左侧面板 - 地图列表
        left_panel = ttk.Frame(self.frame, width=200)
        left_panel.pack(side=tk.LEFT, fill=tk.Y, padx=5, pady=5)

        ttk.Label(left_panel, text="地图列表").pack()

        # 地图列表框
        list_frame = ttk.Frame(left_panel)
        list_frame.pack(fill=tk.BOTH, expand=True)

        self.map_listbox = tk.Listbox(list_frame, width=25)
        self.map_listbox.pack(side=tk.LEFT, fill=tk.BOTH, expand=True)
        self.map_listbox.bind('<<ListboxSelect>>', self.on_map_select)

        scrollbar = ttk.Scrollbar(list_frame, orient=tk.VERTICAL,
                                  command=self.map_listbox.yview)
        scrollbar.pack(side=tk.RIGHT, fill=tk.Y)
        self.map_listbox.config(yscrollcommand=scrollbar.set)

        # 地图操作按钮
        btn_frame = ttk.Frame(left_panel)
        btn_frame.pack(fill=tk.X, pady=5)

        ttk.Button(btn_frame, text="新建地图", command=self.new_map).pack(fill=tk.X)
        ttk.Button(btn_frame, text="删除地图", command=self.delete_map).pack(fill=tk.X)
        ttk.Button(btn_frame, text="编辑属性", command=self.edit_map_properties).pack(fill=tk.X)

        # 中间面板 - 地图画布
        center_panel = ttk.Frame(self.frame)
        center_panel.pack(side=tk.LEFT, fill=tk.BOTH, expand=True, padx=5, pady=5)

        # 画布容器（带滚动条）
        canvas_frame = ttk.Frame(center_panel)
        canvas_frame.pack(fill=tk.BOTH, expand=True)

        self.canvas = tk.Canvas(canvas_frame, bg='white')
        self.canvas.pack(side=tk.LEFT, fill=tk.BOTH, expand=True)

        v_scroll = ttk.Scrollbar(canvas_frame, orient=tk.VERTICAL,
                                 command=self.canvas.yview)
        v_scroll.pack(side=tk.RIGHT, fill=tk.Y)

        h_scroll = ttk.Scrollbar(center_panel, orient=tk.HORIZONTAL,
                                 command=self.canvas.xview)
        h_scroll.pack(fill=tk.X)

        self.canvas.config(xscrollcommand=h_scroll.set, yscrollcommand=v_scroll.set)
        self.canvas.bind('<Button-1>', self.on_canvas_click)
        self.canvas.bind('<B1-Motion>', self.on_canvas_drag)

        # 右侧面板 - 工具栏
        right_panel = ttk.Frame(self.frame, width=200)
        right_panel.pack(side=tk.RIGHT, fill=tk.Y, padx=5, pady=5)

        self.create_tool_panel(right_panel)

    def create_tool_panel(self, parent):
        """创建工具面板"""
        # 工具选择
        ttk.Label(parent, text="编辑工具").pack(pady=5)

        self.tool_var = tk.StringVar(value='terrain')
        ttk.Radiobutton(parent, text="地形", variable=self.tool_var,
                        value='terrain', command=self.on_tool_change).pack(anchor=tk.W)
        ttk.Radiobutton(parent, text="实体", variable=self.tool_var,
                        value='entity', command=self.on_tool_change).pack(anchor=tk.W)
        ttk.Radiobutton(parent, text="删除实体", variable=self.tool_var,
                        value='delete', command=self.on_tool_change).pack(anchor=tk.W)

        ttk.Separator(parent, orient=tk.HORIZONTAL).pack(fill=tk.X, pady=10)

        # 地形选择
        ttk.Label(parent, text="地形类型").pack(pady=5)
        self.terrain_var = tk.StringVar(value='GRASS')
        self.terrain_combo = ttk.Combobox(parent, textvariable=self.terrain_var,
                                          values=TERRAIN_TYPES, state='readonly')
        self.terrain_combo.pack(fill=tk.X)
        self.terrain_combo.bind('<<ComboboxSelected>>', self.on_terrain_change)

        # 地形颜色预览
        self.terrain_preview = tk.Canvas(parent, width=50, height=30, bg='#90EE90')
        self.terrain_preview.pack(pady=5)

        ttk.Separator(parent, orient=tk.HORIZONTAL).pack(fill=tk.X, pady=10)

        # 实体选择
        ttk.Label(parent, text="实体类型").pack(pady=5)
        self.entity_type_var = tk.StringVar(value='WAYPOINT')
        self.entity_combo = ttk.Combobox(parent, textvariable=self.entity_type_var,
                                         values=ENTITY_TYPES, state='readonly')
        self.entity_combo.pack(fill=tk.X)

        # 实体ID选择
        ttk.Label(parent, text="实体ID").pack(pady=5)
        self.entity_id_var = tk.StringVar()
        self.entity_id_combo = ttk.Combobox(parent, textvariable=self.entity_id_var)
        self.entity_id_combo.pack(fill=tk.X)
        self.entity_combo.bind('<<ComboboxSelected>>', self.on_entity_type_change)

        ttk.Separator(parent, orient=tk.HORIZONTAL).pack(fill=tk.X, pady=10)

        # 保存按钮
        ttk.Button(parent, text="保存地图", command=self.save_current_map).pack(fill=tk.X, pady=5)

        # 图例
        ttk.Label(parent, text="图例").pack(pady=5)
        legend_frame = ttk.Frame(parent)
        legend_frame.pack(fill=tk.X)

        for entity_type, color in ENTITY_COLORS.items():
            row = ttk.Frame(legend_frame)
            row.pack(fill=tk.X)
            canvas = tk.Canvas(row, width=15, height=15)
            canvas.pack(side=tk.LEFT, padx=2)
            canvas.create_oval(2, 2, 13, 13, fill=color, outline='black')
            ttk.Label(row, text=entity_type).pack(side=tk.LEFT)

    def load_data(self):
        """加载所有数据"""
        self.maps = read_csv('maps.csv')
        self.terrains = read_csv('map_terrain.csv')
        self.entities = read_csv('map_entities.csv')
        self.waypoints = read_csv('waypoints.csv')
        self.npcs = read_csv('npcs.csv')
        self.enemies = read_csv('enemies.csv')

        self.refresh_map_list()
        self.refresh_entity_ids()

    def refresh_map_list(self):
        """刷新地图列表"""
        self.map_listbox.delete(0, tk.END)
        for m in self.maps:
            self.map_listbox.insert(tk.END, f"{m['id']} - {m['name']}")

    def refresh_entity_ids(self):
        """刷新实体ID列表"""
        entity_type = self.entity_type_var.get()
        ids = []
        if entity_type == 'WAYPOINT':
            ids = [w['id'] for w in self.waypoints]
        elif entity_type == 'NPC':
            ids = [n['id'] for n in self.npcs]
        elif entity_type == 'ENEMY':
            ids = [e['id'] for e in self.enemies]
        elif entity_type == 'CAMPFIRE':
            ids = ['campfire']
        self.entity_id_combo['values'] = ids
        if ids:
            self.entity_id_var.set(ids[0])

    def on_map_select(self, event):
        """选择地图"""
        selection = self.map_listbox.curselection()
        if selection:
            idx = selection[0]
            self.current_map = self.maps[idx]
            self.draw_map()

    def on_tool_change(self):
        """工具切换"""
        self.current_tool = self.tool_var.get()

    def on_terrain_change(self, event):
        """地形切换"""
        terrain = self.terrain_var.get()
        self.current_terrain = terrain
        color = TERRAIN_COLORS.get(terrain, '#FFFFFF')
        self.terrain_preview.config(bg=color)

    def on_entity_type_change(self, event):
        """实体类型切换"""
        self.refresh_entity_ids()

    def draw_map(self):
        """绘制地图"""
        if not self.current_map:
            return

        self.canvas.delete('all')
        map_id = self.current_map['id']
        width = int(self.current_map['width'])
        height = int(self.current_map['height'])
        default_terrain = self.current_map.get('defaultTerrain', 'GRASS')

        # 设置画布大小
        canvas_width = width * self.cell_size
        canvas_height = height * self.cell_size
        self.canvas.config(scrollregion=(0, 0, canvas_width, canvas_height))

        # 初始化地形网格
        self.terrain_grid = [[default_terrain for _ in range(width)] for _ in range(height)]

        # 加载地形数据
        for t in self.terrains:
            if t['mapId'] == map_id:
                x1, y1 = int(t['x1']), int(t['y1'])
                x2, y2 = int(t['x2']), int(t['y2'])
                terrain_types = t['terrainTypes'].split(',')
                terrain = terrain_types[0] if terrain_types else default_terrain
                for y in range(y1, y2 + 1):
                    for x in range(x1, x2 + 1):
                        if 0 <= x < width and 0 <= y < height:
                            self.terrain_grid[y][x] = terrain

        # 绘制地形格子（Y轴翻转，0在底部）
        for y in range(height):
            for x in range(width):
                terrain = self.terrain_grid[y][x]
                color = TERRAIN_COLORS.get(terrain, '#FFFFFF')
                # Y轴翻转
                draw_y = (height - 1 - y) * self.cell_size
                draw_x = x * self.cell_size
                self.canvas.create_rectangle(
                    draw_x, draw_y,
                    draw_x + self.cell_size, draw_y + self.cell_size,
                    fill=color, outline='gray', tags=f'cell_{x}_{y}'
                )

        # 绘制实体
        for e in self.entities:
            if e['mapId'] == map_id:
                x, y = int(e['x']), int(e['y'])
                entity_type = e['entityType']
                color = ENTITY_COLORS.get(entity_type, '#FFFFFF')
                # Y轴翻转
                draw_y = (height - 1 - y) * self.cell_size + self.cell_size // 2
                draw_x = x * self.cell_size + self.cell_size // 2
                r = self.cell_size // 3
                self.canvas.create_oval(
                    draw_x - r, draw_y - r, draw_x + r, draw_y + r,
                    fill=color, outline='black', tags=f'entity_{x}_{y}'
                )

        # 绘制坐标
        for x in range(width):
            self.canvas.create_text(
                x * self.cell_size + self.cell_size // 2, canvas_height + 10,
                text=str(x), font=('Arial', 8)
            )
        for y in range(height):
            draw_y = (height - 1 - y) * self.cell_size + self.cell_size // 2
            self.canvas.create_text(
                canvas_width + 15, draw_y, text=str(y), font=('Arial', 8)
            )

        # 扩展滚动区域以显示坐标
        self.canvas.config(scrollregion=(0, 0, canvas_width + 30, canvas_height + 20))

    def on_canvas_click(self, event):
        """画布点击事件"""
        self.handle_canvas_action(event)

    def on_canvas_drag(self, event):
        """画布拖拽事件"""
        if self.current_tool == 'terrain':
            self.handle_canvas_action(event)

    def handle_canvas_action(self, event):
        """处理画布操作"""
        if not self.current_map:
            return

        # 转换为画布坐标
        canvas_x = self.canvas.canvasx(event.x)
        canvas_y = self.canvas.canvasy(event.y)

        width = int(self.current_map['width'])
        height = int(self.current_map['height'])

        # 计算格子坐标
        x = int(canvas_x // self.cell_size)
        # Y轴翻转
        y = height - 1 - int(canvas_y // self.cell_size)

        if not (0 <= x < width and 0 <= y < height):
            return

        if self.current_tool == 'terrain':
            self.set_terrain(x, y)
        elif self.current_tool == 'entity':
            self.add_entity(x, y)
        elif self.current_tool == 'delete':
            self.delete_entity(x, y)

    def set_terrain(self, x, y):
        """设置地形"""
        terrain = self.terrain_var.get()
        map_id = self.current_map['id']

        # 查找并更新或创建地形记录
        found = False
        for t in self.terrains:
            if (t['mapId'] == map_id and
                int(t['x1']) == x and int(t['y1']) == y and
                int(t['x2']) == x and int(t['y2']) == y):
                t['terrainTypes'] = terrain
                found = True
                break

        if not found:
            self.terrains.append({
                'mapId': map_id,
                'x1': str(x), 'y1': str(y),
                'x2': str(x), 'y2': str(y),
                'terrainTypes': terrain
            })

        self.draw_map()

    def add_entity(self, x, y):
        """添加实体"""
        map_id = self.current_map['id']
        entity_type = self.entity_type_var.get()
        entity_id = self.entity_id_var.get()

        if not entity_id:
            messagebox.showwarning("警告", "请选择实体ID")
            return

        # 检查是否已存在实体
        for e in self.entities:
            if e['mapId'] == map_id and int(e['x']) == x and int(e['y']) == y:
                messagebox.showwarning("警告", f"该位置已存在实体: {e['entityType']}")
                return

        # 生成实例ID
        instance_id = f"{entity_id}_{x}_{y}" if entity_type == 'ENEMY' else ''

        self.entities.append({
            'mapId': map_id,
            'x': str(x), 'y': str(y),
            'entityType': entity_type,
            'entityId': entity_id,
            'instanceId': instance_id
        })

        # 如果是传送点，询问是否编辑连接
        if entity_type == 'WAYPOINT':
            if messagebox.askyesno("传送点", "是否编辑传送点连接？"):
                self.edit_waypoint_connections(entity_id)

        self.draw_map()

    def delete_entity(self, x, y):
        """删除实体"""
        map_id = self.current_map['id']

        for i, e in enumerate(self.entities):
            if e['mapId'] == map_id and int(e['x']) == x and int(e['y']) == y:
                del self.entities[i]
                self.draw_map()
                return

        messagebox.showinfo("提示", "该位置没有实体")

    def edit_waypoint_connections(self, waypoint_id):
        """编辑传送点连接"""
        # 查找传送点
        waypoint = None
        for w in self.waypoints:
            if w['id'] == waypoint_id:
                waypoint = w
                break

        if not waypoint:
            messagebox.showerror("错误", "找不到传送点")
            return

        # 创建编辑对话框
        dialog = tk.Toplevel(self.frame)
        dialog.title(f"编辑传送点连接 - {waypoint_id}")
        dialog.geometry("400x300")

        ttk.Label(dialog, text="选择可传送到的地图传送点:").pack(pady=5)

        # 获取当前连接
        current_connections = waypoint.get('connectedWaypointIds', '').split(';')
        current_connections = [c for c in current_connections if c]

        # 创建复选框列表
        check_vars = {}
        frame = ttk.Frame(dialog)
        frame.pack(fill=tk.BOTH, expand=True, padx=10)

        canvas = tk.Canvas(frame)
        scrollbar = ttk.Scrollbar(frame, orient=tk.VERTICAL, command=canvas.yview)
        scrollable_frame = ttk.Frame(canvas)

        scrollable_frame.bind(
            "<Configure>",
            lambda e: canvas.configure(scrollregion=canvas.bbox("all"))
        )

        canvas.create_window((0, 0), window=scrollable_frame, anchor="nw")
        canvas.configure(yscrollcommand=scrollbar.set)

        for w in self.waypoints:
            if w['id'] != waypoint_id:
                var = tk.BooleanVar(value=w['id'] in current_connections)
                check_vars[w['id']] = var
                map_name = self.get_map_name(w['mapId'])
                ttk.Checkbutton(
                    scrollable_frame,
                    text=f"{w['id']} ({w['name']} - {map_name})",
                    variable=var
                ).pack(anchor=tk.W)

        canvas.pack(side=tk.LEFT, fill=tk.BOTH, expand=True)
        scrollbar.pack(side=tk.RIGHT, fill=tk.Y)

        def save_connections():
            connections = [wid for wid, var in check_vars.items() if var.get()]
            waypoint['connectedWaypointIds'] = ';'.join(connections)
            dialog.destroy()

        ttk.Button(dialog, text="保存", command=save_connections).pack(pady=10)

    def get_map_name(self, map_id):
        """获取地图名称"""
        for m in self.maps:
            if m['id'] == map_id:
                return m['name']
        return map_id

    def new_map(self):
        """新建地图"""
        dialog = tk.Toplevel(self.frame)
        dialog.title("新建地图")
        dialog.geometry("400x400")

        fields = {}
        labels = [
            ('id', '地图ID'),
            ('name', '地图名称'),
            ('description', '描述'),
            ('width', '宽度'),
            ('height', '高度'),
            ('recommendedLevel', '推荐等级')
        ]

        for field, label in labels:
            row = ttk.Frame(dialog)
            row.pack(fill=tk.X, padx=10, pady=2)
            ttk.Label(row, text=label, width=12).pack(side=tk.LEFT)
            entry = ttk.Entry(row)
            entry.pack(side=tk.LEFT, fill=tk.X, expand=True)
            fields[field] = entry

        # 默认值
        fields['width'].insert(0, '10')
        fields['height'].insert(0, '10')
        fields['recommendedLevel'].insert(0, '1')

        # 是否安全区
        safe_var = tk.BooleanVar(value=True)
        ttk.Checkbutton(dialog, text="安全区域", variable=safe_var).pack(pady=5)

        # 默认地形
        terrain_row = ttk.Frame(dialog)
        terrain_row.pack(fill=tk.X, padx=10, pady=2)
        ttk.Label(terrain_row, text="默认地形", width=12).pack(side=tk.LEFT)
        terrain_var = tk.StringVar(value='GRASS')
        terrain_combo = ttk.Combobox(terrain_row, textvariable=terrain_var,
                                     values=TERRAIN_TYPES, state='readonly')
        terrain_combo.pack(side=tk.LEFT, fill=tk.X, expand=True)

        def create_map():
            map_id = fields['id'].get().strip()
            if not map_id:
                messagebox.showerror("错误", "请输入地图ID")
                return

            # 检查ID是否已存在
            for m in self.maps:
                if m['id'] == map_id:
                    messagebox.showerror("错误", "地图ID已存在")
                    return

            new_map_data = {
                'id': map_id,
                'name': fields['name'].get(),
                'description': fields['description'].get(),
                'width': fields['width'].get(),
                'height': fields['height'].get(),
                'isSafe': str(safe_var.get()).lower(),
                'recommendedLevel': fields['recommendedLevel'].get(),
                'defaultTerrain': terrain_var.get()
            }

            self.maps.append(new_map_data)
            self.refresh_map_list()
            dialog.destroy()

        ttk.Button(dialog, text="创建", command=create_map).pack(pady=10)

    def delete_map(self):
        """删除地图"""
        selection = self.map_listbox.curselection()
        if not selection:
            messagebox.showwarning("警告", "请先选择一个地图")
            return

        idx = selection[0]
        map_data = self.maps[idx]
        map_id = map_data['id']

        if not messagebox.askyesno("确认", f"确定要删除地图 {map_id} 吗？\n这将同时删除该地图的所有地形和实体数据。"):
            return

        # 删除地图
        del self.maps[idx]

        # 删除相关地形
        self.terrains = [t for t in self.terrains if t['mapId'] != map_id]

        # 删除相关实体
        self.entities = [e for e in self.entities if e['mapId'] != map_id]

        # 删除相关传送点
        self.waypoints = [w for w in self.waypoints if w['mapId'] != map_id]

        self.current_map = None
        self.canvas.delete('all')
        self.refresh_map_list()

    def edit_map_properties(self):
        """编辑地图属性"""
        if not self.current_map:
            messagebox.showwarning("警告", "请先选择一个地图")
            return

        dialog = tk.Toplevel(self.frame)
        dialog.title(f"编辑地图属性 - {self.current_map['id']}")
        dialog.geometry("400x400")

        fields = {}
        labels = [
            ('name', '地图名称'),
            ('description', '描述'),
            ('width', '宽度'),
            ('height', '高度'),
            ('recommendedLevel', '推荐等级')
        ]

        for field, label in labels:
            row = ttk.Frame(dialog)
            row.pack(fill=tk.X, padx=10, pady=2)
            ttk.Label(row, text=label, width=12).pack(side=tk.LEFT)
            entry = ttk.Entry(row)
            entry.pack(side=tk.LEFT, fill=tk.X, expand=True)
            entry.insert(0, self.current_map.get(field, ''))
            fields[field] = entry

        # 是否安全区
        safe_var = tk.BooleanVar(value=self.current_map.get('isSafe', 'true').lower() == 'true')
        ttk.Checkbutton(dialog, text="安全区域", variable=safe_var).pack(pady=5)

        # 默认地形
        terrain_row = ttk.Frame(dialog)
        terrain_row.pack(fill=tk.X, padx=10, pady=2)
        ttk.Label(terrain_row, text="默认地形", width=12).pack(side=tk.LEFT)
        terrain_var = tk.StringVar(value=self.current_map.get('defaultTerrain', 'GRASS'))
        terrain_combo = ttk.Combobox(terrain_row, textvariable=terrain_var,
                                     values=TERRAIN_TYPES, state='readonly')
        terrain_combo.pack(side=tk.LEFT, fill=tk.X, expand=True)

        def save_properties():
            for field, entry in fields.items():
                self.current_map[field] = entry.get()
            self.current_map['isSafe'] = str(safe_var.get()).lower()
            self.current_map['defaultTerrain'] = terrain_var.get()
            self.refresh_map_list()
            self.draw_map()
            dialog.destroy()

        ttk.Button(dialog, text="保存", command=save_properties).pack(pady=10)

    def save_current_map(self):
        """保存当前地图数据"""
        # 保存地图
        fieldnames = get_fieldnames('maps.csv')
        if not fieldnames:
            fieldnames = ['id', 'name', 'description', 'width', 'height',
                          'isSafe', 'recommendedLevel', 'defaultTerrain']
        write_csv('maps.csv', self.maps, fieldnames)

        # 保存地形
        fieldnames = get_fieldnames('map_terrain.csv')
        if not fieldnames:
            fieldnames = ['mapId', 'x1', 'y1', 'x2', 'y2', 'terrainTypes']
        write_csv('map_terrain.csv', self.terrains, fieldnames)

        # 保存实体
        fieldnames = get_fieldnames('map_entities.csv')
        if not fieldnames:
            fieldnames = ['mapId', 'x', 'y', 'entityType', 'entityId', 'instanceId']
        write_csv('map_entities.csv', self.entities, fieldnames)

        # 保存传送点
        fieldnames = get_fieldnames('waypoints.csv')
        if not fieldnames:
            fieldnames = ['id', 'mapId', 'name', 'description', 'x', 'y', 'connectedWaypointIds']
        write_csv('waypoints.csv', self.waypoints, fieldnames)

        messagebox.showinfo("提示", "地图数据已保存")
