<script setup lang="ts">
import { onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import { useUserStore } from "@/stores/user.ts";

const router = useRouter();
const userStore = useUserStore();
const menuData = ref([]);

onMounted(() => {
  menuData.value = userStore.userInfo.menuTree;
});

const activeIndex = ref<Number>(-1);

const toggleMenu = (idx: number) => {
  if (activeIndex.value === idx) {
    activeIndex.value = -1;
  } else {
    activeIndex.value = idx;
  }
};

// ---------- 退出登录 ----------
const handleLogout = async () => {
  // 调用 store 的 logout 方法（清除 token、用户信息等）
  await userStore.logout();
  // 跳转到登录页（根据实际路由调整）
  router.push("/");
};
</script>

<template>
  <aside class="aside">
    <!-- 头部 Logo -->
    <div class="aside-header">
      <div class="logo">
        <svg viewBox="0 0 32 32" width="28" height="28">
          <rect x="2" y="2" width="28" height="28" rx="6" fill="#1677ff" />
          <path d="M10 10h12v4H10zM10 18h8v4H10z" fill="#fff" opacity="0.9" />
        </svg>
        <span class="logo-text">Admin</span>
      </div>
    </div>

    <!-- 菜单导航 -->
    <nav class="menu-wrapper">
      <ul class="menu">
        <li v-for="(menu, idx) in menuData" :key="menu.id">
          <div
              class="menu-title"
              :class="{
              active: activeIndex === idx,
              'has-children': menu.children && menu.children.length > 0,
            }"
              @click="toggleMenu(idx)"
          >
            <div class="left">
              <span class="menu-icon">
                <slot name="icon" :menu="menu">
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <rect x="3" y="3" width="18" height="18" rx="2" />
                    <line x1="9" y1="3" x2="9" y2="21" />
                  </svg>
                </slot>
              </span>
              <router-link :to="menu.path" class="menu-link">{{ menu.name }}</router-link>
            </div>
            <i v-if="menu.children && menu.children.length > 0" class="arrow" :class="{ open: activeIndex === idx }"></i>
          </div>

          <transition name="slide">
            <ul v-if="menu.children && menu.children.length > 0" class="submenu" v-show="activeIndex === idx">
              <li v-for="secondMenu in menu.children" :key="secondMenu.id">
                <router-link :to="secondMenu.path" class="submenu-link">
                  <span class="submenu-dot"></span>
                  {{ secondMenu.name }}
                </router-link>
              </li>
            </ul>
          </transition>
        </li>
      </ul>
    </nav>

    <!-- ---------- 退出按钮（固定在底部） ---------- -->
    <div class="logout-wrapper">
      <div class="logout-btn" @click="handleLogout">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4" />
          <polyline points="16 17 21 12 16 7" />
          <line x1="21" y1="12" x2="9" y2="12" />
        </svg>
        <span>退出登录</span>
      </div>
    </div>
  </aside>
</template>

<style scoped>
/* ========== 原有样式（保持不变） ========== */
.aside {
  width: 260px;
  height: 100vh;
  background: #ffffff;
  border-right: 1px solid rgba(0, 0, 0, 0.06);
  display: flex;
  flex-direction: column;
  position: sticky;
  top: 0;
  overflow: hidden;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
}

.aside-header {
  padding: 20px 20px 16px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.04);
  flex-shrink: 0;
}

.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  text-decoration: none;
}

.logo-text {
  font-size: 18px;
  font-weight: 700;
  color: #1a1a2e;
  letter-spacing: 0.5px;
}

.menu-wrapper {
  flex: 1;
  overflow-y: auto;
  padding: 12px 12px 20px;
}

.menu-wrapper::-webkit-scrollbar {
  width: 4px;
}
.menu-wrapper::-webkit-scrollbar-thumb {
  background: #d0d5dd;
  border-radius: 4px;
}
.menu-wrapper::-webkit-scrollbar-track {
  background: transparent;
}

.menu {
  list-style: none;
  padding: 0;
  margin: 0;
}
.menu > li {
  margin-bottom: 2px;
  border-radius: 10px;
  overflow: hidden;
}

.menu-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 14px;
  border-radius: 10px;
  cursor: pointer;
  color: #4a4a6a;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  user-select: none;
}
.menu-title:hover {
  background: #f5f7fa;
  color: #1677ff;
}
.menu-title:active {
  transform: scale(0.98);
}
.menu-title .left {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
  min-width: 0;
}
.menu-title .left .menu-link {
  text-decoration: none;
  color: inherit;
  transition: color 0.2s;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.menu-title .left .menu-link:hover {
  color: inherit;
}

.menu-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  flex-shrink: 0;
  color: #8c8ca8;
  transition: all 0.3s;
}
.menu-title.active .menu-icon {
  color: #1677ff;
}
.menu-title:hover .menu-icon {
  color: #1677ff;
}

.menu-title.active {
  background: #e8f0fe;
  color: #1677ff;
}
.menu-title.active .menu-link {
  color: #1677ff;
}

.arrow {
  flex-shrink: 0;
  width: 18px;
  height: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  color: #b0b0c8;
  margin-left: 8px;
}
.arrow::before {
  content: "";
  display: inline-block;
  width: 6px;
  height: 6px;
  border-right: 1.5px solid currentColor;
  border-bottom: 1.5px solid currentColor;
  transform: rotate(45deg);
  transition: inherit;
}
.arrow.open {
  transform: rotate(180deg);
  color: #1677ff;
}
.menu-title.active .arrow {
  color: #1677ff;
}

.submenu {
  list-style: none;
  padding: 4px 0 8px 0;
  margin: 0;
  overflow: hidden;
}
.submenu li {
  position: relative;
}
.submenu-link {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 14px 8px 54px;
  color: #5a5a7a;
  text-decoration: none;
  font-size: 13px;
  font-weight: 400;
  border-radius: 8px;
  transition: all 0.2s;
}
.submenu-link:hover {
  background: #f5f7fa;
  color: #1677ff;
}
.submenu-dot {
  display: inline-block;
  width: 5px;
  height: 5px;
  border-radius: 50%;
  background: #c8c8e0;
  flex-shrink: 0;
  transition: all 0.3s;
}
.submenu-link:hover .submenu-dot {
  background: #1677ff;
  box-shadow: 0 0 0 3px rgba(22, 119, 255, 0.15);
}

.slide-enter-active,
.slide-leave-active {
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  max-height: 500px;
  opacity: 1;
  overflow: hidden;
}
.slide-enter-from {
  max-height: 0;
  opacity: 0;
  transform: translateY(-6px);
}
.slide-leave-to {
  max-height: 0;
  opacity: 0;
  transform: translateY(-6px);
}

.menu > li:not(:last-child)::after {
  content: "";
  display: block;
  margin: 2px 16px 0;
  height: 1px;
  background: rgba(0, 0, 0, 0.04);
}
.menu > li:last-child::after {
  display: none;
}

/* ========== 退出按钮区域（新增） ========== */
.logout-wrapper {
  flex-shrink: 0;
  padding: 12px 16px 20px;
  border-top: 1px solid rgba(0, 0, 0, 0.05);
  background: #ffffff;
}

.logout-btn {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 14px;
  border-radius: 10px;
  cursor: pointer;
  color: #e74c3c;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  user-select: none;
}

.logout-btn:hover {
  background: #fef0ee;
  color: #c0392b;
}

.logout-btn:active {
  transform: scale(0.97);
}

.logout-btn svg {
  flex-shrink: 0;
  transition: transform 0.2s;
}

.logout-btn:hover svg {
  transform: translateX(2px);
}

/* 响应式适配 */
@media (max-width: 768px) {
  .aside {
    width: 220px;
  }
  .menu-title {
    padding: 8px 12px;
    font-size: 13px;
  }
  .submenu-link {
    padding: 7px 12px 7px 46px;
    font-size: 12px;
  }
  .logo-text {
    font-size: 16px;
  }
  .logout-btn {
    font-size: 13px;
    padding: 8px 12px;
  }
}
</style>