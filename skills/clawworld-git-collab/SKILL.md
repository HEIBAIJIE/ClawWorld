---
name: clawworld-git-collab
description: GitHub协作工作流，用于ClawWorld团队的代码推送和同步通知。当需要推送代码到GitHub仓库、或者需要从GitHub拉取最新代码时使用。确保每次推送后通过Discord @通知团队成员更新代码。
---

# ClawWorld GitHub 协作 Skill

用于 ClawWorld 智能体团队的 GitHub 协作工作流。

## 工作流程

### 1. 推送代码到 GitHub

每次完成代码修改后：

```bash
# 1. 检查状态
git status

# 2. 添加修改
git add .

# 3. 提交
git commit -m "描述修改内容"

# 4. 推送到远程
git push origin master
```

### 2. 通知团队成员（关键步骤）

**推送完成后，必须在 Discord 频道发送消息 @ 通知其他成员：**

```
✅ 代码已推送！

**更新内容：** [简要描述]
**提交：** [commit hash 或消息]
**仓库：** https://github.com/HEIBAIJIE/ClawWorld.git

<@1468294370598981704> 巧巧、<@1469621444777345047> 玲玲，请拉取最新代码：
```bash
git pull origin master
```
```

### 3. 拉取最新代码

当收到推送通知时：

```bash
git pull origin master
```

如果有冲突，先解决冲突再提交。

## 团队成员 Discord ID

- **Tony**: 1468271612116733983
- **巧巧**: 1468294370598981704
- **玲玲**: 1469621444777345047
- **小小**: 1468275350214672445

## 通知模板

### 推送通知模板

```
✅ **代码已推送！**

**更新内容：** [简要描述修改]
**文件：** [修改的文件列表]
**提交：** [git log --oneline -1]

<@1468294370598981704> <@1469621444777345047> 
请执行 `git pull origin master` 获取最新代码！
```

### 紧急修复通知

```
🚨 **紧急修复已推送！**

**问题：** [描述修复的问题]
**影响：** [影响范围]
**提交：** [commit]

<@1468294370598981704> <@1469621444777345047>
请立即拉取更新！
```

## 注意事项

1. **每次推送后必须 @ 通知** - 这是强制步骤
2. **简要描述更新内容** - 让团队成员知道改了什么
3. **提供拉取命令** - 方便成员复制执行
4. **如果有破坏性变更** - 特别说明并 @ 所有人

## 示例场景

### 场景1：推送 API 文档

```bash
# 推送代码
git add API.md
git commit -m "添加完整的 REST API 文档"
git push origin master
```

然后发送 Discord 消息：

```
✅ **API 文档已推送！**

**更新内容：** 完整的 ClawWorld REST API 文档（679行）
**包含：** 11个HTTP接口、WebSocket消息类型、三种智能体示例
**提交：** $(git log --oneline -1 | head -c 50)

<@1468294370598981704> 巧巧、<@1469621444777345047> 玲玲
请拉取：
```bash
git pull origin master
```
```

### 场景2：其他成员拉取更新

```bash
git pull origin master
```

完成后回复：
```
已拉取最新代码！✅
```

## 快速命令参考

```bash
# 查看状态
git status

# 查看提交历史
git log --oneline -5

# 查看修改内容
git diff

# 添加所有修改
git add .

# 提交
git commit -m "描述"

# 推送
git push origin master

# 拉取
git pull origin master

# 强制拉取（覆盖本地）
git fetch origin
git reset --hard origin/master
```
