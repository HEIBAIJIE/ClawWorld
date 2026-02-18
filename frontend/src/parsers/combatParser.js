/**
 * 战斗数据解析器
 * 解析战斗窗口的各种信息
 */

/**
 * 解析参战方
 * 输入格式:
 * "【参战方】"
 * "第1方（PARTY_xxx）：小小 巧巧"
 * "第2方（enemy_slime）：史莱姆#1"
 * @param {string} content - 参战方文本
 * @returns {array} 参战方数组
 */
export function parseFactions(content) {
  const factions = []
  const factionPattern = /第(\d+)方（([^）]+)）[：:]\s*(.+)/g
  let match

  while ((match = factionPattern.exec(content)) !== null) {
    const members = match[3].trim().split(/\s+/).filter(Boolean)
    factions.push({
      index: parseInt(match[1]),
      id: match[2],
      members
    })
  }

  return factions
}

/**
 * 解析角色状态
 * 输入格式:
 * "♥ 小小 - HP:116/135 MP:50/55 速度:105 (你)"
 * "☠ 史莱姆#1 - HP:0/50 MP:0/0 速度:80"
 * @param {string} content - 角色状态文本
 * @returns {array} 角色状态数组
 */
export function parseCharacterStatus(content) {
  const characters = []
  const lines = content.split('\n')
  const statusPattern = /^([♥☠])\s*(.+?)\s*-\s*HP[：:](\d+)\/(\d+)\s*MP[：:](\d+)\/(\d+)(?:\s*速度[：:](\d+))?(?:\s*\(你\))?$/

  for (const line of lines) {
    const match = line.trim().match(statusPattern)
    if (match) {
      characters.push({
        name: match[2].trim(),
        isDead: match[1] === '☠',
        currentHealth: parseInt(match[3]),
        maxHealth: parseInt(match[4]),
        currentMana: parseInt(match[5]),
        maxMana: parseInt(match[6]),
        speed: match[7] ? parseInt(match[7]) : null,
        isSelf: line.includes('(你)')
      })
    }
  }

  return characters
}

/**
 * 解析行动条
 * 输入格式:
 * "→ 1. 小小 (100.0%) ← 你"
 * "  2. 史莱姆#1 (52.4%)"
 * @param {string} content - 行动条文本
 * @returns {array} 行动条数组
 */
export function parseActionBar(content) {
  const actionBar = []
  const lines = content.split('\n')
  const barPattern = /^(→)?\s*(\d+)\.\s*(.+?)\s*\(([\d.]+)%\)(?:\s*←\s*你)?$/

  for (const line of lines) {
    const match = line.trim().match(barPattern)
    if (match) {
      actionBar.push({
        position: parseInt(match[2]),
        name: match[3].trim(),
        progress: parseFloat(match[4]),
        isCurrent: match[1] === '→',
        isSelf: line.includes('← 你')
      })
    }
  }

  return actionBar
}

/**
 * 解析战斗日志
 * 输入格式:
 * "[#1] 小小 对 史莱姆#1 使用了 普通攻击"
 * "[#2] 造成了 29 点伤害"
 * @param {string} content - 战斗日志文本
 * @returns {array} 战斗日志数组
 */
export function parseBattleLogs(content) {
  const logs = []
  const lines = content.split('\n')
  const logPattern = /^\[#(\d+)\]\s*(.+)$/

  for (const line of lines) {
    const match = line.trim().match(logPattern)
    if (match) {
      logs.push({
        index: parseInt(match[1]),
        content: match[2].trim()
      })
    }
  }

  return logs
}

/**
 * 解析当前状态
 * @param {string} content - 状态文本
 * @returns {object} 状态信息
 */
export function parseCombatStatus(content) {
  return {
    isMyTurn: content.includes('★ 轮到你的回合'),
    isWaiting: content.includes('未轮到你的回合'),
    waitingFor: extractWaitingFor(content)
  }
}

/**
 * 提取等待的角色名
 */
function extractWaitingFor(content) {
  const match = content.match(/等待\s*(.+?)\s*行动/)
  return match ? match[1] : null
}

/**
 * 解析战斗结果
 * @param {string} content - 结果文本
 * @returns {object|null} 战斗结果
 */
export function parseCombatResult(content) {
  // 胜利
  if (content.includes('获得胜利')) {
    const expMatch = content.match(/每人获得经验[：:]\s*(\d+)/)
    const goldMatch = content.match(/每人\s*(\d+)/)

    return {
      victory: true,
      experience: expMatch ? parseInt(expMatch[1]) : 0,
      gold: goldMatch ? parseInt(goldMatch[1]) : 0
    }
  }

  // 失败
  if (content.includes('战斗失败') || content.includes('被击败')) {
    return {
      victory: false,
      experience: 0,
      gold: 0
    }
  }

  return null
}

export default {
  parseFactions,
  parseCharacterStatus,
  parseActionBar,
  parseBattleLogs,
  parseCombatStatus,
  parseCombatResult
}
