<template>
  <div class="popup-panel sci-panel party-panel">
    <div class="popup-panel-header">
      <span class="popup-panel-title">队伍 ({{ partyStore.memberCount }}/{{ partyStore.maxMembers }})</span>
      <button class="popup-panel-close" @click="uiStore.closePanel()">×</button>
    </div>

    <div class="popup-panel-content">
      <template v-if="partyStore.isInParty">
        <!-- 成员列表 -->
        <div
          v-for="member in partyStore.members"
          :key="member.name"
          class="party-member"
          :class="{ self: member.name === playerStore.name }"
        >
          <div class="member-avatar">👤</div>
          <div class="member-info">
            <div class="member-name">
              {{ member.name }}
              <span v-if="member.isLeader" class="leader-badge">队长</span>
            </div>
            <div class="member-health">
              <div class="sci-progress" style="height: 4px;">
                <div class="sci-progress-bar health" style="width: 100%;"></div>
              </div>
            </div>
          </div>
          <div class="member-actions" v-if="partyStore.isLeader && member.name !== playerStore.name">
            <button class="sci-button" style="padding: 4px 8px; font-size: 11px;" @click="kickMember(member.name)">
              踢出
            </button>
          </div>
        </div>

        <!-- 队伍操作 -->
        <div class="party-actions">
          <button
            v-if="partyStore.isLeader"
            class="sci-button"
            style="flex: 1;"
            @click="disbandParty"
          >
            解散队伍
          </button>
          <button
            v-else
            class="sci-button"
            style="flex: 1;"
            @click="leaveParty"
          >
            离开队伍
          </button>
        </div>
      </template>

      <template v-else>
        <div class="no-party">
          <p>你当前没有队伍</p>
          <p class="hint">点击其他玩家可以邀请组队</p>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup>
import { useUIStore } from '../../stores/uiStore'
import { usePlayerStore } from '../../stores/playerStore'
import { usePartyStore } from '../../stores/partyStore'
import { useCommand } from '../../composables/useCommand'

const uiStore = useUIStore()
const playerStore = usePlayerStore()
const partyStore = usePartyStore()
const { partyKick, partyEnd, partyLeave } = useCommand()

function kickMember(name) {
  partyKick(name)
}

function disbandParty() {
  partyEnd()
}

function leaveParty() {
  partyLeave()
}
</script>

<style scoped>
.no-party {
  text-align: center;
  padding: 24px;
  color: var(--text-secondary);
}

.no-party p {
  margin: 8px 0;
}

.no-party .hint {
  font-size: 12px;
  color: var(--text-muted);
}
</style>
