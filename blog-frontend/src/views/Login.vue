<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <h2>登录个人博客</h2>
        <p>欢迎回来，请登录您的账号</p>
      </div>

      <el-form ref="loginForm" :model="form" :rules="rules" class="login-form">
        <el-form-item prop="username">
          <el-input
              v-model="form.username"
              placeholder="请输入用户名"
              size="large"
              @keyup.enter="handleLogin"
          >
            <template #prefix>
              <el-icon><User /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item prop="password">
          <el-input
              v-model="form.password"
              type="password"
              placeholder="请输入密码"
              size="large"
              show-password
              @keyup.enter="handleLogin"
          >
            <template #prefix>
              <el-icon><Lock /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item>
          <el-button
              type="primary"
              size="large"
              :loading="loading"
              @click="handleLogin"
              class="login-btn"
          >
            登录
          </el-button>
        </el-form-item>

        <div class="login-footer">
          <div class="links">
            <router-link to="/register">还没有账号？立即注册</router-link>
            <router-link to="/">返回首页</router-link>
          </div>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useStore } from 'vuex'
import { useRouter } from 'vue-router'
import { login } from '@/api/auth'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'

const store = useStore()
const router = useRouter()

const form = reactive({
  username: '',
  password: ''
})

const rules = reactive({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 50, message: '长度在 3 到 50 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少为6位', trigger: 'blur' }
  ]
})

const loading = ref(false)

const handleLogin = async () => {
  try {
    loading.value = true
    const response = await login(form)
    store.dispatch('login', {
      token: response.token,
      user: response.user
    })
    ElMessage.success('登录成功')
    router.push('/')
  } catch (error) {
    console.error('登录失败:', error)
    ElMessage.error(error.response?.data?.message || '登录失败，请检查用户名和密码')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 1rem;
  position: relative;
  overflow: hidden;
}

.login-container::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(255,255,255,0.1) 0%, transparent 60%);
  animation: gradientMove 15s ease-in-out infinite alternate;
}

@keyframes gradientMove {
  0% { transform: translate(0, 0); }
  100% { transform: translate(30px, 30px); }
}

.login-box {
  width: 100%;
  max-width: 420px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(102, 126, 234, 0.4);
  overflow: hidden;
  position: relative;
  z-index: 1;
  backdrop-filter: blur(10px);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.login-box:hover {
  transform: translateY(-5px);
  box-shadow: 0 25px 70px rgba(102, 126, 234, 0.5);
}

.login-header {
  padding: 2.5rem 2rem 2rem;
  text-align: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  position: relative;
}

.login-header::after {
  content: '';
  position: absolute;
  bottom: -20px;
  left: 0;
  right: 0;
  height: 40px;
  background: inherit;
  clip-path: polygon(0 100%, 100% 100%, 100% 0);
}

.login-header h2 {
  margin: 0 0 0.5rem 0;
  font-size: 1.75rem;
  font-weight: 700;
  letter-spacing: 1px;
}

.login-header p {
  margin: 0;
  opacity: 0.95;
  font-size: 0.95rem;
}

.login-form {
  padding: 2.5rem 2rem 2rem;
}

.login-btn {
  width: 100%;
  height: 50px;
  font-size: 1rem;
  font-weight: 600;
  border-radius: 8px;
  margin-top: 1rem;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  transition: all 0.3s ease;
  letter-spacing: 2px;
}

.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.4);
  background: linear-gradient(135deg, #5a6fd6 0%, #6a4190 100%);
}

.login-btn:active {
  transform: translateY(0);
}

.login-footer {
  margin-top: 2rem;
}

.links {
  display: flex;
  flex-direction: column;
  gap: 0.8rem;
  text-align: center;
}

.links a {
  color: #666;
  text-decoration: none;
  font-size: 0.9rem;
  transition: all 0.3s;
  position: relative;
  padding: 0.3rem 0;
}

.links a::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  width: 0;
  height: 2px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  transition: all 0.3s ease;
  transform: translateX(-50%);
}

.links a:hover {
  color: #667eea;
}

.links a:hover::after {
  width: 100%;
}

:deep(.el-input) {
  --el-input-height: 50px;
}

:deep(.el-input__wrapper) {
  border-radius: 8px;
  padding-left: 12px;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}

:deep(.el-input__wrapper:hover) {
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.2);
}

:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
}

:deep(.el-form-item) {
  margin-bottom: 1.5rem;
}

:deep(.el-form-item__error) {
  font-size: 0.85rem;
}

:deep(.el-input__prefix) {
  color: #999;
}

:deep(.el-input__suffix) {
  color: #999;
}

@media (max-width: 480px) {
  .login-box {
    max-width: 100%;
  }

  .login-header {
    padding: 2rem 1.5rem 1.5rem;
  }

  .login-header h2 {
    font-size: 1.5rem;
  }

  .login-form {
    padding: 2rem 1.5rem;
  }
}
</style>
