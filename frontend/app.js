(() => {
  const { createApp, ref, computed, onMounted } = Vue;

  const app = createApp({
    setup() {
      // 后台导航配置
      const adminNavItems = [
        {
          key: 'admin-review',
          label: '后台审核',
          icon: '<svg viewBox="0 0 24 24"><path d="M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm-9 14l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z" fill="currentColor"/></svg>'
        },
        {
          key: 'admin-export',
          label: '名单输出',
          icon: '<svg viewBox="0 0 24 24"><path d="M19 9h-4V3H9v6H5l7 7 7-7zM5 18v2h14v-2H5z" fill="currentColor"/></svg>'
        }
      ];

      // 当前视图状态
      const currentView = ref('registration-form');
      // 是否为管理员模式
      const isAdminMode = ref(false);

      // 全局报名状态控制
      const registrationOpen = ref(true);

      // 模拟的数据库 - 报名列表
      // 实际项目中应由后端接口提供
      const registrations = ref([
        {
          id: 1,
          shopCode: 'SH001',
          shopName: '上海测试服务店一',
          targetStar: '三星级',
          paymentUrl: 'payment_fake_url_1.png',
          canopyUrl: '',
          status: '待审核',
          rejectReason: '',
          createdAt: '2026-02-24 10:00:00'
        },
        {
          id: 2,
          shopCode: 'BJ002',
          shopName: '北京测试服务店二',
          targetStar: '五星级',
          paymentUrl: 'payment_fake_url_2.png',
          canopyUrl: 'canopy_fake_url_2.png',
          status: '已退回',
          rejectReason: '缴费凭证模糊不清',
          createdAt: '2026-02-23 14:30:00'
        }
      ]);

      // 当前组件基于 currentView 动态计算，返回已注册的组件名称字符串
      const currentComponent = computed(() => {
        if (currentView.value === 'registration-form') return 'RegistrationForm';
        if (currentView.value === 'admin-review') return 'AdminReview';
        if (currentView.value === 'admin-export') return 'AdminExport';
        return 'RegistrationForm';
      });

      const switchView = (key) => {
        currentView.value = key;
      };

      const onToggleRegistration = (val) => {
        if (!val) {
          // 如果关闭报名，给一个提示
          ElementPlus.ElMessage({
            message: '报名已关闭！当前名单已被锁定。',
            type: 'warning'
          });
        } else {
          ElementPlus.ElMessage({
            message: '报名已开启！服务店可以继续提交与修改审核。',
            type: 'success'
          });
        }
      };

      // 接收子组件的事件并修改模拟数据
      const handleSubmitRegistration = (formData) => {
        const existingIndex = registrations.value.findIndex(r => r.shopCode === formData.shopCode);
        const now = new Date().toLocaleString();

        if (existingIndex !== -1) {
          // 更新现有记录（针对退回重提）
          registrations.value[existingIndex] = {
            ...registrations.value[existingIndex],
            ...formData,
            status: '待审核',
            rejectReason: '',
            updatedAt: now
          };
          ElementPlus.ElMessage.success('您的报名信息已重新提交审核！');
        } else {
          // 新增记录
          registrations.value.push({
            id: Date.now(),
            ...formData,
            status: '待审核',
            rejectReason: '',
            createdAt: now
          });
          ElementPlus.ElMessage.success('提交报名成功，等待后台审核！');
        }

        // 可以选择跳转回列表或者清空表单（表单内部处理了）
      };

      const handleApprove = (id) => {
        const item = registrations.value.find(r => r.id === id);
        if (item) {
          item.status = '已通过';
          item.rejectReason = '';
        }
      };

      const handleReject = ({ id, reason }) => {
        const item = registrations.value.find(r => r.id === id);
        if (item) {
          item.status = '已退回';
          item.rejectReason = reason;
        }
      };

      const handleGenerateList = () => {
        // 此事件仅用于展示通知
        ElementPlus.ElMessage.success('合并导出任务已开始，请稍候下载导出文件...');
      };

      const toggleSystemMode = () => {
        isAdminMode.value = !isAdminMode.value;
        if (isAdminMode.value) {
          currentView.value = 'admin-review';
          ElementPlus.ElMessage.success('已进入后台管理系统');
        } else {
          currentView.value = 'registration-form';
          ElementPlus.ElMessage.info('已返回服务店提报页');
        }
      };

      return {
        adminNavItems,
        currentView,
        currentComponent,
        registrationOpen,
        registrations,
        isAdminMode,
        switchView,
        onToggleRegistration,
        handleSubmitRegistration,
        handleApprove,
        handleReject,
        handleGenerateList,
        toggleSystemMode
      };
    }
  });

  // 使用 Element Plus
  app.use(ElementPlus, {
    locale: ElementPlusLocaleZhCn,
  });

  // 注册全局组件，解决 CDN 引入时 component is 无法正确解析对象引用的问题
  app.component('RegistrationForm', window.RegistrationForm);
  app.component('AdminReview', window.AdminReview);
  app.component('AdminExport', window.AdminExport);


  app.mount('#app');
})();
