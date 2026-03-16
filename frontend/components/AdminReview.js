(() => {
  const { ref, computed } = Vue;

  const template = `
    <div class="admin-review">
      <div class="page-header" style="display:flex; justify-content:space-between; align-items:center;">
        <div>
          <h2 class="page-title">后台人工审核面板</h2>
          <p class="page-desc">对服务店提交的报名信息进行线上审核，需仔细核对凭证与照片资质。</p>
        </div>
        <el-radio-group v-model="filterStatus" size="default">
          <el-radio-button label="全部"></el-radio-button>
          <el-radio-button label="待审核"></el-radio-button>
          <el-radio-button label="已通过"></el-radio-button>
          <el-radio-button label="已退回"></el-radio-button>
        </el-radio-group>
      </div>

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
  `;

  window.AdminReview = {
    template,
    props: {
      registrations: Array
    },
    emits: ['approve', 'reject'],
    setup(props, { emit }) {
      const filterStatus = ref('全部');
      const loading = ref(false);

      const dialogVisible = ref(false);
      const rejectReasonText = ref('');
      const currentRejectId = ref(null);
      const currentShopName = ref('');

      const filteredList = computed(() => {
        let list = [...props.registrations];
        // 按时间倒序排序
        list.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));

        if (filterStatus.value !== '全部') {
          return list.filter(item => item.status === filterStatus.value);
        }
        return list;
      });

      const getStatusClass = (status) => {
        switch (status) {
          case '待审核': return 'pending';
          case '已通过': return 'approved';
          case '已退回': return 'rejected';
          default: return '';
        }
      };

      const handleApproveClick = (row) => {
        ElementPlus.ElMessageBox.confirm(
          `确认审核通过服务店 ${row.shopName} 的资质吗？`,
          '提示',
          {
            confirmButtonText: '确定通过',
            cancelButtonText: '取消',
            type: 'success',
          }
        ).then(() => {
          emit('approve', row.id);
          ElementPlus.ElMessage({ type: 'success', message: '已审核通过' });
        }).catch(() => { });
      };

      const openRejectDialog = (row) => {
        currentRejectId.value = row.id;
        currentShopName.value = row.shopName;
        rejectReasonText.value = '';
        dialogVisible.value = true;
      };

      const confirmReject = () => {
        if (rejectReasonText.value.trim().length > 20) {
          ElementPlus.ElMessage.error('退回原因不能超过20个字！');
          return;
        }
        emit('reject', { id: currentRejectId.value, reason: rejectReasonText.value.trim() });
        dialogVisible.value = false;
        ElementPlus.ElMessage({ type: 'warning', message: '已退回并记录原因' });
      };

      return {
        filterStatus,
        loading,
        filteredList,
        dialogVisible,
        rejectReasonText,
        currentShopName,
        getStatusClass,
        handleApproveClick,
        openRejectDialog,
        confirmReject
      };
    }
  };
})();
