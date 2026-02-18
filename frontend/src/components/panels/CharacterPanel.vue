<template>
  <div class="popup-panel sci-panel character-panel">
    <div class="popup-panel-header">
      <span class="popup-panel-title">角色信息</span>
      <button class="popup-panel-close" @click="uiStore.closePanel()">×</button>
    </div>

    <div class="popup-panel-content sci-scrollbar">
      <!-- 角色头部 -->
      <div class="character-header">
        <div class="character-avatar">{{ playerStore.roleIcon }}</div>
        <div class="character-info">
          <div class="character-name">{{ playerStore.name }}</div>
          <div class="character-class">{{ playerStore.roleName }} Lv.{{ playerStore.level }}</div>
        </div>
      </div>

      <!-- 血条和蓝条 -->
      <div class="character-bars">
        <div class="bar-row">
          <span class="bar-label">HP</span>
          <div class="bar-container">
            <div class="sci-progress">
              <div
                class="sci-progress-bar health"
                :style="{ width: playerStore.healthPercent + '%', backgroundPosition: (100 - playerStore.healthPercent) + '% 0' }"
              ></div>
            </div>
          </div>
          <span class="bar-value">{{ playerStore.currentHealth }}/{{ playerStore.maxHealth }}</span>
        </div>
        <div class="bar-row">
          <span class="bar-label">MP</span>
          <div class="bar-container">
            <div class="sci-progress">
              <div class="sci-progress-bar mana" :style="{ width: playerStore.manaPercent + '%' }"></div>
            </div>
          </div>
          <span class="bar-value">{{ playerStore.currentMana }}/{{ playerStore.maxMana }}</span>
        </div>
        <div class="bar-row">
          <span class="bar-label">EXP</span>
          <div class="bar-container">
            <div class="sci-progress">
              <div class="sci-progress-bar exp" :style="{ width: playerStore.expPercent + '%' }"></div>
            </div>
          </div>
          <span class="bar-value">{{ playerStore.experience }}/{{ playerStore.experienceForNextLevel }}</span>
        </div>
      </div>

      <!-- 金币 -->
      <div class="gold-display">
        <span class="gold-icon">💰</span>
        <span class="gold-value">{{ playerStore.gold }}</span>
      </div>

      <div class="sci-divider"></div>

      <!-- 四维属性 -->
      <div class="attributes-section">
        <div class="section-title">
          属性
          <span v-if="playerStore.freeAttributePoints > 0" class="free-points">
            (可用: {{ playerStore.freeAttributePoints }})
          </span>
        </div>
        <div class="attributes-grid">
          <div class="attribute-item">
            <span class="attribute-name">力量</span>
            <div class="attribute-value">
              <span class="attribute-number">{{ playerStore.strength }}</span>
              <button
                v-if="playerStore.freeAttributePoints > 0"
                class="sci-button attribute-add"
                @click="addAttribute('str')"
              >+</button>
            </div>
          </div>
          <div class="attribute-item">
            <span class="attribute-name">敏捷</span>
            <div class="attribute-value">
              <span class="attribute-number">{{ playerStore.agility }}</span>
              <button
                v-if="playerStore.freeAttributePoints > 0"
                class="sci-button attribute-add"
                @click="addAttribute('agi')"
              >+</button>
            </div>
          </div>
          <div class="attribute-item">
            <span class="attribute-name">智力</span>
            <div class="attribute-value">
              <span class="attribute-number">{{ playerStore.intelligence }}</span>
              <button
                v-if="playerStore.freeAttributePoints > 0"
                class="sci-button attribute-add"
                @click="addAttribute('int')"
              >+</button>
            </div>
          </div>
          <div class="attribute-item">
            <span class="attribute-name">体力</span>
            <div class="attribute-value">
              <span class="attribute-number">{{ playerStore.vitality }}</span>
              <button
                v-if="playerStore.freeAttributePoints > 0"
                class="sci-button attribute-add"
                @click="addAttribute('vit')"
              >+</button>
            </div>
          </div>
        </div>
      </div>

      <div class="sci-divider"></div>

      <!-- 战斗属性 -->
      <div class="attributes-section">
        <div class="section-title">战斗属性</div>
        <div class="combat-stats">
          <div class="stat-row">
            <span>物攻</span><span>{{ playerStore.physicalAttack }}</span>
          </div>
          <div class="stat-row">
            <span>物防</span><span>{{ playerStore.physicalDefense }}</span>
          </div>
          <div class="stat-row">
            <span>法攻</span><span>{{ playerStore.magicAttack }}</span>
          </div>
          <div class="stat-row">
            <span>法防</span><span>{{ playerStore.magicDefense }}</span>
          </div>
          <div class="stat-row">
            <span>速度</span><span>{{ playerStore.speed }}</span>
          </div>
          <div class="stat-row">
            <span>暴击率</span><span>{{ playerStore.critRate }}%</span>
          </div>
          <div class="stat-row">
            <span>暴击伤害</span><span>{{ playerStore.critDamage }}%</span>
          </div>
          <div class="stat-row">
            <span>命中率</span><span>{{ playerStore.hitRate }}%</span>
          </div>
          <div class="stat-row">
            <span>闪避率</span><span>{{ playerStore.dodgeRate }}%</span>
          </div>
        </div>
      </div>

      <div class="sci-divider"></div>

      <!-- 技能列表 -->
      <div class="attributes-section">
        <div class="section-title">技能</div>
        <div class="skills-list">
          <div v-for="skill in playerStore.skills" :key="skill.name" class="skill-item">
            <span class="skill-name">{{ skill.name }}</span>
            <span class="skill-info">
              {{ skill.targetType }} | {{ skill.manaCost }}MP
              <template v-if="skill.cooldown > 0"> | CD:{{ skill.cooldown }}</template>
            </span>
          </div>
          <div v-if="playerStore.skills.length === 0" class="empty-text">
            暂无技能
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useUIStore } from '../../stores/uiStore'
import { usePlayerStore } from '../../stores/playerStore'
import { useCommand } from '../../composables/useCommand'

const uiStore = useUIStore()
const playerStore = usePlayerStore()
const { addAttribute: addAttr } = useCommand()

function addAttribute(attr) {
  addAttr(attr, 1)
}
</script>

<style scoped>
.gold-display {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background: var(--bg-dark);
  border-radius: var(--button-radius);
}

.gold-icon {
  font-size: 16px;
}

.gold-value {
  color: #ffd700;
  font-weight: 600;
}

.free-points {
  color: var(--primary);
  font-size: 11px;
  font-weight: normal;
}

.combat-stats {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 4px;
}

.stat-row {
  display: flex;
  justify-content: space-between;
  padding: 4px 8px;
  font-size: 12px;
}

.stat-row span:first-child {
  color: var(--text-secondary);
}

.stat-row span:last-child {
  color: var(--text-primary);
}

.skills-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.skill-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 6px 10px;
  background: var(--bg-dark);
  border-radius: var(--button-radius);
}

.skill-name {
  color: var(--text-primary);
  font-size: 13px;
}

.skill-info {
  color: var(--text-muted);
  font-size: 11px;
}

.empty-text {
  color: var(--text-muted);
  font-size: 12px;
  text-align: center;
  padding: 12px;
}
</style>
