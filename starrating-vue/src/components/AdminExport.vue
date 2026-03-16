<template>
  <div class="admin-export">
    <div class="page-header" style="display:flex; justify-content:space-between; align-items:flex-end;">
      <div>
        <h2 class="page-title">名单生成与导出</h2>
        <p class="page-desc">请先分别导入【历史抽检数据】与【最新服务店明细】，满足两项前提后即可生成综合初筛列表。</p>
      </div>
      <el-alert
        v-if="!registrationOpen"
        title="在线报名通道已关闭，您可以进行数据合并与导出。"
        type="success"
        show-icon
        :closable="false"
        style="width: 320px;"
      ></el-alert>
      <el-alert
        v-else
        title="报名尚未截止，导出的结果可能不是最终态。"
        type="warning"
        show-icon
        :closable="false"
        style="width: 300px;"
      ></el-alert>
    </div>

    <!-- 数据源上传面板区 -->
    <el-card shadow="never" class="page-card" style="margin-bottom: 24px;">
      <template #header>
        <span style="font-weight:600; font-size:16px;">第一步：准备数据源</span>
      </template>

      <el-row :gutter="48">
        <el-col :span="12">
          <div class="upload-zone">
            <h3>1. 季度历史数据</h3>
            <p>上传后将用于分析服务店既往成绩。(每个季度仅允许上传一次且不可修改)</p>
            <div style="margin-top: 16px;">
              <el-button 
                :type="isHistoryUploaded ? 'success' : 'primary'" 
                :disabled="isHistoryUploaded" 
                @click="handleUploadHistory"
                style="width: 100%;"
              >
                {{ isHistoryUploaded ? '✓ 历史数据已就绪 (已锁定)' : '点击上传历史数据表格' }}
              </el-button>
            </div>
          </div>
        </el-col>
        <el-col :span="12">
          <div class="upload-zone">
            <h3>2. 最新服务店明细名单</h3>
            <p>全量最新架构下的服务店名单库。(可根据架构变动随时重新上传覆盖)</p>
            <div style="margin-top: 16px;">
              <el-button 
                :type="isLatestListUploaded ? 'success' : 'primary'" 
                plain
                @click="handleUploadLatest"
                style="width: 100%;"
              >
                {{ isLatestListUploaded ? '↺ 重新上传最新明细名单' : '点击上传最新服务店明细' }}
              </el-button>
              <div v-if="isLatestListUploaded" style="text-align:center;color:var(--success-color);margin-top:8px;font-size:12px;">✅ 当前明细已准备</div>
            </div>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <el-card class="page-card" shadow="never">
      <template #header>
        <div style="display:flex; justify-content:space-between; align-items:center;">
          <span style="font-weight:600; font-size:16px;">第二步：生成抽取结果</span>
          <el-button 
            type="primary" 
            size="large" 
            @click="handleRunMerge" 
            :loading="isProcessing"
            :disabled="!canGenerate"
          >
            运算合并与生成正式名单
          </el-button>
        </div>
      </template>

      <div v-if="!hasGenerated" style="text-align:center; padding: 64px 0; color: var(--text-secondary);">
        请先在第一步中完成两份必备数据源的上传。
      </div>
      
      <div v-else>
        <el-row :gutter="24" style="margin-bottom: 24px;">
          <el-col :span="8">
            <el-card shadow="hover" class="stat-card">
              <div class="stat-title">本次通过的报名表</div>
              <div class="stat-value" style="color:var(--success-color)">{{ approvedCount }}</div>
              <div class="stat-desc">完成本期主动评定流程并合规</div>
            </el-card>
          </el-col>
          <el-col :span="8">
            <el-card shadow="hover" class="stat-card">
              <div class="stat-title">历史遗留(抽检)分析数</div>
              <div class="stat-value" style="color:var(--warning-color)">{{ spotCheckCount }}</div>
              <div class="stat-desc">由双表联算查出的需强制抽检数</div>
            </el-card>
          </el-col>
          <el-col :span="8">
            <el-card shadow="hover" class="stat-card stat-combined">
              <div class="stat-title">最终检查名单集合</div>
              <div class="stat-value" style="color:var(--primary-color)">{{ totalCount }}</div>
              <div class="stat-desc">双边名单去除重名之后的聚合总数</div>
            </el-card>
          </el-col>
        </el-row>

        <el-tabs v-model="activeTab" class="demo-tabs">
          <el-tab-pane label="初筛生成列表 (历史数据分析)" name="spotCheck">
            <p style="font-size:13px; color:var(--text-secondary); margin-bottom:12px;">此列表是依托全局库「服务店最新明细」与「历史结果表」模拟抓取的数据对象：需要满足成立达183天，且历史两次非星级。</p>
            <el-table :data="mockSpotCheckList" style="width: 100%" size="small" border>
              <el-table-column prop="shopCode" label="服务店代码" width="120"></el-table-column>
              <el-table-column prop="shopName" label="服务店名称"></el-table-column>
              <el-table-column prop="days" label="成立天数" width="100">
                <template #default="scope">
                  <span :style="{ color: scope.row.days >= 183 ? 'var(--success-color)' : 'inherit' }">{{ scope.row.days }}天</span>
                </template>
              </el-table-column>
              <el-table-column prop="historyResult" label="极期历史成绩" width="180">
                <template #default="scope">
                  <span style="color:var(--danger-color)">{{ scope.row.historyResult }}</span>
                </template>
              </el-table-column>
              <el-table-column label="动作" width="120">
                <template #default>
                  <el-tag type="warning" size="small">强制兜底抽检</el-tag>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>

          <el-tab-pane label="最终合并数据视图" name="finalView">
            <el-table :data="finalCombinedList" style="width: 100%" size="small" border v-loading="isProcessing">
              <el-table-column type="index" width="50"></el-table-column>
              <el-table-column prop="shopCode" label="核心唯一ID(shop_code)" width="180"></el-table-column>
              <el-table-column prop="shopName" label="服务店名称"></el-table-column>
              <el-table-column prop="source" label="名单来源归属" width="150">
                <template #default="scope">
                  <el-tag :type="scope.row.source === '主动报名' ? 'success' : 'warning'" disable-transitions>
                    {{ scope.row.source }}
                  </el-tag>
                </template>
              </el-table-column>
            </el-table>
            <div style="margin-top: 16px; text-align:right;">
             <el-button type="success" @click="handleExportCSV"> 下载最新数据报表 (CSV) </el-button>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.upload-zone {
  background: var(--bg-color);
  padding: 24px;
  border-radius: var(--border-radius);
  border: 1px dashed var(--border-color);
  height: 100%;
}
.upload-zone h3 {
  font-size: 16px;
  margin-bottom: 8px;
  color: var(--text-primary);
}
.upload-zone p {
  font-size: 13px;
  color: var(--text-secondary);
  line-height: 1.5;
}
</style>

<script setup>
import { ref, computed } from 'vue';
import { ElMessage, ElLoading } from 'element-plus';

const props = defineProps({
  registrationOpen: Boolean,
  registrations: Array
});

const emit = defineEmits(['generate-list']);

// 业务流程状态控制
const isHistoryUploaded = ref(false);
const isLatestListUploaded = ref(false);
const hasGenerated = ref(false);

const activeTab = ref('spotCheck');
const isProcessing = ref(false);

const canGenerate = computed(() => {
  return isHistoryUploaded.value && isLatestListUploaded.value;
});

// Demo 上传交互
const handleUploadHistory = () => {
  const loading = ElLoading.service({
    lock: true,
    text: '正在解析历史数据...',
    background: 'rgba(255, 255, 255, 0.7)',
  })
  setTimeout(() => {
    isHistoryUploaded.value = true;
    loading.close();
    ElMessage.success('历史数据已导入锁定。');
  }, 800);
}

const handleUploadLatest = () => {
  const loading = ElLoading.service({
    lock: true,
    text: '正在更新服务店明细...',
    background: 'rgba(255, 255, 255, 0.7)',
  })
  setTimeout(() => {
    isLatestListUploaded.value = true;
    hasGenerated.value = false; // 如果重新上传过最新名单，可以强制取消生成结果的展示，需要重新运算
    loading.close();
    ElMessage.success('最新名单已更新成功！');
  }, 600);
}

// 模拟基于历史与明细数据生成的抽检数据源
const mockSpotCheckList = ref([]);

// 计算统计指标
const approvedList = computed(() => {
  return props.registrations.filter(r => r.status === '已通过').map(item => ({
    shopCode: item.shopCode,
    shopName: item.shopName,
    source: '主动报名'
  }));
});

const approvedCount = computed(() => approvedList.value.length);
const spotCheckCount = computed(() => mockSpotCheckList.value.length);

const finalCombinedList = computed(() => {
  if (!hasGenerated.value) return [];
  // 合并并基于 shopCode 去重
  const map = new Map();

  // 先推入报名成功的数据
  approvedList.value.forEach(item => {
    map.set(item.shopCode, item);
  });
  // 再推入强制抽检的数据
  mockSpotCheckList.value.forEach(item => {
    if (!map.has(item.shopCode)) {
      map.set(item.shopCode, {
        shopCode: item.shopCode,
        shopName: item.shopName,
        source: '规则抽检'
      });
    }
  });
  return Array.from(map.values());
});

const totalCount = computed(() => finalCombinedList.value.length);

const handleRunMerge = () => {
  isProcessing.value = true;
  hasGenerated.value = true;
  activeTab.value = 'spotCheck';
  
  setTimeout(() => {
    // 放入假数据展示
    mockSpotCheckList.value = [
      { shopCode: 'GD003', shopName: '广东测试抽检门店', days: 200, historyResult: '22年非星级, 23年非星级', source: '规则抽检' },
      { shopCode: 'SZ009', shopName: '深圳遗留服务站', days: 500, historyResult: '22年退回, 23年不合规', source: '规则抽检' }
    ];
    isProcessing.value = false;
    emit('generate-list');
  }, 1200);
};

const handleExportCSV = () => {
  if (finalCombinedList.value.length === 0) {
    ElMessage.warning('当前没有任何数据可以导出');
    return;
  }
  // 简单模拟 CSV
  let csvContent = "data:text/csv;charset=utf-8,\uFEFF";
  csvContent += "门店标识码,主体名称,进入检查来源类别\n";
  finalCombinedList.value.forEach(row => {
    csvContent += `${row.shopCode},${row.shopName},${row.source}\n`;
  });

  const encodedUri = encodeURI(csvContent);
  const link = document.createElement("a");
  link.setAttribute("href", encodedUri);
  link.setAttribute("download", "service_shop_final_inspection_list.csv");
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
};
</script>
