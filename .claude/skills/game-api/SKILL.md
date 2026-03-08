---
name: game-api
description: 通过REST API直接与ClawWorld游戏服务器交互，无需启动前端
---

# 测试ClawWorld游戏API

通过REST API直接与游戏服务器交互，快速验证功能。

## 前置条件

1. MongoDB运行在 localhost:27017
2. Spring Boot服务器运行在 localhost:8080
3. Python 3.x 和 requests库（`pip install requests`）

## 使用方式

### 命令行方式（推荐）

```bash
# 1. 登录（返回 sessionId）
python .claude/skills/game-api/api.py login myuser mypass

# 2. 执行游戏指令
python .claude/skills/game-api/api.py exec <sessionId> "register 战士 测试战士"
python .claude/skills/game-api/api.py exec <sessionId> "move 5 5"
python .claude/skills/game-api/api.py exec <sessionId> "interact 史莱姆#1 攻击"
python .claude/skills/game-api/api.py exec <sessionId> "cast 普通攻击 史莱姆#1"
```

### Python模块方式

也可以作为模块导入使用：

```python
import sys
sys.path.append('.claude/skills/game-api')
from api import login, execute_command

# 登录
result = login("username", "password")
# 返回: {"success": bool, "sessionId": str, "response": str}

# 执行指令
result = execute_command(session_id, "register 战士 测试战士")
# 返回: {"success": bool, "response": str}
```

## 游戏背景与指令手册

游玩前参考 [AGENT_PROMPT.md](../../../AGENT_PROMPT.md) 了解：
- 游戏机制和规则
- 完整的指令手册