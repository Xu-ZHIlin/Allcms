<script setup lang="ts">
import Layout from './components/Layout.vue';
</script>

<template>
  <Layout>
    <router-view></router-view>
  </Layout>
</template>

<style>
/* 全局基础样式重置 */
* {
  margin: 0;
  padding: 0;
}

body {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background-color: #ffffff;
  margin: 0;
  padding: 0;
  font-family: "微软雅黑", "Microsoft YaHei", Arial, sans-serif;
}

#app {
  width: 100%;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

/* 移除浏览器自带的 input 默认阴影等样式 */
input, select {
  font-family: inherit;
  box-sizing: border-box;
}

/* 导航和列表布局辅助样式 */
.clearfix::after {
  content: "";
  display: block;
  clear: both;
}

.fl {
  float: left;
}

.fr {
  float: right;
}
</style>