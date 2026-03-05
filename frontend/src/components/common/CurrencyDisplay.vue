<template>
  <span class="currency-display" :class="{ compact, large }">
    <template v-if="gold > 0">
      <span class="currency-unit gold">
        <span class="currency-icon">🟡</span>
        <span class="currency-value">{{ gold }}</span>
      </span>
    </template>
    <template v-if="silver > 0">
      <span class="currency-unit silver">
        <span class="currency-icon">⚪</span>
        <span class="currency-value">{{ silver }}</span>
      </span>
    </template>
    <template v-if="copper > 0 || (gold === 0 && silver === 0)">
      <span class="currency-unit copper">
        <span class="currency-icon">🟤</span>
        <span class="currency-value">{{ copper }}</span>
      </span>
    </template>
  </span>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  // 可以传入铜币总数，或者传入已解析的文本
  copperAmount: {
    type: Number,
    default: 0
  },
  // 或者传入格式化的文本 "1金234银567铜"
  text: {
    type: String,
    default: ''
  },
  // 紧凑模式（更小的间距）
  compact: {
    type: Boolean,
    default: false
  },
  // 大号模式
  large: {
    type: Boolean,
    default: false
  }
})

// 解析货币文本或计算货币分级
const currencyData = computed(() => {
  if (props.text) {
    // 从文本解析 "1金234银567铜"
    const goldMatch = props.text.match(/(\d+)金/)
    const silverMatch = props.text.match(/(\d+)银/)
    const copperMatch = props.text.match(/(\d+)铜/)

    return {
      gold: goldMatch ? parseInt(goldMatch[1]) : 0,
      silver: silverMatch ? parseInt(silverMatch[1]) : 0,
      copper: copperMatch ? parseInt(copperMatch[1]) : 0
    }
  } else {
    // 从铜币总数计算
    const total = props.copperAmount || 0
    const g = Math.floor(total / 1000000)
    const s = Math.floor((total % 1000000) / 1000)
    const c = total % 1000

    return { gold: g, silver: s, copper: c }
  }
})

const gold = computed(() => currencyData.value.gold)
const silver = computed(() => currencyData.value.silver)
const copper = computed(() => currencyData.value.copper)
</script>

<style scoped>
.currency-display {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-family: 'Courier New', monospace;
}

.currency-display.compact {
  gap: 4px;
}

.currency-unit {
  display: inline-flex;
  align-items: center;
  gap: 3px;
  padding: 2px 6px;
  border-radius: 4px;
  background: rgba(0, 0, 0, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.1);
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.3);
}

.currency-icon {
  font-size: 12px;
  filter: drop-shadow(0 0 2px rgba(0, 0, 0, 0.5));
}

.currency-value {
  font-size: 13px;
  font-weight: 600;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.5);
}

/* 金币样式 */
.currency-unit.gold {
  background: linear-gradient(135deg, rgba(255, 215, 0, 0.2), rgba(255, 193, 7, 0.15));
  border-color: rgba(255, 215, 0, 0.4);
}

.currency-unit.gold .currency-value {
  color: #ffd700;
}

/* 银币样式 */
.currency-unit.silver {
  background: linear-gradient(135deg, rgba(192, 192, 192, 0.2), rgba(169, 169, 169, 0.15));
  border-color: rgba(192, 192, 192, 0.4);
}

.currency-unit.silver .currency-value {
  color: #c0c0c0;
}

/* 铜币样式 */
.currency-unit.copper {
  background: linear-gradient(135deg, rgba(205, 127, 50, 0.2), rgba(184, 115, 51, 0.15));
  border-color: rgba(205, 127, 50, 0.4);
}

.currency-unit.copper .currency-value {
  color: #cd7f32;
}

/* 大号模式 */
.currency-display.large .currency-unit {
  padding: 4px 10px;
}

.currency-display.large .currency-icon {
  font-size: 16px;
}

.currency-display.large .currency-value {
  font-size: 16px;
}
</style>
