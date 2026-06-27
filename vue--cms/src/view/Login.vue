<template>
  <div class="login-wrapper">
    <div class="login-container">
      <el-card class="login-card">
        <div class="login-header">
          <h2 class="title">CMS 管理系统</h2>
          <p class="subtitle">Content Management System</p>
        </div>

        <el-form
            ref="loginFormRef"
            :model="form"
            :rules="rules"
            label-position="top"
            size="large"
            @submit.prevent="handleSubmit"
        >
          <el-form-item label="用户名" prop="username">
            <el-input
                v-model="form.username"
                placeholder="请输入用户名"
                clearable
            />
          </el-form-item>

          <el-form-item label="密码" prop="password">
            <el-input
                v-model="form.password"
                type="password"
                placeholder="请输入密码"
                show-password
                clearable
            />
          </el-form-item>

          <div class="form-options">
            <el-checkbox v-model="rememberMe">记住我</el-checkbox>
          </div>

          <el-form-item style="margin-top: 10px;">
            <el-button
                type="primary"
                native-type="submit"
                class="submit-btn"
                :loading="loading"
            >
              {{ loading ? '登录中...' : '登 录' }}
            </el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { login } from "@/http/login.ts"
import router from "@/router/index.ts"
import { useUserStore } from "@/stores/user.js"

const userStore = useUserStore()
const loginFormRef = ref(null)
const loading = ref(false)
const rememberMe = ref(false)

// 初始化表单数据
const form = ref({
  username: '',
  password: ''
})

// 表单校验规则
const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码不能少于6位', trigger: 'blur' }
  ]
}

// 组件挂载时，检查本地是否有记住的账号密码
onMounted(() => {
  const savedUsername = localStorage.getItem('cms_username')
  const savedPassword = localStorage.getItem('cms_password')
  if (savedUsername && savedPassword) {
    form.value.username = savedUsername
    form.value.password = savedPassword
    rememberMe.value = true
  }
})

// 处理登录提交（完全改用 async/await，更加稳健）
const handleSubmit = async () => {
  if (!loginFormRef.value) return

  const valid = await loginFormRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const res = await login(form.value)

    if (res.code === 200) {
      // 处理“记住我”逻辑
      if (rememberMe.value) {
        localStorage.setItem('cms_username', form.value.username)
        localStorage.setItem('cms_password', form.value.password)
      } else {
        localStorage.removeItem('cms_username')
        localStorage.removeItem('cms_password')
      }

      // 将用户信息和 Token 存入 Pinia
      userStore.setToken(res.data.token)
      userStore.setUserInfo(res.data)

      ElMessage.success('登录成功，欢迎回来！')
      router.push('/dashboard')
    } else {
      ElMessage.error(res.message || '用户名或密码错误')
    }
  } catch (error) {
    console.error('登录异常:', error)
    ElMessage.error('网络连接失败或服务器异常，请稍后重试')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
/* 登录页大背景 */
.login-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #0f0c29, #302b63, #24243e);
  background-size: 400% 400%;
  animation: gradientBG 15s ease infinite;
}

@keyframes gradientBG {
  0% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
  100% { background-position: 0% 50%; }
}

.login-container {
  width: 420px;
  padding: 20px;
  box-sizing: border-box;
}

.login-card {
  border-radius: 16px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.5) !important;
  background: rgba(255, 255, 255, 0.9) !important;
  backdrop-filter: blur(10px);
  padding: 10px 0 20px 0;
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.title {
  font-size: 28px;
  font-weight: 600;
  color: #302b63;
  margin: 0 0 8px 0;
  letter-spacing: 2px;
}

.subtitle {
  font-size: 14px;
  color: #888;
  margin: 0;
  letter-spacing: 1px;
}

/* 输入框微调 */
:deep(.el-input__wrapper) {
  box-shadow: 0 0 0 1px #dcdfe6 inset;
  transition: all 0.3s;
}
:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #302b63 inset !important;
}

/* 选项区域 */
.form-options {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 10px;
}

/* 登录按钮 */
.submit-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
  letter-spacing: 4px;
  background: linear-gradient(135deg, #302b63, #24243e);
  border: none;
  box-shadow: 0 4px 12px rgba(48, 43, 99, 0.3);
  transition: all 0.3s ease;
}

.submit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(48, 43, 99, 0.5);
}

.submit-btn:active {
  transform: translateY(0);
}

/* 响应式适配 */
@media screen and (max-width: 480px) {
  .login-container {
    width: 90%;
  }
}
</style>