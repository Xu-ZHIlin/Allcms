<script setup lang="ts">
import Aside from "@/nav/components/aside.vue";
import Content from "@/nav/components/content.vue";
import { ref } from 'vue';

// 控制侧边栏折叠状态
const isCollapse = ref(false);
</script>

<template>
  <div class="main-layout">
    <!-- 传递折叠状态给 Aside -->
    <Aside :is-collapse="isCollapse" @update:is-collapse="isCollapse = $event" />

    <Content />
  </div>
</template>

<style scoped>
.main-layout {
  width: 100vw;
  height: 100vh;
  display: flex;
  background-color: #f5f7fa;
  overflow: hidden;
}
</style>