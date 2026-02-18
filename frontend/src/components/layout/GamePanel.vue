<template>
  <div class="game-panel">
    <!-- 地图视图 -->
    <MapView />

    <!-- 功能按钮栏 -->
    <ActionBar />

    <!-- 弹出面板 -->
    <CharacterPanel v-if="uiStore.activePanel === 'character'" />
    <InventoryPanel v-if="uiStore.activePanel === 'inventory'" />
    <PartyPanel v-if="uiStore.activePanel === 'party'" />
    <EntityListPanel v-if="uiStore.activePanel === 'entities'" />
  </div>
</template>

<script setup>
import { useUIStore } from '../../stores/uiStore'
import MapView from '../map/MapView.vue'
import ActionBar from '../panels/ActionBar.vue'
import CharacterPanel from '../panels/CharacterPanel.vue'
import InventoryPanel from '../panels/InventoryPanel.vue'
import PartyPanel from '../panels/PartyPanel.vue'
import EntityListPanel from '../panels/EntityListPanel.vue'

const uiStore = useUIStore()
</script>
