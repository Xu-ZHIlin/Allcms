import {createRouter, createWebHistory} from 'vue-router'
import Index from "@/nav/index.vue";
import UserManage from "@/view/UserManage.vue";
import Login from "@/view/Login.vue";
import RoleInfo from "@/view/RoleInfo.vue";
import Welcome from "@/view/Welcome.vue";
import PermissionManage from "@/view/PermissionManage.vue";


const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {path: '/', component: Login},
    {
      path: '/dashboard',
      component: Index,
      redirect: '/dashboard/welcome',
      children:[
        {path: '/dashboard/welcome', component: Welcome},
        {path: '/dashboard/use/userManage', component: UserManage},
        {path: '/dashboard/role/roleInfo', component: RoleInfo},
        {path: '/role/list', name: 'RoleInfo', component: RoleInfo},
        {path: '/permission/list', name: 'PermissionManage', component: PermissionManage},
        {path: '/account/user', name: 'UserManage', component: UserManage},
      ]
    },//全部都是在index下
  ],
})

export default router
