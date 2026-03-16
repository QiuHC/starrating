(() => {
  const { ref, computed } = Vue;

  const template = `
    <div class="admin-export">
      <div class="page-header" style="display:flex; justify-content:space-between; align-items:flex-end;">
        <div>
          <h2 class="page-title">名单生成与导出</h2>
          <p class="page-desc">基于历史表与全量明细表自动筛选强制抽检名单，并与主动报名“已通过”名单进行合并输出最终结果。</p>
        </div>
        <el-alert
          v-if="!registrationOpen"
          title="报名通道已关闭，可通过分析生成最终名单。"
          type="success"
          show-icon
          :closable="false"
          style="width: 320px;"
        ></el-alert>
        <el-alert
          v-else
          title="报名尚未截止，结果仅供预演查看。"
          type="warning"
          show-icon
          :closable="false"
          style="width: 300px;"
        ></el-alert>
      </div>

      <el-row :gutter="24" style="margin-bottom: 24px;">
        <el-col :span="8">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-title">已通过主动报名服务店</div>
            <div class="stat-value" style="color:var(--success-color)">{{ approvedCount }}</div>
            <div class="stat-desc">完成本次评定流程并合规的单位</div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-title">历史遗留(抽检)分析数</div>
            <div class="stat-value" style="color:var(--warning-color)">{{ spotCheckCount }}</div>
            <div class="stat-desc">成立超半年且连续2年非星级被强制抽起</div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card shadow="hover" class="stat-card stat-combined">
            <div class="stat-title">本次最终检查名单总数</div>
            <div class="stat-value" style="color:var(--primary-color)">{{ totalCount }}</div>
            <div class="stat-desc">去重合并后的必须检查名单集合</div>
          </el-card>
        </el-col>
      </el-row>

      <el-card class="page-card" shadow="never">
        <template #header>
          <div style="display:flex; justify-content:space-between; align-items:center;">
            <span style="font-weight:600; font-size:16px;">模拟数据流转与名单聚合区</span>
            <el-button type="primary" size="large" @click="handleRunMerge" :loading="isProcessing">
              运行合并与生成正式名单
            </el-button>
          </div>
        </template>
        
        <el-tabs v-model="activeTab" class="demo-tabs">
          <el-tab-pane label="初筛生成最终抽检列表 (系统规则运行)" name="spotCheck">
            <p style="font-size:13px; color:var(--text-secondary); margin-bottom:12px;">此列表是依托全局库「服务店最新明细」与「历史结果表」模拟抓取的数据对象：需要满足成立达183天，且历史两次非星级。</p>
            <el-table :data="mockSpotCheckList" style="width: 100%" size="small" border>
              <el-table-column prop="shopCode" label="服务店代码" width="120"></el-table-column>
              <el-table-column prop="shopName" label="服务店名称"></el-table-column>
              <el-table-column prop="days" label="成立天数" width="100">
                <template #default="scope">
                  <span :style="{ color: scope.row.days >= 183 ? 'var(--success-color)' : 'inherit' }">{{ scope.row.days }}天</span>
                </template>
              </el-table-column>
              <el-table-column prop="historyResult" label="连续两期历史情况" width="180">
                <template #default="scope">
                  <span style="color:var(--danger-color)">{{ scope.row.historyResult }}</span>
                </template>
              </el-table-column>
              <el-table-column label="操作分类" width="120">
                <template #default>
                  <el-tag type="warning" size="small">强制抽检补增</el-tag>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>

          <el-tab-pane label="最终合并数据视图预览" name="finalView">
            <el-table :data="finalCombinedList" style="width: 100%" size="small" border v-loading="isProcessing">
              <el-table-column type="index" width="50"></el-table-column>
              <el-table-column prop="shopCode" label="核心唯一ID(shop_code)" width="180"></el-table-column>
              <el-table-column prop="shopName" label="服务店名称"></el-table-column>
              <el-table-column prop="source" label="名单来源分类" width="150">
                <template #default="scope">
                  <el-tag :type="scope.row.source === '主动报名' ? 'success' : 'warning'" disable-transitions>
                    {{ scope.row.source }}
                  </el-tag>
                </template>
              </el-table-column>
            </el-table>
            <div style="margin-top: 16px; text-align:right;">
             <el-button type="success" @click="handleExportCSV"> 将最终名单导出为 CSV </el-button>
            </div>
          </el-tab-pane>
        </el-tabs>

      </el-card>
    </div>
  `;

  window.AdminExport = {
    template,
    props: {
      registrationOpen: Boolean,
      registrations: Array
    },
    emits: ['generate-list'],
    setup(props, { emit }) {
      const activeTab = ref('finalView');
      const isProcessing = ref(false);

      // 模拟基于历史与明细数据生成的抽检数据源
      const mockSpotCheckList = ref([
        { shopCode: 'GD003', shopName: '广东测试抽检门店', days: 200, historyResult: '22年非星级, 23年非星级', source: '规则抽检' },
        { shopCode: 'SZ009', shopName: '深圳遗留服务站', days: 500, historyResult: '22年退回, 23年不合规', source: '规则抽检' }
      ]);

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
        // 合并并基于 shopCode 去重
        const map = new Map();

        // 先推入报名成功的数据
        approvedList.value.forEach(item => {
          map.set(item.shopCode, item);
        });
        // 再推入强制抽检的数据，如果主动报名里已经有了且通过了，应该以主动报名为准？
        // 根据企业逻辑，不一定。此处示例只做直接追加并标记。
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
        setTimeout(() => {
          isProcessing.value = false;
          activeTab.value = 'finalView';
          emit('generate-list');
        }, 1000);
      };

      const handleExportCSV = () => {
        if (finalCombinedList.value.length === 0) {
          ElementPlus.ElMessage.warning('当前没有任何数据可以导出');
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

      return {
        activeTab,
        isProcessing,
        mockSpotCheckList,
        approvedCount,
        spotCheckCount,
        totalCount,
        finalCombinedList,
        handleRunMerge,
        handleExportCSV
      };
    }
  };
})();
