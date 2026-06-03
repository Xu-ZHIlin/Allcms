<template>
  <div class="login-container">
    <el-card class="login-form">
      <h2 class="title">CMS 管理系统</h2>
      <el-form
          ref="loginFormRef"
          :model="form"
          :rules="rules"
          label-position="top"
          @submit.prevent="handleSubmit"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input
              v-model="form.password"
              type="password"
              placeholder="请输入密码"
              show-password
          />
        </el-form-item>

        <el-form-item>
          <el-button
              type="primary"
              native-type="submit"
              style="width: 100%"
              :loading="loading"
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import {ref } from 'vue'
import {login} from "@/http/login.ts"
import router from "@/router/index.ts"
import {useUserStore} from "@/stores/user.js";

// 如果需要路由跳转，可引入 useRouter
// import { useRouter } from 'vue-router'

const form = ref({
  username: 'XZL',
  password: '123456'
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码不能少于6位', trigger: 'blur' }
  ]
}

const loginFormRef = ref(null)
const loading = ref(false)

const handleSubmit = () => {
  loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        //登录请求
        login(form.value).then((data) => {
          console.log("后端返回的数据：",data)
          if (data.code===200) {
            //把后端通过用户名密码识别的该用户拥有的权限信息数据传到某个地方，以便前端各组件获取（）
            // localStorage.setItem("userInfo", JSON.stringify(data))
            const userStore = useUserStore();
            // 1. 存入 Token (Action 里会同步到 localStorage)
            userStore.setToken(data.data.token);
            userStore.setUserInfo(data.data);
            router.push('/dashboard')
          }
        })
      } catch (error) {
        console.error('登录失败:', error)
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: #f5f7fa;
}

.login-form {
  width: 400px;
  padding: 30px;
  border-radius: 10px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.title {
  text-align: center;
  margin-bottom: 30px;
  color: #333;
}
</style>