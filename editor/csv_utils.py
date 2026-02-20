#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
CSV文件读写工具
"""

import csv
import os

DATA_DIR = os.path.join(os.path.dirname(__file__), '..', 'src', 'main', 'resources', 'data')


def get_csv_path(filename):
    """获取CSV文件的完整路径"""
    return os.path.join(DATA_DIR, filename)


def read_csv(filename):
    """读取CSV文件，返回列表"""
    path = get_csv_path(filename)
    data = []
    if os.path.exists(path):
        with open(path, 'r', encoding='utf-8', newline='') as f:
            reader = csv.DictReader(f)
            for row in reader:
                data.append(row)
    return data


def write_csv(filename, data, fieldnames):
    """写入CSV文件"""
    path = get_csv_path(filename)
    with open(path, 'w', encoding='utf-8', newline='') as f:
        writer = csv.DictWriter(f, fieldnames=fieldnames)
        writer.writeheader()
        writer.writerows(data)


def get_fieldnames(filename):
    """获取CSV文件的列名"""
    path = get_csv_path(filename)
    if os.path.exists(path):
        with open(path, 'r', encoding='utf-8', newline='') as f:
            reader = csv.reader(f)
            return next(reader, [])
    return []
