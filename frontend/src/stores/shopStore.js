import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useShopStore = defineStore('shop', () => {
  // 商店基础信息
  const isInShop = ref(false)
  const shopName = ref('')
  const purchaseInfo = ref('') // 收购信息
  const shopGold = ref(0) // 商店资金（铜币）
  const shopGoldDisplay = ref('') // 商店资金显示文本

  // 商店商品列表
  const shopItems = ref([])

  // 玩家资产（在商店中显示）
  const playerGold = ref(0) // 玩家金币（铜币）
  const playerGoldDisplay = ref('') // 玩家金币显示文本
  const playerInventory = ref([])
  const inventoryCapacity = ref(50)
  const inventoryUsed = ref(0)

  // 计算属性：可出售的背包物品
  const sellableItems = computed(() => {
    return playerInventory.value.filter(item => !item.isEquipment || item.canSell)
  })

  // 打开商店
  function openShop(name) {
    isInShop.value = true
    shopName.value = name
    shopItems.value = []
    purchaseInfo.value = ''
    shopGold.value = 0
  }

  // 更新商店商品
  function updateShopItems(items) {
    shopItems.value = items
  }

  // 更新收购信息
  function updatePurchaseInfo(info) {
    purchaseInfo.value = info
  }

  // 更新商店资金
  function updateShopGold(gold, goldDisplay) {
    shopGold.value = gold
    shopGoldDisplay.value = goldDisplay || ''
  }

  // 更新玩家资产
  function updatePlayerAssets(gold, goldDisplay, inventory, used, capacity) {
    playerGold.value = gold
    playerGoldDisplay.value = goldDisplay || ''
    if (inventory) {
      playerInventory.value = inventory
    }
    if (used !== undefined) {
      inventoryUsed.value = used
    }
    if (capacity !== undefined) {
      inventoryCapacity.value = capacity
    }
  }

  // 关闭商店
  function closeShop() {
    isInShop.value = false
    shopName.value = ''
    shopItems.value = []
    purchaseInfo.value = ''
    shopGold.value = 0
    playerGold.value = 0
    playerInventory.value = []
  }

  // 重置
  function reset() {
    closeShop()
  }

  return {
    // 状态
    isInShop,
    shopName,
    shopItems,
    purchaseInfo,
    shopGold,
    shopGoldDisplay,
    playerGold,
    playerGoldDisplay,
    playerInventory,
    inventoryCapacity,
    inventoryUsed,
    // 计算属性
    sellableItems,
    // 方法
    openShop,
    updateShopItems,
    updatePurchaseInfo,
    updateShopGold,
    updatePlayerAssets,
    closeShop,
    reset
  }
})
