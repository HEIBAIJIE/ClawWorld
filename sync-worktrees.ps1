# sync-worktrees.ps1

# 1. 获取当前所有 worktree 的信息
# 过滤掉主库路径（通常是第一个），只针对其他的 worktree 目录执行
$worktrees = git worktree list --porcelain | Select-String "^worktree " | ForEach-Object { $_.ToString().Split(" ")[1] }

# 定义主分支名称（根据你的实际情况修改，如 master 或 main）
$masterBranch = "master"

Write-Host "开始同步所有 Dev Worktrees 到 $masterBranch..." -ForegroundColor Cyan

foreach ($wtPath in $worktrees) {
    # 进入 worktree 目录
    Push-Location $wtPath

    # 获取当前分支名称
    $currentBranch = git rev-parse --abbrev-ref HEAD

    # 跳过 master 分支本身
    if ($currentBranch -eq $masterBranch) {
        Write-Host "跳过主分支: $wtPath" -ForegroundColor Gray
        Pop-Location
        continue
    }

    Write-Host "正在检查分支: $currentBranch (路径: $wtPath)" -ForegroundColor Yellow

    # 2. 检查是否有未提交或暂存的修改 (git status --short)
    $status = git status --short
    if ([string]::IsNullOrWhiteSpace($status)) {
        Write-Host "  -> 状态干净，准备重置到 $masterBranch" -ForegroundColor Green

        # 3. 执行同步操作
        # 获取远程 master 最新状态
        git fetch origin $masterBranch

        # 强制重置本地分支到远程 master
        git reset --hard "origin/$masterBranch"

        # 4. 强制推送到对应的远端分支
        # 假设远端分支名与本地一致
        git push origin $currentBranch --force

        Write-Host "  -> [成功] $currentBranch 已同步并推送。" -ForegroundColor Cyan
    } else {
        Write-Host "  -> [跳过] 存在未提交的修改，为了安全不执行重置。" -ForegroundColor Red
    }

    # 返回上一级目录
    Pop-Location
}

Write-Host "`n所有任务处理完毕。" -ForegroundColor Magenta