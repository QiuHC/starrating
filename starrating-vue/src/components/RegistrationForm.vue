<template>
  <div class="registration-form">
    <div class="page-header">
      <h2 class="page-title">服务店星级评定报名</h2>
      <p class="page-desc">请认真填写各项内容并上传相关凭证，提交后将由后台进行资质审核。</p>
    </div>

    <div v-if="!registrationOpen" class="el-alert el-alert--error is-light" style="margin-bottom: 24px;">
      <i class="el-alert__icon el-icon-warning"></i>
      <div class="el-alert__content">
        <span class="el-alert__title">当前不在报名开放期间</span>
        <p class="el-alert__description">系统已关闭本次报名通道，确定了最终审查名单，暂不再接受新的提交或修改。</p>
      </div>
    </div>

    <el-card class="page-card" shadow="never">
      <!-- 退回提示 -->
      <el-alert
        v-if="existingRegistration && existingRegistration.status === '已退回'"
        title="您的报名信息已被退回！请修改后重新提交"
        type="error"
        show-icon
        :closable="false"
        style="margin-bottom: 24px;"
      >
        <template #default>
          <div><strong>退回原因：</strong>{{ existingRegistration.rejectReason }}</div>
        </template>
      </el-alert>
      
      <el-alert
        v-if="existingRegistration && existingRegistration.status === '待审核'"
        title="您的报名信息正在审核中，请耐心等待..."
        type="info"
        show-icon
        :closable="false"
        style="margin-bottom: 24px;"
      >
      </el-alert>

      <el-alert
        v-if="existingRegistration && existingRegistration.status === '已通过'"
        title="恭喜，您的报名资质已审核通过！已纳入最终审查名单。"
        type="success"
        show-icon
        :closable="false"
        style="margin-bottom: 24px;"
      >
      </el-alert>

      <el-form 
        ref="formRef" 
        :model="formData" 
        :rules="rules" 
        label-position="top" 
        :disabled="!registrationOpen || (existingRegistration && existingRegistration.status === '待审核') || (existingRegistration && existingRegistration.status === '已通过')"
      >
        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item label="服务店代码 (Shop Code)" prop="shopCode">
              <el-input 
                v-model="formData.shopCode" 
                :placeholder="isShopAccount ? '当前账号已绑定服务店代码' : '请输入您的服务店唯一代码'"
                clearable
                :disabled="isShopAccount"
                @blur="checkExistingRegistration"
              >
              </el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="服务店名称 (Shop Name)" prop="shopName">
              <el-input v-model="formData.shopName" placeholder="例如：上海宏桥第一服务中心" clearable></el-input>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item label="申报星级 (Target Star)" prop="targetStar">
              <el-select v-model="formData.targetStar" placeholder="请选择申报星级" style="width: 100%;">
                <el-option label="三星级" value="三星级"></el-option>
                <el-option label="四星级" value="四星级"></el-option>
                <el-option label="五星级" value="五星级"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="缴费扣费凭证 (Payment Voucher)" prop="paymentUrl">
          <el-upload
            class="upload-demo"
            drag
            action="#"
            :auto-upload="false"
            :on-change="(file) => handleFileChange('payment', file)"
            :show-file-list="false"
          >
            <div v-if="formData.paymentUrl" class="upload-preview">
              <span style="color:var(--success-color); font-weight:600;">文件已准备：{{ formData.paymentUrl }}</span>
              <p style="font-size:12px;color:#999;">点击重新上传</p>
            </div>
            <div v-else>
              <div class="el-upload__text">将文件拖到此处，或 <em>点击上传</em></div>
            </div>
          </el-upload>
        </el-form-item>

        <transition name="fade-slide">
          <el-form-item v-if="isFiveStar" label="雨棚照片 (Canopy Photo) - 五星级专属必填" prop="canopyUrl">
            <el-upload
              class="upload-demo"
              drag
              action="#"
              :auto-upload="false"
              :on-change="(file) => handleFileChange('canopy', file)"
              :show-file-list="false"
            >
              <div v-if="formData.canopyUrl" class="upload-preview">
                <span style="color:var(--success-color); font-weight:600;">照片已准备：{{ formData.canopyUrl }}</span>
                <p style="font-size:12px;color:#999;">点击重新选择照片</p>
              </div>
              <div v-else>
                <div class="el-upload__text">将现场照片拖到此处，或 <em>点击上传</em></div>
              </div>
            </el-upload>
          </el-form-item>
        </transition>

        <el-form-item style="margin-top: 32px; text-align: right;">
          <el-button @click="resetForm">重置所有</el-button>
          <el-button 
            type="primary" 
            @click="submitForm" 
            :disabled="!registrationOpen || (existingRegistration && existingRegistration.status === '待审核') || (existingRegistration && existingRegistration.status === '已通过')"
          >
            {{ existingRegistration && existingRegistration.status === '已退回' ? '修正并重新提交' : '提交星级评定申请' }}
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch, onMounted } from 'vue';

const props = defineProps({
  registrationOpen: Boolean,
  registrations: Array
});

const emit = defineEmits(['submit-registration']);

const formRef = ref(null);
const existingRegistration = ref(null);
const isShopAccount = computed(() => (localStorage.getItem('role') || '').toUpperCase() === 'SHOP');
const currentShopCode = computed(() => localStorage.getItem('shopCode') || '');
const currentShopName = computed(() => localStorage.getItem('shopName') || localStorage.getItem('displayName') || '');

const formData = reactive({
  shopCode: '',
  shopName: '',
  targetStar: '',
  paymentUrl: '',
  canopyUrl: ''
});

// 计算属性：判断是否选择五星级
const isFiveStar = computed(() => formData.targetStar === '五星级');

// 必填验证规则
const rules = reactive({
  shopCode: [{ required: true, message: '请输入服务店代码', trigger: 'blur' }],
  shopName: [{ required: true, message: '请输入服务店名称', trigger: 'blur' }],
  targetStar: [{ required: true, message: '请选择申报星级', trigger: 'change' }],
  paymentUrl: [{ required: true, message: '请上传缴费凭证', trigger: 'change' }]
});

// 动态监听五星级变化补充规则
watch(isFiveStar, (newVal) => {
  if (newVal) {
    rules.canopyUrl = [{ required: true, message: '五星级服务店必须上传雨棚照片', trigger: 'change' }];
  } else {
    delete rules.canopyUrl;
    // 重置雨棚照片
    formData.canopyUrl = '';
  }
});

const checkExistingRegistration = () => {
  if (!formData.shopCode) {
    existingRegistration.value = null;
    return;
  }
  const exist = props.registrations.find(r => r.shopCode === formData.shopCode);
  if (exist) {
    existingRegistration.value = exist;
    formData.shopName = exist.shopName;
    formData.targetStar = exist.targetStar;
    formData.paymentUrl = exist.paymentUrl;
    formData.canopyUrl = exist.canopyUrl;
  } else {
    existingRegistration.value = null;
    if (isShopAccount.value) {
      formData.shopName = currentShopName.value;
    }
  }
};

const handleFileChange = (type, file) => {
  // 模拟上传成功拿到 URL
  if (type === 'payment') {
    formData.paymentUrl = file.name;
  } else if (type === 'canopy') {
    formData.canopyUrl = file.name;
  }
};

const submitForm = () => {
  if (!formRef.value) return;
  formRef.value.validate((valid) => {
    if (valid) {
      emit('submit-registration', { ...formData });
    } else {
      return false;
    }
  });
};

const resetForm = () => {
  if (formRef.value) {
    formRef.value.resetFields();
  }
  formData.paymentUrl = '';
  formData.canopyUrl = '';
  existingRegistration.value = null;
  if (isShopAccount.value) {
    formData.shopCode = currentShopCode.value;
    formData.shopName = currentShopName.value;
    checkExistingRegistration();
  }
};

const initShopSession = () => {
  if (!isShopAccount.value) {
    return;
  }
  formData.shopCode = currentShopCode.value;
  formData.shopName = currentShopName.value;
  checkExistingRegistration();
};

watch(
  () => props.registrations,
  () => {
    if (isShopAccount.value) {
      initShopSession();
    }
  },
  { deep: true }
);

onMounted(() => {
  initShopSession();
});
</script>
