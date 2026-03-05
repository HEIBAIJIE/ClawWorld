<template>
  <div class="trade-overlay" v-if="tradeStore.isInTrade">
    <div class="trade-window sci-panel">
      <!-- 标题栏 -->
      <div class="trade-header">
        <span class="trade-title">与 {{ tradeStore.partnerName }} 的交易</span>
        <button class="trade-close sci-button" @click="handleEndTrade">取消交易</button>
      </div>

      <!-- 交易主区域 -->
      <div class="trade-main">
        <!-- 左侧：对方交易区 -->
        <div class="trade-side partner-side" :class="{ locked: tradeStore.partnerLocked }">
          <div class="trade-side-header">
            <span class="side-title">{{ tradeStore.partnerName }} 的提供</span>
            <span class="lock-status" :class="{ locked: tradeStore.partnerLocked }">
              {{ tradeStore.partnerLocked ? '已锁定' : '未锁定' }}
            </span>
          </div>

          <!-- 对方金额 -->
          <div class="trade-gold">
            <CurrencyDisplay :copper-amount="tradeStore.partnerOfferGold" />
          </div>

          <!-- 对方物品 -->
          <div class="trade-items-grid">
            <div
              v-for="(slot, index) in partnerOfferSlots"
              :key="'partner-' + index"
              class="trade-item-slot"
              :class="{ empty: !slot }"
            >
              <template v-if="slot">
                <span class="item-icon">{{ getItemIcon(slot) }}</span>
                <span class="item-name">{{ slot.name }}</span>
              </template>
            </div>
          </div>
        </div>

        <!-- 中间分隔 -->
        <div class="trade-divider"></div>

        <!-- 右侧：我方交易区 -->
        <div class="trade-side my-side" :class="{ locked: tradeStore.myLocked }">
          <div class="trade-side-header">
            <span class="side-title">我的提供</span>
            <span class="lock-status" :class="{ locked: tradeStore.myLocked }">
              {{ tradeStore.myLocked ? '已锁定' : '未锁定' }}
            </span>
          </div>

          <!-- 我方金额（可编辑） -->
          <div class="trade-gold editable">
            <div class="currency-inputs">
              <div class="currency-input-group">
                <input
                  type="number"
                  class="currency-input sci-input"
                  v-model.number="goldInput"
                  :disabled="tradeStore.myLocked"
                  @change="handleGoldChange"
                  @blur="handleGoldChange"
                  min="0"
                  placeholder="0"
                />
                <span class="currency-label gold">金</span>
              </div>
              <div class="currency-input-group">
                <input
                  type="number"
                  class="currency-input sci-input"
                  v-model.number="silverInput"
                  :disabled="tradeStore.myLocked"
                  @change="handleGoldChange"
                  @blur="handleGoldChange"
                  min="0"
                  max="999"
                  placeholder="0"
                />
                <span class="currency-label silver">银</span>
              </div>
              <div class="currency-input-group">
                <input
                  type="number"
                  class="currency-input sci-input"
                  v-model.number="copperInput"
                  :disabled="tradeStore.myLocked"
                  @change="handleGoldChange"
                  @blur="handleGoldChange"
                  min="0"
                  max="999"
                  placeholder="0"
                />
                <span class="currency-label copper">铜</span>
              </div>
            </div>
            <div class="currency-total">
              <span class="total-label">总计:</span>
              <CurrencyDisplay :copper-amount="tradeStore.myGold" compact />
            </div>
          </div>

          <!-- 我方物品（可点击移除） -->
          <div class="trade-items-grid">
            <div
              v-for="(slot, index) in myOfferSlots"
              :key="'my-' + index"
              class="trade-item-slot"
              :class="{ empty: !slot, clickable: slot && !tradeStore.myLocked }"
              @click="handleRemoveItem(slot)"
            >
              <template v-if="slot">
                <span class="item-icon">{{ getItemIcon(slot) }}</span>
                <span class="item-name">{{ slot.name }}</span>
                <span v-if="!tradeStore.myLocked" class="remove-hint">点击移除</span>
              </template>
            </div>
          </div>
        </div>
      </div>

      <!-- 我方背包区域 -->
      <div class="trade-inventory">
        <div class="inventory-header">
          <span class="inventory-title">我的背包</span>
          <span class="inventory-hint">点击物品添加到交易</span>
        </div>
        <div class="inventory-grid">
          <div
            v-for="(slot, index) in inventorySlots"
            :key="'inv-' + index"
            class="inventory-slot"
            :class="{ empty: !slot, clickable: slot && !tradeStore.myLocked }"
            @click="handleAddItem(slot)"
          >
            <template v-if="slot">
              <span class="item-icon">{{ getItemIcon(slot) }}</span>
              <span v-if="slot.quantity > 1" class="item-count">{{ slot.quantity }}</span>
            </template>
          </div>
        </div>
      </div>

      <!-- 底部操作区 -->
      <div class="trade-actions">
        <label class="lock-checkbox" :class="{ disabled: tradeStore.myConfirmed }">
          <input
            type="checkbox"
            v-model="lockChecked"
            @change="handleLockChange"
            :disabled="tradeStore.myConfirmed"
          />
          <span class="checkbox-label">{{ tradeStore.myLocked ? '已锁定（点击解锁）' : '锁定交易' }}</span>
        </label>

        <button
          class="sci-button primary confirm-btn"
          :class="{ confirmed: tradeStore.myConfirmed }"
          :disabled="!tradeStore.bothLocked || tradeStore.myConfirmed"
          @click="handleConfirm"
        >
          {{ tradeStore.myConfirmed ? '等待对方确认...' : '确认交易' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onUnmounted } from 'vue'
import { useTradeStore } from '../../stores/tradeStore'
import { useAgentStore } from '../../stores/agentStore'
import { useSessionStore } from '../../stores/sessionStore'
import { useCommand } from '../../composables/useCommand'
import CurrencyDisplay from '../common/CurrencyDisplay.vue'
import { breakdownCurrency, toCopper } from '../../utils/currency'

const tradeStore = useTradeStore()
const agentStore = useAgentStore()
const sessionStore = useSessionStore()
const { sendCommand } = useCommand()

// 金额输入值（分为金/银/铜）
const goldInput = ref(0)
const silverInput = ref(0)
const copperInput = ref(0)

// 锁定勾选框
const lockChecked = ref(false)

// 自动刷新定时器
let autoRefreshTimer = null

// 监听 store 中的金额变化，同步到输入框
watch(() => tradeStore.myOfferGold, (newVal) => {
  const breakdown = breakdownCurrency(newVal)
  goldInput.value = breakdown.gold
  silverInput.value = breakdown.silver
  copperInput.value = breakdown.copper
}, { immediate: true })

// 监听 store 中的锁定状态，启动/停止自动刷新
watch(() => tradeStore.myLocked, (newVal) => {
  lockChecked.value = newVal
  // 非AI代理模式下，锁定后启动自动刷新（如果还没确认交易）
  if (newVal && !agentStore.isEnabled && !tradeStore.myConfirmed) {
    startAutoRefresh()
  } else if (!newVal) {
    stopAutoRefresh()
  }
}, { immediate: true })

// 监听确认状态，确认后也需要轮询等待对方确认
watch(() => tradeStore.myConfirmed, (newVal) => {
  if (newVal && tradeStore.isInTrade && !agentStore.isEnabled) {
    startAutoRefresh()
  }
}, { immediate: true })

// 监听交易状态，交易结束时停止自动刷新
watch(() => tradeStore.isInTrade, (newVal) => {
  if (!newVal) {
    stopAutoRefresh()
  }
})

// 组件卸载时清理定时器
onUnmounted(() => {
  stopAutoRefresh()
})

// 启动自动刷新
function startAutoRefresh() {
  stopAutoRefresh() // 先清理已有的定时器
  autoRefreshTimer = setInterval(() => {
    // 锁定状态或已确认状态下，交易进行中时刷新，且不在等待响应时
    const shouldRefresh = (tradeStore.myLocked || tradeStore.myConfirmed) &&
                          tradeStore.isInTrade &&
                          !agentStore.isEnabled &&
                          !sessionStore.isWaiting
    if (shouldRefresh) {
      sendCommand('trade wait 1')
    } else if (!tradeStore.isInTrade) {
      stopAutoRefresh()
    }
    // 如果只是 isWaiting，跳过本次刷新但不停止定时器
  }, 3000) // 每3秒刷新一次
}

// 停止自动刷新
function stopAutoRefresh() {
  if (autoRefreshTimer) {
    clearInterval(autoRefreshTimer)
    autoRefreshTimer = null
  }
}

// 对方物品槽位（固定10个）
const partnerOfferSlots = computed(() => {
  const slots = new Array(10).fill(null)
  tradeStore.partnerOfferItems.forEach((item, index) => {
    if (index < 10) slots[index] = item
  })
  return slots
})

// 我方物品槽位（固定10个）
const myOfferSlots = computed(() => {
  const slots = new Array(10).fill(null)
  tradeStore.myOfferItems.forEach((item, index) => {
    if (index < 10) slots[index] = item
  })
  return slots
})

// 背包槽位（显示可交易的物品）
const inventorySlots = computed(() => {
  const slots = new Array(24).fill(null)
  tradeStore.availableInventory.forEach((item, index) => {
    if (index < 24) slots[index] = item
  })
  return slots
})

// 获取物品图标
function getItemIcon(item) {
  if (!item) return ''
  if (item.isEquipment) return '⚔️'
  if (item.name.includes('药水') || item.name.includes('药剂')) return '🧪'
  if (item.name.includes('技能书')) return '📖'
  return '📦'
}

// 处理金额变化
function handleGoldChange() {
  if (tradeStore.myLocked) return

  // 限制银币和铜币不超过999
  if (silverInput.value > 999) silverInput.value = 999
  if (copperInput.value > 999) copperInput.value = 999
  if (goldInput.value < 0) goldInput.value = 0
  if (silverInput.value < 0) silverInput.value = 0
  if (copperInput.value < 0) copperInput.value = 0

  // 计算总铜币数
  const totalCopper = toCopper(goldInput.value || 0, silverInput.value || 0, copperInput.value || 0)

  // 检查是否超过玩家拥有的金币
  if (totalCopper > tradeStore.myGold) {
    // 重置为玩家拥有的最大金额
    const breakdown = breakdownCurrency(tradeStore.myGold)
    goldInput.value = breakdown.gold
    silverInput.value = breakdown.silver
    copperInput.value = breakdown.copper
    return
  }

  // 发送指令
  sendCommand(`trade money ${goldInput.value || 0} ${silverInput.value || 0} ${copperInput.value || 0}`)
}

// 处理添加物品
function handleAddItem(item) {
  if (!item || tradeStore.myLocked) return
  if (tradeStore.myOfferItems.length >= 10) return
  sendCommand(`trade add ${item.name}`)
}

// 处理移除物品
function handleRemoveItem(item) {
  if (!item || tradeStore.myLocked) return
  sendCommand(`trade remove ${item.name}`)
}

// 处理锁定变化
function handleLockChange() {
  if (lockChecked.value) {
    sendCommand('trade lock')
  } else {
    sendCommand('trade unlock')
  }
}

// 处理确认交易
async function handleConfirm() {
  if (!tradeStore.bothLocked) return

  // 先停止自动刷新
  stopAutoRefresh()

  // 等待当前请求完成
  while (sessionStore.isWaiting) {
    await new Promise(resolve => setTimeout(resolve, 100))
  }

  // 标记已确认
  tradeStore.setMyConfirmed(true)

  // 发送确认命令
  await sendCommand('trade confirm')

  // 确认后启动自动刷新，等待对方确认
  if (tradeStore.isInTrade && !agentStore.isEnabled) {
    startAutoRefresh()
  }
}

// 处理结束交易
function handleEndTrade() {
  sendCommand('trade end')
}
</script>

<style scoped>
.trade-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.6);
  z-index: 100;
}

.trade-window {
  width: 90%;
  max-width: 750px;
  min-width: 600px;
  max-height: 90%;
  display: flex;
  flex-direction: column;
  background: var(--bg-panel);
  border: 1px solid var(--border-color);
  border-radius: var(--panel-radius);
  overflow: hidden;
}

.trade-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  border-bottom: 1px solid var(--border-color);
  background: var(--bg-dark);
}

.trade-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-highlight);
}

.trade-close {
  padding: 6px 12px;
  font-size: 12px;
}

.trade-main {
  display: flex;
  padding: 16px;
  gap: 16px;
}

.trade-side {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 12px;
  border-radius: 8px;
  border: 2px solid transparent;
  transition: border-color 0.3s ease;
}

.trade-side.locked {
  border-color: #f44336;
  box-shadow: 0 0 8px rgba(244, 67, 54, 0.3);
}

.trade-side-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  flex-wrap: wrap;
}

.side-title {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
  min-width: 0;
}

.lock-status {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 4px;
  background: rgba(244, 67, 54, 0.2);
  color: var(--entity-enemy);
  white-space: nowrap;
  flex-shrink: 0;
}

.lock-status.locked {
  background: rgba(76, 175, 80, 0.2);
  color: var(--primary);
}

.trade-gold {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 10px 12px;
  background: var(--bg-dark);
  border-radius: var(--button-radius);
}

.trade-gold.editable {
  background: var(--bg-dark);
}

.currency-inputs {
  display: flex;
  gap: 8px;
  align-items: center;
}

.currency-input-group {
  display: flex;
  align-items: center;
  gap: 4px;
  flex: 1;
}

.currency-input {
  width: 100%;
  padding: 6px 8px;
  font-size: 14px;
  text-align: center;
  background: rgba(0, 0, 0, 0.3);
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.currency-input:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.currency-label {
  font-size: 12px;
  font-weight: 600;
  min-width: 20px;
  text-align: center;
}

.currency-label.gold {
  color: #ffd700;
}

.currency-label.silver {
  color: #c0c0c0;
}

.currency-label.copper {
  color: #cd7f32;
}

.currency-total {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 8px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.total-label {
  font-size: 11px;
  color: var(--text-muted);
}

.trade-items-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 4px;
  min-height: 88px;
}

.trade-item-slot {
  aspect-ratio: 1;
  background: var(--bg-dark);
  border: 1px solid var(--border-color);
  border-radius: 4px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  position: relative;
  padding: 4px;
  min-height: 40px;
}

.trade-item-slot.clickable {
  cursor: pointer;
  transition: all var(--transition-fast);
}

.trade-item-slot.clickable:hover {
  border-color: var(--primary);
  background: var(--bg-hover);
}

.trade-item-slot.empty {
  opacity: 0.5;
}

.trade-item-slot .item-icon {
  font-size: 16px;
}

.trade-item-slot .item-name {
  font-size: 9px;
  color: var(--text-secondary);
  text-align: center;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 100%;
}

.trade-item-slot .remove-hint {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  font-size: 8px;
  color: var(--entity-enemy);
  text-align: center;
  opacity: 0;
  transition: opacity var(--transition-fast);
}

.trade-item-slot.clickable:hover .remove-hint {
  opacity: 1;
}

.trade-divider {
  width: 1px;
  background: linear-gradient(180deg, transparent, var(--border-color), transparent);
}

.trade-inventory {
  padding: 0 16px 16px;
  border-top: 1px solid var(--border-color);
  margin-top: 0;
  padding-top: 16px;
}

.inventory-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.inventory-title {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
}

.inventory-hint {
  font-size: 11px;
  color: var(--text-muted);
}

.inventory-grid {
  display: grid;
  grid-template-columns: repeat(12, 1fr);
  gap: 4px;
}

.inventory-slot {
  aspect-ratio: 1;
  background: var(--bg-dark);
  border: 1px solid var(--border-color);
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  min-height: 32px;
}

.inventory-slot.clickable {
  cursor: pointer;
  transition: all var(--transition-fast);
}

.inventory-slot.clickable:hover {
  border-color: var(--primary);
  background: var(--bg-hover);
}

.inventory-slot.empty {
  opacity: 0.5;
}

.inventory-slot .item-icon {
  font-size: 16px;
}

.inventory-slot .item-count {
  position: absolute;
  bottom: 2px;
  right: 4px;
  font-size: 9px;
  color: var(--text-primary);
  text-shadow: 0 0 2px var(--bg-dark);
}

.trade-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  border-top: 1px solid var(--border-color);
  background: var(--bg-dark);
}

.lock-checkbox {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  user-select: none;
}

.lock-checkbox.disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.lock-checkbox input {
  width: 16px;
  height: 16px;
  cursor: pointer;
}

.lock-checkbox input:disabled {
  cursor: not-allowed;
}

.checkbox-label {
  font-size: 13px;
  color: var(--text-primary);
}

.confirm-btn {
  padding: 10px 24px;
  font-size: 14px;
}

.confirm-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.confirm-btn.confirmed {
  background: rgba(255, 193, 7, 0.3);
  border-color: rgba(255, 193, 7, 0.5);
  color: #ffc107;
}
</style>
