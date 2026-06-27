<script setup lang="ts">
</script>

<template>
  <div class="content-wrapper">
    <div class="content-scroll">
      <router-view></router-view>
    </div>
  </div>
</template>

<style scoped>
.content-wrapper {
  flex: 1;
  height: 100vh;
  overflow: hidden;
  background: #f5f7fa;
}
.content-scroll {
  height: 100%;
  overflow-y: auto;
  padding: 20px;
  box-sizing: border-box;
}
</style>