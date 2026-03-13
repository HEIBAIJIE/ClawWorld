# sync-worktrees.ps1
$ErrorActionPreference = "Continue"

# 1. 确保当前在 master 窗口执行
$currentBranch = git branch --show-current
if ($currentBranch -ne "master") {
    Write-Host "当前不在 master 分支！请在 master 窗口运行此同步脚本。" -ForegroundColor Yellow
    exit 1
}

# 2. 推送 master 分支
Write-Host "🚀 正在推送 master 分支到远端..." -ForegroundColor Cyan
git push origin master
if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ master 推送失败，已终止同步。" -ForegroundColor Red
    exit $LASTEXITCODE
}

Write-Host "✅ master 推送成功，开始检查其他 Worktree..." -ForegroundColor Green

# 3. 获取所有 worktree
$worktrees = git worktree list

foreach ($line in $worktrees) {
    # 正则解析 git worktree list 的输出 (提取路径和分支名)
    if ($line -match "^(?<path>.*?)\s+[a-f0-9]+\s+\[(?<branch>.+)\]$") {
        $wtPath = $matches['path'].Trim()
        $wtBranch = $matches['branch'].Trim()

        # 跳过 master 本身
        if ($wtBranch -eq "master") { continue }

        Write-Host "`n🔍 检查工作树: $wtPath (分支: $wtBranch)" -ForegroundColor Cyan

        # 检查是否有未提交/未暂存/未追踪的更改
        $status = git -C $wtPath status --porcelain

        if ([string]::IsNullOrWhiteSpace($status)) {
            Write-Host "   -> 工作区干净。正在同步..." -ForegroundColor Green
            # 强制重置到本地刚更新好的 master
            git -C $wtPath reset --hard master
            # 强制推送到对应的 dev 分支远端 (根据你的需求)
            git -C $wtPath push --force origin $wtBranch
            Write-Host "   -> 分支 $wtBranch 同步并推送完成！" -ForegroundColor Green
        } else {
            Write-Host "   -> ⚠️ 发现未保存的更改，跳过同步 $wtBranch，以防丢失代码。" -ForegroundColor Yellow
        }
    }
}
Write-Host "`n🎉 所有可用工作树处理完毕！" -ForegroundColor Cyan