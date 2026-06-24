import {createRouter, createWebHistory} from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    // 登录页
    {path: '/', component: () => import('@/view/Login.vue')},
    // 后台主布局框架（侧边栏+内容容器）
    {
      path: '/dashboard',
      component: () => import('@/nav/index.vue'),
      redirect: '/dashboard/welcome',
      children:[
        {path: '/dashboard/welcome', component: () => import('@/view/Welcome.vue')},
        {path: '/dashboard/use/userManage', component: () => import('@/view/UserManage.vue')},
        {path: '/dashboard/role/roleInfo', component: () => import('@/view/RoleInfo.vue')},
        {path: '/role/list', name: 'RoleInfo', component: () => import('@/view/RoleInfo.vue')},
        {path: '/permission/list', name: 'PermissionManage', component: () => import('@/view/PermissionManage.vue')},
        {path: '/account/user', name: 'UserManage', component: () => import('@/view/UserManage.vue')},
        // 新增【新闻管理页面】和老师文档路径保持一致
        {
          path: '/content/news',
          name: 'NewsManager',
          component: () => import('@/view/NewsManager.vue')
        }
      ]
    }
  ],
})

export default router