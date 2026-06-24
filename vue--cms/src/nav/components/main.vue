<script setup lang="ts">

import Aside from "@/nav/components/aside.vue";
import Content from "@/nav/components/content.vue";
</script>

<template>
  <div class="main">
    <Aside>
    </Aside>
    <Content>
    </Content>
  </div>
</template>

<style scoped>
.main {
  width: 100%;
  height: 80%;
  display: flex;
}
</style>