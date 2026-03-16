<template>
  <el-container class="admin-layout">
    <!-- 侧边栏 -->
    <el-aside width="240px" class="admin-aside">
      <div class="aside-logo">
        <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" class="logo-icon-small">
          <path
            d="M12 2L15.09 8.26L22 9.27L17 14.14L18.18 21.02L12 17.77L5.82 21.02L7 14.14L2 9.27L8.91 8.26L12 2Z"
            fill="currentColor" />
        </svg>
        <span style="font-weight: 600; font-size: 16px; margin-left:8px; color:#fff;">厂端管理中控区</span>
      </div>

      <el-menu
        :default-active="activeMenu"
        class="admin-menu"
        background-color="#1e293b"
        text-color="#94a3b8"
        active-text-color="#ffffff"
        @select="handleSelect"
      >
        <el-sub-menu index="1">
          <template #title>
            <el-icon><Setting /></el-icon>
            <span>全局配置管理</span>
          </template>
          <el-menu-item index="1-1" disabled>基础权限与账号配置</el-menu-item>
          <el-menu-item index="1-2" disabled>评定周期与标准导入</el-menu-item>
          <el-menu-item index="1-3" disabled>评定标准智能体配置</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="2">
          <template #title>
            <el-icon><Document /></el-icon>
            <span style="color:#ffffff;">申报管理</span>
          </template>
          <el-menu-item index="2-1">服务店申报管理</el-menu-item>
          <el-menu-item index="2-2" disabled>飞检名单确认及修改</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="3">
          <template #title>
            <el-icon><User /></el-icon>
            <span>资源与培训管理</span>
          </template>
          <el-menu-item index="3-1" disabled>人员培训管理</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="4">
          <template #title>
            <el-icon><Monitor /></el-icon>
            <span>设备管理</span>
          </template>
          <el-menu-item index="4-1" disabled>评定设备管理</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="5">
          <template #title>
            <el-icon><Position /></el-icon>
            <span>作业指派中心</span>
          </template>
          <el-menu-item index="5-1" disabled>分组与行程输出</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="6">
          <template #title>
            <el-icon><ChatLineSquare /></el-icon>
            <span>审核与评审中心</span>
          </template>
          <el-menu-item index="6-1" disabled>异议评审管理</el-menu-item>
          <el-menu-item index="6-2" disabled>整改报告审核</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="7">
          <template #title>
            <el-icon><DataBoard /></el-icon>
            <span>公示与通报中心</span>
          </template>
          <el-menu-item index="7-1" disabled>非现场数据汇总</el-menu-item>
          <el-menu-item index="7-2" disabled>结果公示推送</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="8">
          <template #title>
            <el-icon><PieChart /></el-icon>
            <span>数据中心</span>
          </template>
          <el-menu-item index="8-1" disabled>星评进度看板</el-menu-item>
          <el-menu-item index="8-2" disabled>星评结果看板</el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-aside>

    <!-- 主体区域 -->
    <el-container class="admin-main-wrapper">
      <!-- 头部栏 -->
      <el-header class="admin-topbar">
        <div class="topbar-left">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item>厂端后台</el-breadcrumb-item>
            <el-breadcrumb-item>{{ currentBreadcrumb }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="topbar-right">
          <div style="font-size: 14px; margin-right: 24px; display: flex; align-items: center;">
            <span style="color:var(--text-secondary); margin-right: 12px; white-space: nowrap;">报名申请周期设置</span>
            <el-date-picker
              v-model="registrationPeriod"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              size="small"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
              @change="handlePeriodChange"
              style="width: 260px;"
            />
          </div>
          <el-button type="danger" plain size="small" @click="handleLogout">退出登录</el-button>
        </div>
      </el-header>

      <!-- 核心内容渲染区 -->
      <el-main class="admin-main-content">
        <div v-if="activeMenu === '2-1'" class="content-wrapper">
          <AdminReview 
            :registrations="registrations" 
            @approve="val => emit('approve', val)" 
            @reject="val => emit('reject', val)" 
          />
        </div>

        <div v-else class="placeholder-wrapper">
          <el-empty description="该模块内容正在开发建设中..."></el-empty>
        </div>
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped>
.admin-layout {
  height: 100vh;
  width: 100%;
}

.admin-aside {
  background-color: #1e293b;
  box-shadow: 2px 0 8px rgba(0,0,0,0.15);
  display: flex;
  flex-direction: column;
  z-index: 10;
}

.aside-logo {
  height: 64px;
  display: flex;
  align-items: center;
  padding: 0 20px;
  background-color: #0f172a;
  border-bottom: 1px solid #334155;
}
.logo-icon-small {
  width: 24px;
  height: 24px;
  color: #38bdf8;
}

.admin-menu {
  border-right: none;
  flex: 1;
  overflow-y: auto;
}

/* 覆盖 el-menu 的高光样式 */
:deep(.el-menu-item.is-active) {
  background-color: #38bdf8 !important;
  color: #fff !important;
}

.admin-main-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
  background-color: #f1f5f9;
}

.admin-topbar {
  background-color: #ffffff;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.05);
  z-index: 5;
}

.topbar-left, .topbar-right {
  display: flex;
  align-items: center;
}

.admin-main-content {
  padding: 24px;
  overflow-y: auto;
}

.content-wrapper {
  background: transparent;
  width: 100%;
  height: 100%;
}
.placeholder-wrapper {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #ffffff;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.05);
}

/* 覆盖 tab 样式，使其更贴合后台感 */
:deep(.el-tabs--border-card) {
  border-radius: 8px;
  overflow: hidden;
  border: none;
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
}
</style>

<script setup>
import { ref, computed } from 'vue';
import { 
  Setting, Document, User, Monitor, Position, ChatLineSquare, DataBoard, PieChart
} from '@element-plus/icons-vue';
import AdminReview from './AdminReview.vue';

const props = defineProps({
  registrations: Array
});

const emit = defineEmits([
  'update-period',
  'approve', 
  'reject', 
  'logout'
]);

const activeMenu = ref('2-1');
const registrationPeriod = ref([]); // [startTime, endTime]

const handlePeriodChange = (val) => {
  if (val) {
    emit('update-period', { startTime: val[0], endTime: val[1] });
  }
};

const handleSelect = (key) => {
  activeMenu.value = key;
};

const currentBreadcrumb = computed(() => {
  if (activeMenu.value === '2-1') return '申报管理 / 服务店申报管理';
  return '功能建设中';
});

const handleLogout = () => {
  emit('logout');
};
</script>
