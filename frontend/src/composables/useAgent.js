import { useAgentStore } from '../stores/agentStore'
import { useLogStore } from '../stores/logStore'
import { useSessionStore } from '../stores/sessionStore'
import { useCommand } from './useCommand'
import { gameApi } from '../api/game'
import agentPromptTemplate from '../../../AGENT_PROMPT.md?raw'

/**
 * 智能代理composable - 处理与大模型的交互
 */
export function useAgent() {
  const agentStore = useAgentStore()
  const logStore = useLogStore()
  const sessionStore = useSessionStore()
  const { sendCommand } = useCommand()

  /**
   * 构建系统提示词
   */
  function buildSystemPrompt() {
    return agentPromptTemplate
      .replace('{{GAME_GOAL}}', agentStore.config.gameGoal)
      .replace('{{BEHAVIOR_STYLE}}', agentStore.config.behaviorStyle)
  }

  /**
   * 调用大模型API（前端直连模式）
   */
  async function callLLMDirect(messages) {
    const { baseUrl, apiKey, model } = agentStore.config

    const response = await fetch(`${baseUrl}/chat/completions`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${apiKey}`
      },
      body: JSON.stringify({
        model: model,
        messages: messages,
        temperature: 0.7,
        max_tokens: 500
      })
    })

    if (!response.ok) {
      const errorText = await response.text()
      throw new Error(`API请求失败: ${response.status} - ${errorText}`)
    }

    const data = await response.json()
    return data.choices[0].message.content
  }

  /**
   * 调用大模型API（后端代理模式）
   */
  async function callLLMProxy(messages) {
    const { baseUrl, apiKey, model } = agentStore.config

    const response = await gameApi.proxyAgentChat({
      baseUrl,
      apiKey,
      model,
      messages,
      temperature: 0.7,
      maxTokens: 500
    })

    return response.data.choices[0].message.content
  }

  /**
   * 调用大模型API（根据配置选择模式）
   */
  async function callLLM(messages) {
    try {
      if (agentStore.config.useBackendProxy) {
        console.log('[Agent] 使用后端代理模式')
        return await callLLMProxy(messages)
      } else {
        console.log('[Agent] 使用前端直连模式')
        return await callLLMDirect(messages)
      }
    } catch (error) {
      console.error('[Agent] LLM调用失败:', error)
      throw error
    }
  }

  /**
   * 解析大模型响应，提取指令
   * @returns {{ success: boolean, thinking: string, command: string, raw: string }}
   */
  function parseResponse(responseText) {
    try {
      // 尝试直接解析JSON
      const parsed = JSON.parse(responseText)
      if (parsed.command && typeof parsed.command === 'string') {
        return {
          success: true,
          thinking: parsed.thinking || '',
          command: parsed.command,
          raw: responseText
        }
      }
    } catch (e) {
      // 尝试从文本中提取JSON
      const jsonMatch = responseText.match(/\{[\s\S]*\}/)
      if (jsonMatch) {
        try {
          const parsed = JSON.parse(jsonMatch[0])
          if (parsed.command && typeof parsed.command === 'string') {
            return {
              success: true,
              thinking: parsed.thinking || '',
              command: parsed.command,
              raw: responseText
            }
          }
        } catch (e2) {
          // 继续尝试其他方式
        }
      }
      // 尝试提取command字段
      const commandMatch = responseText.match(/"command"\s*:\s*"([^"]+)"/)
      if (commandMatch) {
        const thinkingMatch = responseText.match(/"thinking"\s*:\s*"([^"]*)"/)
        return {
          success: true,
          thinking: thinkingMatch ? thinkingMatch[1] : '',
          command: commandMatch[1],
          raw: responseText
        }
      }
    }
    // 解析失败
    console.error('[Agent] 无法解析响应:', responseText)
    return {
      success: false,
      thinking: '',
      command: '',
      raw: responseText
    }
  }

  /**
   * 检查是否需要等待（战斗中不是自己的回合）
   * 这种情况下前端会自动发送wait，不需要调用LLM
   */
  function shouldSkipLLM(serverResponse) {
    return serverResponse.includes('未轮到你的回合，请输入wait继续等待')
  }

  /**
   * 检查是否轮到自己行动
   */
  function isMyTurn(serverResponse) {
    return serverResponse.includes('★ 轮到你的回合！请选择行动')
  }

  /**
   * 检查战斗是否结束
   */
  function isCombatEnded(serverResponse) {
    return serverResponse.includes('战斗结束') ||
           serverResponse.includes('获得胜利') ||
           serverResponse.includes('战斗失败') ||
           serverResponse.includes('切换到地图窗口')
  }

  /**
   * 获取最近的服务器响应（从上次发送指令后的内容）
   */
  function getRecentResponse() {
    const lines = logStore.rawText.split('\n')
    // 获取最近100行作为上下文
    return lines.slice(-100).join('\n')
  }

  /**
   * 启动智能代理循环
   * @param {string} initialContext - 初始上下文（当前文本窗口内容）
   */
  async function startAgentLoop(initialContext) {
    if (!agentStore.isEnabled || !agentStore.isConfigured) {
      console.warn('[Agent] 代理未启用或未配置')
      return
    }

    // 初始化对话历史
    agentStore.clearHistory()

    // 添加系统消息
    const systemPrompt = buildSystemPrompt()
    agentStore.addMessage('system', systemPrompt)

    // 添加初始上下文作为第一条用户消息
    agentStore.addMessage('user', `当前游戏状态：\n${initialContext}`)

    // 开始循环
    await agentLoop()
  }

  /**
   * 智能代理主循环
   */
  async function agentLoop() {
    while (agentStore.isEnabled) {
      try {
        // 等待之前的请求完成
        while (sessionStore.isWaiting) {
          await new Promise(resolve => setTimeout(resolve, 100))
          if (!agentStore.isEnabled) return
        }

        // 获取当前服务器响应
        const currentResponse = getRecentResponse()

        // 检查是否需要跳过LLM（战斗中等待对方行动）
        if (shouldSkipLLM(currentResponse)) {
          console.log('[Agent] 战斗中等待对方行动，缓存日志，跳过LLM调用')
          // 缓存当前响应
          agentStore.addPendingCombatLog(currentResponse)
          // 等待前端自动发送wait并获取新响应
          await new Promise(resolve => setTimeout(resolve, 500))
          continue
        }

        // 准备发送给LLM的上下文
        let contextToSend = currentResponse

        // 如果有缓存的战斗日志，合并它们
        const pendingLogs = agentStore.flushPendingCombatLogs()
        if (pendingLogs.length > 0) {
          console.log('[Agent] 合并缓存的战斗日志，共', pendingLogs.length, '条')
          contextToSend = pendingLogs.join('\n---\n') + '\n---\n当前状态：\n' + currentResponse
        }

        // 更新对话历史（如果不是第一次循环）
        if (agentStore.conversationHistory.length > 2) {
          agentStore.addMessage('user', `服务器响应：\n${contextToSend}`)
        }

        agentStore.isThinking = true
        console.log('[Agent] 开始思考...')

        // 调用大模型
        const llmResponse = await callLLM(agentStore.conversationHistory)
        console.log('[Agent] 大模型响应:', llmResponse)

        // 解析响应
        const parseResult = parseResponse(llmResponse)
        console.log('[Agent] 解析结果:', parseResult)

        // 记录大模型的响应
        agentStore.addMessage('assistant', llmResponse)

        // 检查解析是否成功
        if (!parseResult.success) {
          console.warn('[Agent] 响应格式错误，要求大模型重新回答')
          // 告诉大模型格式有问题
          agentStore.addMessage('user',
            `你的上一条响应格式不正确，无法解析。你的响应是：\n${llmResponse}\n\n` +
            `请严格按照JSON格式响应：{"thinking":"简短思考","command":"指令"}\n` +
            `不要添加任何额外的文字、解释或markdown代码块。`
          )
          // 继续循环，让大模型重新回答
          await new Promise(resolve => setTimeout(resolve, 500))
          continue
        }

        // 更新思考内容显示
        agentStore.setThinking(parseResult.thinking)

        agentStore.isThinking = false

        if (!agentStore.isEnabled) {
          console.log('[Agent] 代理已关闭，停止循环')
          break
        }

        // 发送指令到游戏服务器
        await sendCommand(parseResult.command)

        // 短暂延迟，等待响应处理完成
        await new Promise(resolve => setTimeout(resolve, 1000))

      } catch (error) {
        console.error('[Agent] 循环出错:', error)
        agentStore.isThinking = false
        // 出错后等待一段时间再重试
        await new Promise(resolve => setTimeout(resolve, 3000))
      }
    }
  }

  return {
    startAgentLoop,
    callLLM,
    parseResponse
  }
}
