import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const usePlayerStore = defineStore('player', () => {
  // 基础信息
  const name = ref('')
  const roleName = ref('')
  const roleId = ref('')
  const level = ref(1)
  const experience = ref(0)
  const experienceForNextLevel = ref(100)
  const gold = ref(0)

  // 位置
  const x = ref(0)
  const y = ref(0)
  const mapId = ref('')
  const mapName = ref('')
  const facing = ref({ dx: 0, dy: 1 }) // 默认朝上

  // 四维属性
  const strength = ref(0)
  const agility = ref(0)
  const intelligence = ref(0)
  const vitality = ref(0)
  const freeAttributePoints = ref(0)

  // 战斗属性
  const currentHealth = ref(100)
  const maxHealth = ref(100)
  const currentMana = ref(50)
  const maxMana = ref(50)
  const physicalAttack = ref(10)
  const physicalDefense = ref(5)
  const magicAttack = ref(5)
  const magicDefense = ref(5)
  const speed = ref(100)
  const critRate = ref(5)
  const critDamage = ref(50)
  const hitRate = ref(100)
  const dodgeRate = ref(5)

  // 技能列表
  const skills = ref([])

  // 装备栏
  const equipment = ref({
    HEAD: null,
    CHEST: null,
    LEGS: null,
    FEET: null,
    LEFT_HAND: null,
    RIGHT_HAND: null,
    ACCESSORY1: null,
    ACCESSORY2: null
  })

  // 背包
  const inventory = ref([])

  // 计算属性
  const healthPercent = computed(() =>
    maxHealth.value > 0 ? (currentHealth.value / maxHealth.value) * 100 : 0
  )

  const manaPercent = computed(() =>
    maxMana.value > 0 ? (currentMana.value / maxMana.value) * 100 : 0
  )

  const expPercent = computed(() =>
    experienceForNextLevel.value > 0 ? (experience.value / experienceForNextLevel.value) * 100 : 0
  )

  // 职业图标映射
  const roleIcon = computed(() => {
    const icons = {
      '战士': '⚔️',
      '游侠': '🏹',
      '法师': '🔮',
      '牧师': '✨'
    }
    return icons[roleName.value] || '👤'
  })

  // 更新玩家状态
  function updateFromParsed(data) {
    console.log('[PlayerStore] 更新玩家状态:', data)
    if (data.name !== undefined) name.value = data.name
    if (data.roleName !== undefined) roleName.value = data.roleName
    if (data.level !== undefined) level.value = data.level
    if (data.experience !== undefined) experience.value = data.experience
    if (data.experienceForNextLevel !== undefined) experienceForNextLevel.value = data.experienceForNextLevel
    if (data.gold !== undefined) gold.value = data.gold
    if (data.x !== undefined) x.value = data.x
    if (data.y !== undefined) y.value = data.y
    if (data.mapId !== undefined) mapId.value = data.mapId
    if (data.mapName !== undefined) mapName.value = data.mapName
    if (data.strength !== undefined) strength.value = data.strength
    if (data.agility !== undefined) agility.value = data.agility
    if (data.intelligence !== undefined) intelligence.value = data.intelligence
    if (data.vitality !== undefined) vitality.value = data.vitality
    if (data.freeAttributePoints !== undefined) freeAttributePoints.value = data.freeAttributePoints
    if (data.currentHealth !== undefined) currentHealth.value = data.currentHealth
    if (data.maxHealth !== undefined) maxHealth.value = data.maxHealth
    if (data.currentMana !== undefined) currentMana.value = data.currentMana
    if (data.maxMana !== undefined) maxMana.value = data.maxMana
    if (data.physicalAttack !== undefined) physicalAttack.value = data.physicalAttack
    if (data.physicalDefense !== undefined) physicalDefense.value = data.physicalDefense
    if (data.magicAttack !== undefined) magicAttack.value = data.magicAttack
    if (data.magicDefense !== undefined) magicDefense.value = data.magicDefense
    if (data.speed !== undefined) speed.value = data.speed
    if (data.critRate !== undefined) critRate.value = data.critRate
    if (data.critDamage !== undefined) critDamage.value = data.critDamage
    if (data.hitRate !== undefined) hitRate.value = data.hitRate
    if (data.dodgeRate !== undefined) dodgeRate.value = data.dodgeRate
    if (data.skills !== undefined) skills.value = data.skills
    if (data.equipment !== undefined) equipment.value = { ...equipment.value, ...data.equipment }
    if (data.inventory !== undefined) inventory.value = data.inventory
  }

  // 更新朝向
  function setFacing(dx, dy) {
    facing.value = { dx, dy }
  }

  // 重置状态
  function reset() {
    name.value = ''
    roleName.value = ''
    level.value = 1
    experience.value = 0
    gold.value = 0
    x.value = 0
    y.value = 0
    skills.value = []
    inventory.value = []
  }

  return {
    // 基础信息
    name, roleName, roleId, level, experience, experienceForNextLevel, gold,
    // 位置
    x, y, mapId, mapName, facing,
    // 四维属性
    strength, agility, intelligence, vitality, freeAttributePoints,
    // 战斗属性
    currentHealth, maxHealth, currentMana, maxMana,
    physicalAttack, physicalDefense, magicAttack, magicDefense, speed,
    critRate, critDamage, hitRate, dodgeRate,
    // 技能和装备
    skills, equipment, inventory,
    // 计算属性
    healthPercent, manaPercent, expPercent, roleIcon,
    // 方法
    updateFromParsed, setFacing, reset
  }
})
