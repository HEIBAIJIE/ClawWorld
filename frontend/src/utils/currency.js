/**
 * 货币工具类
 * 处理货币的解析和格式化
 */

/**
 * 从文本中解析货币
 * @param {string} text - 包含货币的文本，如 "金币: 1金234银567铜"
 * @returns {object} { gold, silver, copper, total }
 */
export function parseCurrency(text) {
  if (!text) {
    return { gold: 0, silver: 0, copper: 0, total: 0 }
  }

  const goldMatch = text.match(/(\d+)金/)
  const silverMatch = text.match(/(\d+)银/)
  const copperMatch = text.match(/(\d+)铜/)

  const gold = goldMatch ? parseInt(goldMatch[1]) : 0
  const silver = silverMatch ? parseInt(silverMatch[1]) : 0
  const copper = copperMatch ? parseInt(copperMatch[1]) : 0

  const total = gold * 1000000 + silver * 1000 + copper

  return { gold, silver, copper, total }
}

/**
 * 将铜币总数转换为金/银/铜
 * @param {number} copperAmount - 铜币总数
 * @returns {object} { gold, silver, copper }
 */
export function breakdownCurrency(copperAmount) {
  if (!copperAmount || copperAmount < 0) {
    return { gold: 0, silver: 0, copper: 0 }
  }

  const gold = Math.floor(copperAmount / 1000000)
  const silver = Math.floor((copperAmount % 1000000) / 1000)
  const copper = copperAmount % 1000

  return { gold, silver, copper }
}

/**
 * 格式化货币为文本
 * @param {number} copperAmount - 铜币总数
 * @returns {string} 格式化后的文本，如 "1金234银567铜"
 */
export function formatCurrency(copperAmount) {
  const { gold, silver, copper } = breakdownCurrency(copperAmount)

  const parts = []
  if (gold > 0) parts.push(`${gold}金`)
  if (silver > 0) parts.push(`${silver}银`)
  if (copper > 0 || parts.length === 0) parts.push(`${copper}铜`)

  return parts.join('')
}

/**
 * 从金/银/铜计算铜币总数
 * @param {number} gold - 金币数量
 * @param {number} silver - 银币数量
 * @param {number} copper - 铜币数量
 * @returns {number} 铜币总数
 */
export function toCopper(gold, silver, copper) {
  return (gold || 0) * 1000000 + (silver || 0) * 1000 + (copper || 0)
}

export default {
  parseCurrency,
  breakdownCurrency,
  formatCurrency,
  toCopper
}
