import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useCombatStore = defineStore('combat', () => {
  // 战斗状态
  const isInCombat = ref(false)
  const combatId = ref(null)

  // 参战方
  const factions = ref([])

  // 所有战斗角色
  const characters = ref([])

  // 行动条
  const actionBar = ref([])

  // 当前回合
  const currentTurn = ref(null)
  const isMyTurn = ref(false)

  // 战斗日志
  const battleLogs = ref([])

  // 计算属性：我方角色
  const allies = computed(() => {
    return characters.value.filter(c => c.isAlly)
  })

  // 计算属性：敌方角色
  const enemies = computed(() => {
    return characters.value.filter(c => !c.isAlly && c.type === 'ENEMY')
  })

  // 计算属性：存活的敌人
  const aliveEnemies = computed(() => {
    return enemies.value.filter(e => !e.isDead)
  })

  // 获取角色
  function getCharacter(name) {
    return characters.value.find(c => c.name === name)
  }

  // 更新战斗状态
  function updateCombatState(data) {
    if (data.isInCombat !== undefined) isInCombat.value = data.isInCombat
    if (data.combatId !== undefined) combatId.value = data.combatId
    if (data.factions !== undefined) factions.value = data.factions
    if (data.characters !== undefined) characters.value = data.characters
    if (data.actionBar !== undefined) actionBar.value = data.actionBar
    if (data.currentTurn !== undefined) currentTurn.value = data.currentTurn
    if (data.isMyTurn !== undefined) isMyTurn.value = data.isMyTurn
  }

  // 添加战斗日志
  function addBattleLog(log) {
    battleLogs.value.push({
      ...log,
      timestamp: Date.now()
    })
    // 保留最近100条日志
    if (battleLogs.value.length > 100) {
      battleLogs.value.shift()
    }
  }

  // 更新角色状态
  function updateCharacter(name, updates) {
    const character = characters.value.find(c => c.name === name)
    if (character) {
      Object.assign(character, updates)
    }
  }

  // 重置状态
  function reset() {
    isInCombat.value = false
    combatId.value = null
    factions.value = []
    characters.value = []
    actionBar.value = []
    currentTurn.value = null
    isMyTurn.value = false
    battleLogs.value = []
  }

  return {
    // 状态
    isInCombat, combatId, factions, characters, actionBar,
    currentTurn, isMyTurn, battleLogs,
    // 计算属性
    allies, enemies, aliveEnemies,
    // 方法
    getCharacter, updateCombatState, addBattleLog, updateCharacter, reset
  }
})
