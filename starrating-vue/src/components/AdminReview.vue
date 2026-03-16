<template>
  <div class="admin-review">
    <div class="page-header">
      <div>
        <h2 class="page-title">后台人工审核面板</h2>
        <p class="page-desc">对服务店提交的报名信息进行线上审核，需仔细核对凭证与照片资质。</p>
      </div>
    </div>

    <el-card class="filter-card" shadow="never" style="margin-bottom: 16px;">
      <el-form :inline="true" :model="filters" class="demo-form-inline">
        <el-form-item label="服务店名称">
          <el-input v-model="filters.shopName" placeholder="输入名称搜索" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item label="申报星级">
          <el-select v-model="filters.targetStar" placeholder="全部" clearable style="width: 120px">
            <el-option label="三星级" value="三星级" />
            <el-option label="四星级" value="四星级" />
            <el-option label="五星级" value="五星级" />
          </el-select>
        </el-form-item>
        <el-form-item label="审核状态">
          <el-select v-model="filters.status" placeholder="全部" clearable style="width: 120px">
            <el-option label="待审核" value="待审核" />
            <el-option label="已通过" value="已通过" />
            <el-option label="已退回" value="已退回" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleResetFilters" plain>重置筛选</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="page-card" shadow="never">
      <el-table :data="filteredList" style="width: 100%" v-loading="loading">
        <el-table-column prop="shopCode" label="服务店代码" width="120" fixed></el-table-column>
        <el-table-column prop="shopName" label="服务店名称" min-width="180"></el-table-column>
        <el-table-column prop="targetStar" label="申报星级" width="120">
          <template #default="scope">
            <el-tag :type="scope.row.targetStar === '五星级' ? 'warning' : 'info'">{{ scope.row.targetStar }}</el-tag>
          </template>
        </el-table-column>
        
        <el-table-column label="上传资料" width="200">
          <template #default="scope">
            <div style="font-size:12px; line-height:1.4;">
              <div>缴费凭证: <span style="color:var(--primary-color)">{{ scope.row.paymentUrl || '无' }}</span></div>
              <div v-if="scope.row.canopyUrl">雨棚照片: <span style="color:var(--primary-color)">{{ scope.row.canopyUrl }}</span></div>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="createdAt" label="提交时间" width="160"></el-table-column>

        <el-table-column prop="status" label="审核状态" width="120">
          <template #default="scope">
            <span :class="['status-tag', getStatusClass(scope.row.status)]" style="padding:4px 8px; border-radius:4px; font-size:12px; border:1px solid;">
              {{ scope.row.status }}
            </span>
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="scope">
            <template v-if="scope.row.status === '待审核'">
              <el-button size="small" type="success" @click="handleApproveClick(scope.row)">通过</el-button>
              <el-button size="small" type="danger" @click="openRejectDialog(scope.row)">退回</el-button>
            </template>
            <template v-else-if="scope.row.status === '已退回'">
              <el-tooltip :content="scope.row.rejectReason" placement="top-start" effect="dark">
                <span style="color: var(--text-secondary); font-size:13px; cursor:help;">退回原因</span>
              </el-tooltip>
            </template>
            <template v-else>
              <span style="color: var(--success-color); font-size:13px;">包含于通过名单</span>
            </template>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 退回弹窗 -->
    <el-dialog v-model="dialogVisible" title="填写退回原因" width="30%" destroy-on-close>
      <p style="margin-bottom: 12px; color: var(--text-regular);">正在退回服务店：<strong>{{ currentShopName }}</strong></p>
      <el-input
        v-model="rejectReasonText"
        type="textarea"
        :rows="3"
        placeholder="请输入不符合原因 (重点必填，且必须20个字以内)"
        maxlength="20"
        show-word-limit
      />
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消操作</el-button>
          <el-button type="danger" @click="confirmReject" :disabled="!rejectReasonText.trim()">
            确认退回
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';

const props = defineProps({
  registrations: Array
});

const emit = defineEmits(['approve', 'reject']);

const filters = ref({
  shopName: '',
  targetStar: '',
  status: ''
});

const loading = ref(false);
const dialogVisible = ref(false);
const currentRejectId = ref(null);
const currentShopName = ref('');
const rejectReasonText = ref('');

const filteredList = computed(() => {
  return props.registrations.filter(r => {
    const matchName = !filters.value.shopName || r.shopName.includes(filters.value.shopName);
    const matchStar = !filters.value.targetStar || r.targetStar === filters.value.targetStar;
    const matchStatus = !filters.value.status || r.status === filters.value.status;
    return matchName && matchStar && matchStatus;
  });
});

const handleResetFilters = () => {
  filters.value = {
    shopName: '',
    targetStar: '',
    status: ''
  };
};

const getStatusClass = (status) => {
  if (status === '待审核') return 'pending';
  if (status === '已通过') return 'approved';
  if (status === '已退回') return 'rejected';
  return '';
};

const handleApproveClick = (row) => {
  loading.value = true;
  setTimeout(() => {
    loading.value = false;
    emit('approve', row.id);
  }, 400); // 模拟网络延迟
};

const openRejectDialog = (row) => {
  currentRejectId.value = row.id;
  currentShopName.value = row.shopName;
  rejectReasonText.value = '';
  dialogVisible.value = true;
};

const confirmReject = () => {
  emit('reject', { id: currentRejectId.value, reason: rejectReasonText.value });
  dialogVisible.value = false;
};
</script>
