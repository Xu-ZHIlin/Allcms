<template>
  <div class="layout-wrapper">
    <!-- 顶部蓝条 -->
    <div class="top-bar"></div>

    <!-- 1.顶部logo、搜索框区域 -->
    <header class="top">
      <div class="logo">
        <router-link to="/">
          <img src="https://www.guet.edu.cn/_upload/tpl/00/1d/29/template29/htmlRes/logo.png" width="384" height="70" alt="学院Logo" />
        </router-link>
      </div>

      <div class="topR">
        <div class="topR_t">
          <a href="javascript:void(0)" @click="addToFavorites">加入收藏</a>
          |
          <a href="#">学校主页</a>
        </div>

        <div class="Search">
          <form @submit.prevent="handleSearch">
            <input type="text" v-model="searchQuery" placeholder="请输入关键字搜索" />
          </form>
        </div>
      </div>
    </header>

    <!-- 2. 顶部导航栏 -->
    <NavBar />

    <!-- 3. 主体区域 -->
    <main>
      <slot />
    </main>

    <!-- 4. 底部 -->
    <footer class="foot_two">
      <p>
        Copyright© 2018 All Rights Reserved. 桂林电子科技大学数学与计算科学学院
      </p>
      <div class="choice">
        <select name="select" @change="handleLinkChange" class="select-links">
          <option value="">== 相关链接 ==</option>
          <option v-for="link in selectLinks" :key="link.name" :value="link.url">
            {{ link.name }}
          </option>
        </select>
        <div class="pt10">
          <a
            class="lh1"
            href="#"
            title="管理入口"
            >管理入口</a
          >
        </div>
      </div>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import NavBar from './NavBar.vue';
import { selectLinks } from '../data/newsData';

const searchQuery = ref('');

const addToFavorites = () => {
  alert('请按 Ctrl + D 将本页面加入收藏夹');
};

const handleSearch = () => {
  if (searchQuery.value.trim()) {
    alert(`正在搜索关键字: "${searchQuery.value}"`);
  }
};

const handleLinkChange = (e: Event) => {
  const target = e.target as HTMLSelectElement;
  const url = target.value;
  if (url) {
    window.open(url, '_blank');
    target.value = ''; // 重置选择器
  }
};
</script>

<style scoped>
.layout-wrapper {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  width: 100%;
}

/* 顶部蓝条 */
.top-bar {
  width: 100%vh;
  height: 4px;
  background-color: #275895;
}

/* 1.头部 */
header.top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 0 auto;
  width: 1000px;
  height: 112px;
}

/* 右侧整体区域 */
.topR {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 7px;
}

/* 右侧文字 */
.topR_t {
  color: #666666;
  font-family: "微软雅黑";
  font-size: 12px;
  padding: 0 5px;
}

.topR_t a {
  color: #666666;
  text-decoration: none;
  font-family: "微软雅黑";
  font-size: 12px;
  padding: 0 5px;
}

.topR_t a:hover {
  color: #275895;
  text-decoration: underline;
}

/* 搜索框整体区域 */
.Search {
  width: 287px;
  height: 38px;
  color: #999999;
  margin: 6px 0 0 0;
}

/* 输入框 */
.Search input {
  outline: none;
  width: 257px; /* 减去 padding */
  height: 33px;
  margin: 2px 0 0 15px;
  border: 1px solid #ddd;
  border-radius: 18px;
  padding: 0 15px;
  font-family: "微软雅黑";
  font-size: 12px;
}

.Search input:focus {
  border-color: #275895;
}

/* 3. 主区域 */
main {
  flex: 1;
  display: flex;
  flex-direction: column;
  width: 1000px;
  margin: 0 auto;
  height: auto;
  background-color: #ffffff;
}

/* 5.底部 */
.foot_two {
  display: flex;
  justify-content: space-between;/* 主轴 */
  align-items: center;/* 交叉轴 */
  background-color: #224b77;
  color: #ffffff;
  width: 100%vh;
  height: 96px;
}

.foot_two p {
  font-family: "微软雅黑";
  font-size: 13px;
  width: 600px;
  line-height: 32px;
  margin-left: 267px;
}

.choice {
  margin-right: 261px;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  /* width: 230px; */
}

.choice select {
  height: 28px;
  width: 230px;
  line-height: 28px;
  font-size: 14px;
  font-family: "微软雅黑";
  margin-top: 10px;
}

.pt10 {
  display: flex;
  justify-content: right;
  padding: 5px 0;
}

.pt10 a {
  text-decoration: none;
  color: #ffffff;
  font-family: "微软雅黑";
  font-size: 14px;
}

.pt10 a:hover {
  text-decoration: underline;
}

@media (max-width: 1000px) {
  header.top, main {
    width: 100%vh;
    padding: 0 10px;
    box-sizing: border-box;
  }
}
</style>
