<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <div class="logo-icon">
          <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path
              d="M12 2L15.09 8.26L22 9.27L17 14.14L18.18 21.02L12 17.77L5.82 21.02L7 14.14L2 9.27L8.91 8.26L12 2Z"
              fill="currentColor" />
          </svg>
        </div>
        <h2 class="login-title">星级评定业务平台</h2>
        <p class="login-subtitle">请使用您的专属账号登录系统</p>
      </div>

      <el-form :model="loginForm" :rules="rules" ref="loginFormRef" class="login-form">
        <el-form-item prop="username">
          <el-input 
            v-model="loginForm.username" 
            placeholder="请输入账号" 
            prefix-icon="User"
            size="large"
            clearable
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input 
            v-model="loginForm.password" 
            type="password" 
            placeholder="请输入密码" 
            prefix-icon="Lock"
            size="large"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        
        <el-form-item>
          <el-button 
            type="primary" 
            class="login-submit-btn" 
            size="large" 
            :loading="loading"
            @click="handleLogin"
          >
            登录系统
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { User, Lock } from '@element-plus/icons-vue';
import request from '../utils/request';

const emit = defineEmits(['login-success']);

const loginFormRef = ref(null);
const loading = ref(false);

const loginForm = reactive({
  username: '',
  password: ''
});

const rules = reactive({
  username: [
    { required: true, message: '请输入登录账号', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入登录密码', trigger: 'blur' }
  ]
});

const handleLogin = async () => {
  if (!loginFormRef.value) return;
  
  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true;
      try {
        const { username, password } = loginForm;
        const res = await request.post('/auth/login', { username, password });
        
        if (res.token) {
          localStorage.setItem('token', res.token);
          localStorage.setItem('role', res.role);
          localStorage.setItem('userType', res.userType || '');
          localStorage.setItem('displayName', res.displayName || '');
          localStorage.setItem('shopName', res.shopName || '');
          localStorage.setItem('permissionCodes', JSON.stringify(res.permissionCodes || []));
          localStorage.setItem('roleCodes', JSON.stringify(res.roleCodes || []));
          if (res.shopCode) {
            localStorage.setItem('shopCode', res.shopCode);
          }
          
          if (res.role === 'ADMIN') {
            ElMessage.success('登录成功，欢迎来到厂端管理中控区');
            emit('login-success', 'admin');
          } else {
            ElMessage.success(`登录成功，欢迎来到${res.shopName || '服务店门户'}`);
            emit('login-success', 'shop');
          }
        } else {
          ElMessage.error('登录失败，未获取到鉴权凭证');
        }
      } catch (error) {
        ElMessage.error(error.response?.data?.message || '账号或密码错误，请检查！');
      } finally {
        loading.value = false;
      }
    }
  });
};
</script>
<style scoped>
.login-container {
  height: 100vh;
  width: 100vw;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #f0f7ff 0%, #e6f3ff 100%);
}

.login-box {
  width: 100%;
  max-width: 420px;
  background: #ffffff;
  padding: 48px 40px;
  border-radius: 16px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.05), 0 1px 8px rgba(0, 0, 0, 0.02);
  display: flex;
  flex-direction: column;
}

.login-header {
  text-align: center;
  margin-bottom: 40px;
}

.logo-icon {
  width: 48px;
  height: 48px;
  color: var(--primary-color);
  margin: 0 auto 16px;
}

.logo-icon svg {
  width: 100%;
  height: 100%;
}

.login-title {
  font-size: 24px;
  color: #1a1a1a;
  font-weight: 600;
  margin: 0 0 8px 0;
}

.login-subtitle {
  font-size: 14px;
  color: #666;
  margin: 0;
}

.login-form {
  margin-bottom: 16px;
}

:deep(.el-input__wrapper) {
  padding: 4px 12px;
  box-shadow: 0 0 0 1px #e2e8f0 inset;
}

:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px var(--primary-color) inset;
}

:deep(.el-input__inner) {
  height: 40px;
}

.login-submit-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 500;
  margin-top: 12px;
  border-radius: 8px;
  transition: all 0.3s ease;
}
</style>
