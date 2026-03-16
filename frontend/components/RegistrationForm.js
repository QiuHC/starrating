(() => {
  const { ref, reactive, computed, watch, onMounted } = Vue;

  const template = `
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
                  placeholder="请输入您的服务店唯一代码" 
                  clearable
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
  `;

  window.RegistrationForm = {
    template,
    props: {
      registrationOpen: Boolean,
      registrations: Array
    },
    emits: ['submit-registration'],
    setup(props, { emit }) {
      const formRef = ref(null);
      const checkingCode = ref(false);

      // 初始化表单数据
      const initialState = {
        shopCode: '',
        shopName: '',
        targetStar: '',
        paymentUrl: '',
        canopyUrl: ''
      };

      const formData = reactive({ ...initialState });

      // 查找是否已经提交过
      const existingRegistration = computed(() => {
        if (!formData.shopCode) return null;
        return props.registrations.find(r => r.shopCode === formData.shopCode && formData.shopCode.trim() !== '');
      });

      const isFiveStar = computed(() => formData.targetStar === '五星级');

      const validateCanopy = (rule, value, callback) => {
        if (isFiveStar.value && !value) {
          callback(new Error('申报五星级必须上传雨棚照片'));
        } else {
          callback();
        }
      };

      const rules = reactive({
        shopCode: [{ required: true, message: '请输入服务店代码', trigger: 'blur' }],
        shopName: [{ required: true, message: '请输入服务店名称', trigger: 'blur' }],
        targetStar: [{ required: true, message: '请选择申报星级', trigger: 'change' }],
        paymentUrl: [{ required: true, message: '请上传缴费扣费凭证', trigger: 'change' }],
        canopyUrl: [{ validator: validateCanopy, trigger: 'change' }]
      });

      // 模拟文件上传
      const handleFileChange = (type, file) => {
        if (type === 'payment') {
          formData.paymentUrl = file.name;
          // 触发再校验
          formRef.value.validateField('paymentUrl');
        } else if (type === 'canopy') {
          formData.canopyUrl = file.name;
          formRef.value.validateField('canopyUrl');
        }
        ElementPlus.ElMessage.success('文件已被选中');
      };

      const checkExistingRegistration = () => {
        if (formData.shopCode) {
          checkingCode.value = true;
          setTimeout(() => {
            checkingCode.value = false;
            const exist = existingRegistration.value;
            if (exist) {
              // 如果存在已被退回的，自动带出现有信息供修改
              if (exist.status === '已退回') {
                formData.shopName = exist.shopName;
                formData.targetStar = exist.targetStar;
                formData.paymentUrl = exist.paymentUrl;
                formData.canopyUrl = exist.canopyUrl;
                ElementPlus.ElMessage.warning('检测到您有被退回的申请记录，请基于原信息进行修改。');
              } else if (exist.status === '待审核') {
                formData.shopName = exist.shopName;
                formData.targetStar = exist.targetStar;
                // 锁定不再修改在 template 控制了
              }
            }
          }, 800);
        }
      };

      const resetForm = () => {
        if (formRef.value) {
          formRef.value.resetFields();
          Object.assign(formData, initialState);
        }
      };

      const submitForm = async () => {
        if (!formRef.value) return;
        await formRef.value.validate((valid, fields) => {
          if (valid) {
            emit('submit-registration', { ...formData });
            // 不清空 shopCode 方便用户查看状态
          } else {
            console.log('error submit!', fields);
          }
        });
      };

      return {
        formRef,
        formData,
        rules,
        isFiveStar,
        existingRegistration,
        checkingCode,
        handleFileChange,
        checkExistingRegistration,
        resetForm,
        submitForm
      };
    }
  };
})();
