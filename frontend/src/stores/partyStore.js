import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const usePartyStore = defineStore('party', () => {
  // 队伍信息
  const partyId = ref(null)
  const isInParty = ref(false)
  const isLeader = ref(false)
  const members = ref([])
  const maxMembers = ref(4)

  // 计算属性
  const memberCount = computed(() => members.value.length)

  const isFull = computed(() => memberCount.value >= maxMembers.value)

  // 获取队长
  const leader = computed(() => members.value.find(m => m.isLeader))

  // 更新队伍信息
  function updatePartyInfo(data) {
    console.log('[PartyStore] 更新队伍信息:', data)
    if (data.partyId !== undefined) partyId.value = data.partyId
    if (data.isInParty !== undefined) isInParty.value = data.isInParty
    if (data.isLeader !== undefined) isLeader.value = data.isLeader
    if (data.members !== undefined) members.value = data.members
  }

  // 添加成员
  function addMember(member) {
    if (!members.value.find(m => m.name === member.name)) {
      console.log('[PartyStore] 添加成员:', member.name)
      members.value.push(member)
    }
  }

  // 移除成员
  function removeMember(memberName) {
    const index = members.value.findIndex(m => m.name === memberName)
    if (index !== -1) {
      console.log('[PartyStore] 移除成员:', memberName)
      members.value.splice(index, 1)
    }
  }

  // 更新成员信息
  function updateMember(memberName, updates) {
    const member = members.value.find(m => m.name === memberName)
    if (member) {
      console.log('[PartyStore] 更新成员:', memberName, updates)
      Object.assign(member, updates)
    }
  }

  // 重置状态
  function reset() {
    console.log('[PartyStore] 重置队伍状态')
    partyId.value = null
    isInParty.value = false
    isLeader.value = false
    members.value = []
  }

  return {
    // 状态
    partyId, isInParty, isLeader, members, maxMembers,
    // 计算属性
    memberCount, isFull, leader,
    // 方法
    updatePartyInfo, addMember, removeMember, updateMember, reset
  }
})
