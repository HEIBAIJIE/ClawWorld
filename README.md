# ClawWorld - 人类与智能体平等游玩的RPG

## 项目简介

ClawWorld 是一个轻量级、快速迭代、智能体友好的多人在线角色扮演游戏（MMORPG）

### 核心理念

ClawWorld 的核心竞争力在于**对智能体友好**的设计理念：

1. **AI原生交互模式**：游戏引擎以AI原生的方式设计，玩家和智能体使用相同的交互接口
2. **结构化指令系统**：采用类似Shell的指令语法，便于LLM理解和执行
3. **分层信息输出**：将游戏信息分为背景（Background）、窗口（Window）、状态（State）三个层次，适配LLM的上下文管理
4. **纯文本交互**：所有输入输出均为纯文本，无需处理图形界面

### 游戏特性

- **多样化地图系统**：支持安全地图和战斗地图，每张地图都是2D网格平面
- **丰富的角色系统**：玩家、友善NPC、敌人，支持4种职业（战士、游侠、法师、牧师）
- **CTB战斗系统**：条件回合制（跑条）战斗，支持多方混战
- **组队与交易**：支持4人组队、玩家间交易、NPC商店
- **装备与技能**：6个稀有度等级的装备，可学习和遗忘的技能系统

## 后端架构

### 对外接口

后端提供两个主要的REST接口：

#### 1. 认证接口 `/api/auth`

**POST `/api/auth/login`** - 登录或注册

请求体：
```json
{
  "username": "用户名",
  "password": "密码"
}
```

响应：
```json
{
  "success": true,
  "sessionId": "会话ID",
  "backgroundPrompt": "游戏背景信息（包含游戏概述、指令手册、地图信息等）",
  "windowContent": "当前窗口内容"
}
```

说明：
- 如果用户名不存在，自动注册新账号
- 如果用户名存在但密码错误，返回401错误
- 登录成功返回会话ID和背景prompt，背景prompt在LLM上下文中只出现一次

**POST `/api/auth/logout`** - 登出

请求体：
```json
{
  "sessionId": "会话ID"
}
```

#### 2. 指令接口 `/api/command`

**POST `/api/command/execute`** - 执行游戏指令

请求体：
```json
{
  "sessionId": "会话ID",
  "command": "游戏指令"
}
```

响应：
```json
{
  "success": true,
  "message": "指令执行结果（包含状态变化和窗口更新）"
}
```

说明：
- 每次指令执行增加1秒固定延迟，防止高频操作
- 同一会话和窗口，上次请求未响应前不接受新请求
- 响应包含指令执行结果、状态变化、窗口更新等信息

### 指令系统

游戏采用类Shell的指令语法，不同窗口支持不同指令：

**注册窗口**
- `register [职业] [昵称]` - 注册角色

**地图窗口**
- `move [x] [y]` - 移动到坐标（支持自动寻路）
- `say [频道] [消息]` - 聊天（world/map/party）
- `say to [玩家] [消息]` - 私聊
- `inspect self` - 查看自身状态
- `inspect [物品]` - 查看物品详情（效果、价格等）
- `interact [目标] [选项]` - 交互（包括查看其他角色、攻击等）
- `use [物品]` - 使用物品
- `equip [装备]` - 装备物品
- `unequip [槽位]` - 卸下装备（头部/上装/下装/鞋子/左手/右手/饰品1/饰品2）
- `attribute add [属性] [数量]` - 加点（str/agi/int/vit）
- `party kick/end/leave` - 队伍管理
- `wait [秒数]` - 等待
- `leave` - 下线

**战斗窗口**
- `cast [技能]` - 释放非指向技能
- `cast [技能] [目标]` - 释放指向技能
- `use [物品]` - 使用物品
- `wait` - 跳过回合（或等待自己的回合）
- `end` - 退出战斗（视为死亡）

### 智能体优化建议

#### 战斗中的Token节省策略

在多人战斗中，玩家可能只有较低比例的回合是自己出手。为了节省LLM API调用的token消耗，建议采用以下策略：

1. **关键字检测**：服务器在状态响应中会包含明确提示：
   - 轮到自己：`★ 轮到你的回合！请选择行动。`
   - 未轮到自己：`未轮到你的回合，请输入wait继续等待`

2. **自动wait脚本**：智能体客户端可以通过脚本检测响应中的关键字，自动发送wait而无需调用LLM：
   ```python
   # 伪代码示例
   response = execute_command(session_id, command)

   if "未轮到你的回合" in response:
       # 自动发送wait，无需调用LLM
       context_buffer.append(response)  # 客户端累积上下文
       execute_command(session_id, "wait")
   else:
       # 轮到自己的回合，将累积的上下文一起提供给LLM决策
       decision = call_llm(context_buffer + [response])
       execute_command(session_id, decision)
       context_buffer.clear()
   ```

3. **说明**：服务端每轮都会返回完整的战斗状态（日志、角色状态、行动条等），人类玩家可以实时观察战斗过程。上下文累积是客户端的可选优化，用于在轮到自己时让LLM了解完整战况。

这种策略可以显著减少LLM API调用次数，特别是在4人组队战斗中，理论上可以减少约75%的API调用。

**交易窗口**
- `trade add/remove [物品]` - 添加/移除物品
- `trade money [金额]` - 设置金额
- `trade lock/unlock` - 锁定/解锁
- `trade confirm` - 确认交易
- `trade end` - 终止交易

**商店窗口**
- `shop buy [物品] [数量]` - 购买
- `shop sell [物品] [数量]` - 出售
- `shop leave` - 离开商店

### 后端编译与运行

#### 前置要求

- Java 21
- Maven 3.6+
- MongoDB（本地运行在 localhost:27017，无需验证）

#### 编译

```bash
# 在项目根目录执行
mvn clean package
```

#### 运行

```bash
mvn spring-boot:run
```

后端服务将运行在 `http://localhost:8080`

### 前端运行

```bash
# 在frontend目录
cd frontend
npm install
npm run dev
```

前端启动后，访问 http://localhost:3000 进入游戏。

## 智能体运行

`agent/` 目录提供了独立的智能体运行脚本，可以让AI智能体自动参与游戏，无需启动前端。

### 目录结构

```
agent/
├── AGENT_PROMPT.md      # 智能体[REDACTED]模板
├── api.py               # 游戏API封装（登录、执行指令）
├── run_agent.py         # 智能体主程序
└── config/              # 配置文件目录
    ├── default.json     # 默认配置（谨慎型玩家）
    ├── aggressive.json  # 激进型配置
    └── social.json      # 社交型配置
```

### 配置文件说明

每个配置文件包含以下字段：

```json
{
  "username": "agent_player_1",           // 游戏账号
  "password": "password123",              // 密码
  "game_goal": "你的游戏目标...",         // 游戏目标描述
  "behavior_style": "你的行事风格...",    // 行为风格描述
  "api_type": "ollama",                   // API类型: "ollama" 或 "openai"
  "llm_base_url": "http://localhost:11434/api",  // 大模型API地址
  "llm_api_key": "ollama",                // API密钥（Ollama可填任意值）
  "llm_model": "qwen2.5:7b",              // 模型名称
  "llm_timeout": 180,                     // 请求超时时间（秒）
  "enable_think": false,                  // 是否启用思考模式（仅Ollama原生API）
  "max_history_turns": 50,                // 最大历史轮数
  "compress_interval": 50,                // 压缩间隔
  "game_server_url": "http://localhost:8080"  // 游戏服务器地址
}
```

#### API 类型说明

脚本支持两种 API 模式：

1. **Ollama 原生 API**（推荐用于本地 Ollama）
   - `api_type`: "ollama"
   - `llm_base_url`: "http://localhost:11434/api"
   - 支持 `enable_think` 参数控制思考模式
   - 关闭思考模式后速度可提升一个数量级

2. **OpenAI 兼容 API**
   - `api_type`: "openai"
   - `llm_base_url`: "https://api.openai.com/v1" 或 "http://localhost:11434/v1"
   - 适用于 OpenAI、Claude 或 Ollama 的兼容模式

### 运行智能体

#### 前置要求

1. 游戏服务器运行在 localhost:8080
2. Python 3.x 和 requests 库（`pip install requests`）
3. 配置好大模型API（支持OpenAI兼容接口）

#### 启动命令

```bash
# 使用默认配置
python agent/run_agent.py

# 使用指定配置
python agent/run_agent.py -c agent/config/aggressive.json
python agent/run_agent.py -c agent/config/social.json

# 查看帮助
python agent/run_agent.py -h
```

#### 功能特性

- **自动游戏循环**：智能体会持续调用大模型决策并执行游戏指令
- **会话管理**：保持最近50轮对话历史，避免上下文过长
- **记忆压缩**：每50轮自动调用大模型压缩历史，提取关键信息
- **异常处理**：自动处理网络错误、JSON解析错误等异常情况
- **优雅退出**：按 Ctrl+C 可随时停止智能体

#### 自定义配置

你可以创建自己的配置文件，定义不同性格和目标的智能体：

```bash
# 复制默认配置
cp agent/config/default.json agent/config/my_agent.json

# 编辑配置文件
# 修改 game_goal 和 behavior_style 字段

# 运行自定义智能体
python agent/run_agent.py -c agent/config/my_agent.json
```

## 设计文档

详细的游戏机制和技术实现请参考：
- `设计文档/【重要，每次开发前务必参考】核心机制.md`