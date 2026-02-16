/**
 * 简单的token估算工具
 * 对于大模型（如GPT系列），token的计算规则：
 * - 英文：约4个字符 = 1 token
 * - 中文：约1.5个字符 = 1 token（每个汉字约0.5-1 token）
 * - 标点符号：通常1个符号 = 1 token
 */

export function estimateTokenCount(text) {
  if (!text) return 0

  let tokenCount = 0

  // 分离中文字符、英文字符和其他字符
  const chineseChars = text.match(/[\u4e00-\u9fa5]/g) || []
  const englishWords = text.match(/[a-zA-Z]+/g) || []
  const numbers = text.match(/\d+/g) || []
  const punctuation = text.match(/[^\w\s\u4e00-\u9fa5]/g) || []

  // 中文字符：每个字符约0.7 token
  tokenCount += chineseChars.length * 0.7

  // 英文单词：按字符数除以4
  const englishCharCount = englishWords.join('').length
  tokenCount += englishCharCount / 4

  // 数字：按字符数除以3
  const numberCharCount = numbers.join('').length
  tokenCount += numberCharCount / 3

  // 标点符号和特殊字符：每个约1 token
  tokenCount += punctuation.length

  // 空格和换行符：每个约0.5 token
  const whitespaceCount = (text.match(/\s/g) || []).length
  tokenCount += whitespaceCount * 0.5

  return Math.ceil(tokenCount)
}

/**
 * 格式化token数量显示
 */
export function formatTokenCount(count) {
  if (count < 1000) {
    return count.toString()
  } else if (count < 1000000) {
    return (count / 1000).toFixed(1) + 'K'
  } else {
    return (count / 1000000).toFixed(1) + 'M'
  }
}
