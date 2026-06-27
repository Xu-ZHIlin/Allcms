<script setup lang="ts">
import { onMounted, ref, computed } from "vue";
import { useRouter, useRoute } from "vue-router";
import { useUserStore } from "@/stores/user.ts";
import {
  Menu as IconMenu,
  Fold,
  Expand,
  SwitchButton
} from '@element-plus/icons-vue'; // 需要安装 @element-plus/icons-vue

const props = defineProps({
  isCollapse: Boolean
});
const emit = defineEmits(['update:is-collapse']);

const router = useRouter();
const route = useRoute();
const userStore = useUserStore();
const menuData = ref([]);

// 动态计算当前激活的路由，配合 Element Plus 高亮
const activeMenu = computed(() => {
  return route.path;
});

// 切换侧边栏折叠
const toggleCollapse = () => {
  emit('update:is-collapse', !props.isCollapse);
};

// 退出登录
const handleLogout = async () => {
  await userStore.logout();
  router.push("/");
};

onMounted(() => {
  menuData.value = userStore.userInfo.menuTree;
});
</script>

<template>
  <aside class="aside">
    <!-- 头部：Logo + 折叠按钮 -->
    <div class="aside-header">
      <div class="logo-area">
        <div class="logo">
<!--          <svg viewBox="0 0 32 32" width="28" height="28">-->
<!--            <rect x="2" y="2" width="28" height="28" rx="6" fill="#1677ff" />-->
<!--            <path d="M10 10h12v4H10zM10 18h8v4H10z" fill="#fff" opacity="0.9" />-->
<!--          </svg>-->
          <span class="logo-text" v-show="!isCollapse">Admin</span>
        </div>

        <div class="toggle-btn" @click="toggleCollapse">
          <el-icon :size="20">
            <Fold v-if="!isCollapse" />
            <Expand v-else />
          </el-icon>
        </div>
      </div>
    </div>

    <!-- 侧边栏核心菜单（Element Plus） -->
    <el-scrollbar class="menu-scrollbar">
      <el-menu
          :default-active="activeMenu"
          class="el-menu-vertical"
          :collapse="isCollapse"
          :collapse-transition="false"
          :unique-opened="true"
          background-color="#ffffff"
          text-color="#4a4a6a"
          active-text-color="#1677ff"
      >
        <template v-for="menu in menuData" :key="menu.id">
          <!-- 如果拥有二级菜单 -->
          <el-sub-menu v-if="menu.children && menu.children.length > 0" :index="menu.path">
            <template #title>
              <el-icon><IconMenu /></el-icon>
              <span>{{ menu.name }}</span>
            </template>
            <el-menu-item
                v-for="child in menu.children"
                :key="child.id"
                :index="child.path"
            >
              <router-link :to="child.path" class="menu-link">{{ child.name }}</router-link>
            </el-menu-item>
          </el-sub-menu>

          <!-- 一级菜单（无子节点） -->
          <el-menu-item v-else :index="menu.path">
            <router-link :to="menu.path" class="menu-link">
              <el-icon><IconMenu /></el-icon>
              <span>{{ menu.name }}</span>
            </router-link>
          </el-menu-item>
        </template>
      </el-menu>
    </el-scrollbar>

    <!-- 底部：退出登录 -->
    <div class="logout-wrapper">
      <div class="logout-btn" @click="handleLogout">
        <el-icon :size="18"><SwitchButton /></el-icon>
        <span v-show="!isCollapse">退出登录</span>
      </div>
    </div>
  </aside>
</template>

<style scoped>
.aside {
  height: 100vh;
  background: #ffffff;
  border-right: 1px solid rgba(0, 0, 0, 0.06);
  display: flex;
  flex-direction: column;
  transition: width 0.3s;
  width: 260px;
  flex-shrink: 0;
}
.aside-header {
  padding: 12px 16px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.04);
  flex-shrink: 0;
}
.logo-area {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  text-decoration: none;
  overflow: hidden;
}
.logo-text {
  font-size: 18px;
  font-weight: 700;
  color: #1a1a2e;
  letter-spacing: 0.5px;
  white-space: nowrap;
}
.toggle-btn {
  cursor: pointer;
  padding: 4px;
  border-radius: 6px;
  color: #4a4a6a;
  transition: all 0.2s;
}
.toggle-btn:hover {
  background: #f5f7fa;
  color: #1677ff;
}

/* 菜单区域 */
.menu-scrollbar {
  flex: 1;
  overflow-y: auto;
  padding: 12px 0;
}
.el-menu-vertical {
  border-right: none;
}
.el-menu-item .menu-link {
  text-decoration: none;
  color: inherit;
  display: flex;
  align-items: center;
  width: 100%;
  height: 100%;
}
.el-menu-item.is-active .menu-link {
  color: #1677ff;
}
:deep(.el-sub-menu__title .el-icon) {
  margin-right: 8px;
}

/* 退出按钮区域 */
.logout-wrapper {
  flex-shrink: 0;
  padding: 12px 16px;
  border-top: 1px solid rgba(0, 0, 0, 0.05);
  background: #ffffff;
}
.logout-btn {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 12px;
  border-radius: 8px;
  cursor: pointer;
  color: #e74c3c;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.2s;
  user-select: none;
}
.logout-btn:hover {
  background: #fef0ee;
  color: #c0392b;
}
.logout-btn:active {
  transform: scale(0.97);
}

/* 折叠状态适配 */
.aside .el-menu-vertical:not(.el-menu--collapse) {
  width: 240px;
}
</style>