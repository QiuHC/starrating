<template>
  <div class="app-container">
    <transition name="fade-slide" mode="out-in">
      
      <Login v-if="!userRole" @login-success="handleLoginSuccess" key="login" />

      <!-- 服务店前台门户视图 -->
      <div v-else-if="userRole === 'shop'" class="portal-view" key="portal">
        <!-- 顶部导航栏 -->
        <header class="app-header">
          <div class="header-left">
            <div class="logo-area">
              <div class="logo-icon">
                <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path
                    d="M12 2L15.09 8.26L22 9.27L17 14.14L18.18 21.02L12 17.77L5.82 21.02L7 14.14L2 9.27L8.91 8.26L12 2Z"
                    fill="currentColor" />
                </svg>
              </div>
              <h1 class="app-title">星级评定业务平台门户</h1>
            </div>
          </div>
          
          <div class="header-right">
            <div class="header-status">
              <span :class="['status-dot', registrationOpen ? 'open' : 'closed']"></span>
              <span class="status-text">{{ registrationOpen ? '报名进行中' : '报名已关闭' }}</span>
            </div>
            <el-button type="danger" plain size="small" style="margin-left: 16px;" @click="handleLogout">
              退出登录
            </el-button>
          </div>
        </header>

        <!-- 主内容区域 -->
        <main class="app-main">
          <transition name="fade-slide" mode="out-in">
            <RegistrationForm 
              v-if="currentView === 'registration-form'" 
              :registration-open="registrationOpen"
              :registrations="registrations" 
              @submit-registration="handleSubmitRegistration" 
            />
          </transition>
        </main>

        <!-- 底部 -->
        <footer class="app-footer">
          <p>@服务工程部 星级评定业务平台</p>
        </footer>
      </div>

      <!-- 厂端全局管理后台视图 -->
      <AdminLayout 
        v-else-if="userRole === 'admin'" 
        key="admin"
        :registrations="registrations"
        @update-period="handleUpdatePeriod"
        @approve="handleApprove" 
        @reject="handleReject" 
        @logout="handleLogout"
      />
      
    </transition>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import Login from './components/Login.vue';
import RegistrationForm from './components/RegistrationForm.vue';
import AdminLayout from './components/AdminLayout.vue';
import request from './utils/request';

// 当前视图状态
const currentView = ref('registration-form');
// 认证权限角色：null, 'shop', 'admin'
const userRole = ref(localStorage.getItem('role') || null);

// 全局报名状态控制
const registrationOpen = ref(true);

// 动态获取的报名列表
const registrations = ref([]);

const mapStatusToChinese = (status) => {
  if (status === 'PENDING') return '待审核';
  if (status === 'APPROVED') return '已通过';
  if (status === 'REJECTED') return '已退回';
  return status;
};

const fetchRegistrations = async () => {
  try {
    const role = localStorage.getItem('role');
    let url = '/registrations/list';
    if (role === 'SHOP' || role === 'shop') {
      const shopCode = localStorage.getItem('shopCode');
      url += `?shopCode=${shopCode}`;
    }
    const res = await request.get(url);
    registrations.value = (res || []).map(item => ({
      ...item,
      createdAt: item.createTime ? new Date(item.createTime).toLocaleString() : '',
      status: mapStatusToChinese(item.status)
    }));
  } catch (error) {
    ElMessage.error('获取申报列表失败，请检查网络或重新登录');
  }
};

const fetchConfig = async () => {
  try {
    const res = await request.get('/config/registration_period');
    if (res && res.configValue) {
      const period = JSON.parse(res.configValue);
      if (period.startTime && period.endTime) {
         const now = new Date();
         const start = new Date(period.startTime);
         const end = new Date(period.endTime);
         registrationOpen.value = now >= start && now <= end;
      }
    }
  } catch (e) {
    // 静默失败，保持默认
  }
};

const handleUpdatePeriod = async (period) => {
  try {
    await request.post('/config/period', period);
    ElMessage.success(`报名周期已更新：${period.startTime} 至 ${period.endTime}`);
  } catch (error) {
    ElMessage.error('更新周期失败');
  }
};

// 提交或重新提交报名
const handleSubmitRegistration = async (formData) => {
  try {
    const shopCode = localStorage.getItem('shopCode');
    const role = localStorage.getItem('role');
    // 对于 SHOP 角色强制使用绑定的 shopCode
    const payload = { ...formData };
    if ((role === 'SHOP' || role === 'shop') && shopCode) {
      payload.shopCode = shopCode;
    }
    await request.post('/registrations/submit', payload);
    ElMessage.success('提交报名成功，等待后台审核！');
    fetchRegistrations();
  } catch (error) {
    ElMessage.error('提交失败：' + (error.response?.data?.message || '未知错误'));
  }
};

const handleApprove = async (id) => {
  try {
    await request.post(`/registrations/${id}/approve`);
    ElMessage.success('审核已通过');
    fetchRegistrations();
  } catch (error) {
    ElMessage.error('操作失败');
  }
};

const handleReject = async ({ id, reason }) => {
  try {
    await request.post(`/registrations/${id}/reject`, { reason });
    ElMessage.success('记录已退回');
    fetchRegistrations();
  } catch (error) {
    ElMessage.error('操作失败');
  }
};

const handleLoginSuccess = (role) => {
  // 保持兼容旧版 emit 发出的小写 role，真实 role 存在 localStorage
  userRole.value = role; 
  if (role === 'shop') {
    currentView.value = 'registration-form';
  }
  fetchRegistrations();
};

const handleLogout = () => {
  userRole.value = null;
  localStorage.removeItem('token');
  localStorage.removeItem('role');
  localStorage.removeItem('shopCode');
  localStorage.removeItem('shopName');
  localStorage.removeItem('userType');
  localStorage.removeItem('displayName');
  localStorage.removeItem('permissionCodes');
  localStorage.removeItem('roleCodes');
  ElMessage.info('已安全退出登录');
};

onMounted(() => {
  fetchConfig();
  // 如果刷新时已经有登录状态，则读取数据
  const role = localStorage.getItem('role');
  if (role) {
    userRole.value = role === 'ADMIN' ? 'admin' : 'shop';
    fetchRegistrations();
  }
});
</script>
