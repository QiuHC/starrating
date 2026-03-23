<template>
  <div class="access-page">
    <div class="page-header access-header">
      <div>
        <h2 class="page-title">账号与权限管理</h2>
        <p class="page-desc">管理厂端账号、店端账号、角色及模块级权限矩阵。</p>
      </div>
      <div class="header-actions">
        <el-button plain @click="openRoleDialog()">新增角色</el-button>
        <el-button type="primary" @click="openAccountDialog()">新增账号</el-button>
      </div>
    </div>

    <el-card class="page-card" shadow="never">
      <template #header>
        <div class="card-header-row">
          <span class="section-title">用户列表</span>
          <div class="toolbar">
            <el-select v-model="filters.userType" clearable placeholder="账号类型" style="width: 140px;">
              <el-option label="厂端账号" value="FACTORY" />
              <el-option label="店端账号" value="SHOP" />
            </el-select>
            <el-select v-model="filters.status" clearable placeholder="状态" style="width: 120px;">
              <el-option label="激活" value="ACTIVE" />
              <el-option label="停用" value="DISABLED" />
            </el-select>
          </div>
        </div>
      </template>

      <el-table :data="filteredAccounts" style="width: 100%">
        <el-table-column label="用户名" min-width="220">
          <template #default="scope">
            <div class="user-cell">
              <span class="user-name">{{ scope.row.displayName }}</span>
              <span class="user-meta">
                {{ scope.row.userType === 'SHOP' ? `shopCode: ${scope.row.shopCode}` : `登录账号: ${scope.row.username}` }}
              </span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="email" label="邮箱" min-width="220">
          <template #default="scope">
            {{ scope.row.email || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="账号类型" width="120">
          <template #default="scope">
            {{ scope.row.userType === 'SHOP' ? '店端账号' : '厂端账号' }}
          </template>
        </el-table-column>
        <el-table-column label="角色" width="180">
          <template #default="scope">
            <el-tag :type="scope.row.userType === 'SHOP' ? 'warning' : 'success'" effect="plain">
              {{ scope.row.roleName }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="120">
          <template #default="scope">
            <span>{{ scope.row.status === 'ACTIVE' ? '激活' : '停用' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="scope">
            <el-button type="primary" link @click="openAccountDialog(scope.row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-card class="page-card" shadow="never">
      <template #header>
        <div class="card-header-row">
          <div>
            <span class="section-title">角色权限分配</span>
            <span v-if="selectedRole" class="section-subtitle">（当前：{{ selectedRole.roleName }}）</span>
          </div>
          <div class="toolbar">
            <el-select v-model="selectedRoleId" placeholder="选择角色" style="width: 220px;" @change="syncRoleSelection">
              <el-option
                v-for="role in roles"
                :key="role.id"
                :label="role.roleName"
                :value="role.id"
              />
            </el-select>
            <el-button :disabled="!selectedRole" @click="openRoleDialog(selectedRole)">编辑角色</el-button>
            <el-button type="primary" :disabled="!selectedRole" @click="saveSelectedRolePermissions">保存权限</el-button>
          </div>
        </div>
      </template>

      <div v-if="selectedRole" class="permission-grid">
        <div v-for="group in permissionGroups" :key="group.groupName" class="permission-group">
          <h3>{{ group.groupName }}</h3>
          <el-checkbox-group v-model="selectedPermissionIds">
            <el-checkbox
              v-for="permission in group.permissions"
              :key="permission.id"
              :label="permission.id"
            >
              {{ permission.permissionName }}
            </el-checkbox>
          </el-checkbox-group>
        </div>
      </div>
      <el-empty v-else description="请选择一个角色后分配权限"></el-empty>
    </el-card>

    <el-dialog v-model="accountDialogVisible" :title="editingAccount ? '编辑账号' : '新增账号'" width="520px">
      <el-form :model="accountForm" label-position="top">
        <el-form-item label="账号类型">
          <el-radio-group v-model="accountForm.userType">
            <el-radio label="FACTORY">厂端账号</el-radio>
            <el-radio label="SHOP">店端账号</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="accountForm.userType === 'FACTORY'" label="登录账号">
          <el-input v-model="accountForm.username" placeholder="请输入厂端账号" />
        </el-form-item>
        <el-form-item v-if="accountForm.userType === 'SHOP'" label="Shop Code / 登录账号">
          <el-input v-model="accountForm.shopCode" placeholder="请输入服务店 shopCode" />
        </el-form-item>
        <el-form-item label="显示名称">
          <el-input v-model="accountForm.displayName" placeholder="请输入显示名称" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="accountForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item v-if="accountForm.userType === 'SHOP'" label="服务店名称">
          <el-input v-model="accountForm.shopName" placeholder="请输入服务店名称" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="accountForm.roleId" placeholder="请选择角色" style="width: 100%;">
            <el-option
              v-for="role in roleOptionsForAccount"
              :key="role.id"
              :label="role.roleName"
              :value="role.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="accountForm.status" style="width: 100%;">
            <el-option label="激活" value="ACTIVE" />
            <el-option label="停用" value="DISABLED" />
          </el-select>
        </el-form-item>
        <el-form-item :label="editingAccount ? '重置密码（可选）' : '初始密码'">
          <el-input v-model="accountForm.password" type="password" show-password placeholder="请输入密码" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="accountDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveAccount">保存账号</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="roleDialogVisible" :title="editingRole ? '编辑角色' : '新增角色'" width="460px">
      <el-form :model="roleForm" label-position="top">
        <el-form-item label="角色编码">
          <el-input v-model="roleForm.roleCode" :disabled="Boolean(editingRole)" placeholder="例如 FACTORY_REVIEWER" />
        </el-form-item>
        <el-form-item label="角色名称">
          <el-input v-model="roleForm.roleName" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="角色域">
          <el-select v-model="roleForm.roleScope" style="width: 100%;">
            <el-option label="厂端" value="FACTORY" />
            <el-option label="店端" value="SHOP" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="roleForm.status" style="width: 100%;">
            <el-option label="激活" value="ACTIVE" />
            <el-option label="停用" value="DISABLED" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="roleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveRole">保存角色</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import request from '../utils/request';

const accounts = ref([]);
const roles = ref([]);
const permissionGroups = ref([]);
const selectedRoleId = ref(null);
const selectedPermissionIds = ref([]);

const filters = reactive({
  userType: '',
  status: '',
});

const accountDialogVisible = ref(false);
const roleDialogVisible = ref(false);
const editingAccount = ref(null);
const editingRole = ref(null);

const accountForm = reactive({
  username: '',
  email: '',
  displayName: '',
  password: '',
  userType: 'FACTORY',
  status: 'ACTIVE',
  shopCode: '',
  shopName: '',
  roleId: null,
});

const roleForm = reactive({
  roleCode: '',
  roleName: '',
  roleScope: 'FACTORY',
  status: 'ACTIVE',
});

const filteredAccounts = computed(() =>
  accounts.value.filter((item) => {
    const matchType = !filters.userType || item.userType === filters.userType;
    const matchStatus = !filters.status || item.status === filters.status;
    return matchType && matchStatus;
  })
);

const selectedRole = computed(() => roles.value.find((role) => role.id === selectedRoleId.value) || null);

const roleOptionsForAccount = computed(() =>
  roles.value.filter((role) => role.roleScope === accountForm.userType)
);

const loadAccessData = async () => {
  const [accountRes, roleRes, permissionRes] = await Promise.all([
    request.get('/accounts'),
    request.get('/roles'),
    request.get('/roles/permissions'),
  ]);

  accounts.value = accountRes || [];
  roles.value = roleRes || [];
  permissionGroups.value = permissionRes || [];

  if (!selectedRoleId.value && roles.value.length) {
    selectedRoleId.value = roles.value[0].id;
    syncRoleSelection();
  }
};

const syncRoleSelection = () => {
  if (!selectedRole.value) return;
  selectedPermissionIds.value = [...(selectedRole.value.permissionIds || [])];
};

const resetAccountForm = () => {
  accountForm.username = '';
  accountForm.email = '';
  accountForm.displayName = '';
  accountForm.password = '';
  accountForm.userType = 'FACTORY';
  accountForm.status = 'ACTIVE';
  accountForm.shopCode = '';
  accountForm.shopName = '';
  accountForm.roleId = null;
};

const openAccountDialog = (account = null) => {
  resetAccountForm();
  editingAccount.value = account;
  if (account) {
    accountForm.username = account.username;
    accountForm.email = account.email;
    accountForm.displayName = account.displayName;
    accountForm.userType = account.userType;
    accountForm.status = account.status;
    accountForm.shopCode = account.shopCode;
    accountForm.shopName = account.shopName;
    accountForm.roleId = account.roleId;
  } else {
    const firstRole = roleOptionsForAccount.value[0];
    accountForm.roleId = firstRole ? firstRole.id : null;
  }
  accountDialogVisible.value = true;
};

const saveAccount = async () => {
  if (!accountForm.displayName || !accountForm.roleId) {
    ElMessage.warning('请先填写显示名称并选择角色');
    return;
  }
  if (accountForm.userType === 'FACTORY' && !accountForm.username) {
    ElMessage.warning('请输入厂端登录账号');
    return;
  }
  if (accountForm.userType === 'SHOP' && (!accountForm.shopCode || !accountForm.shopName)) {
    ElMessage.warning('店端账号必须填写 shopCode 和服务店名称');
    return;
  }
  if (!editingAccount.value && !accountForm.password) {
    ElMessage.warning('请设置初始密码');
    return;
  }

  const payload = {
    username: accountForm.userType === 'SHOP' ? accountForm.shopCode : accountForm.username,
    email: accountForm.email,
    displayName: accountForm.displayName,
    password: accountForm.password,
    userType: accountForm.userType,
    status: accountForm.status,
    shopCode: accountForm.userType === 'SHOP' ? accountForm.shopCode : null,
    shopName: accountForm.userType === 'SHOP' ? accountForm.shopName : null,
    roleId: accountForm.roleId,
  };

  try {
    if (editingAccount.value) {
      await request.put(`/accounts/${editingAccount.value.id}`, payload);
      ElMessage.success('账号已更新');
    } else {
      await request.post('/accounts', payload);
      ElMessage.success('账号已创建');
    }
    accountDialogVisible.value = false;
    await loadAccessData();
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '账号保存失败');
  }
};

const resetRoleForm = () => {
  roleForm.roleCode = '';
  roleForm.roleName = '';
  roleForm.roleScope = 'FACTORY';
  roleForm.status = 'ACTIVE';
};

const openRoleDialog = (role = null) => {
  resetRoleForm();
  editingRole.value = role;
  if (role) {
    roleForm.roleCode = role.roleCode;
    roleForm.roleName = role.roleName;
    roleForm.roleScope = role.roleScope;
    roleForm.status = role.status;
  }
  roleDialogVisible.value = true;
};

const saveRole = async () => {
  if (!roleForm.roleCode || !roleForm.roleName) {
    ElMessage.warning('请填写角色编码和角色名称');
    return;
  }

  const payload = {
    roleCode: roleForm.roleCode,
    roleName: roleForm.roleName,
    roleScope: roleForm.roleScope,
    status: roleForm.status,
    permissionIds: editingRole.value ? selectedPermissionIds.value : [],
  };

  try {
    if (editingRole.value) {
      await request.put(`/roles/${editingRole.value.id}`, payload);
      ElMessage.success('角色已更新');
    } else {
      const created = await request.post('/roles', payload);
      ElMessage.success('角色已创建');
      selectedRoleId.value = created.id;
    }
    roleDialogVisible.value = false;
    await loadAccessData();
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '角色保存失败');
  }
};

const saveSelectedRolePermissions = async () => {
  if (!selectedRole.value) return;
  try {
    await request.put(`/roles/${selectedRole.value.id}`, {
      roleCode: selectedRole.value.roleCode,
      roleName: selectedRole.value.roleName,
      roleScope: selectedRole.value.roleScope,
      status: selectedRole.value.status,
      permissionIds: selectedPermissionIds.value,
    });
    ElMessage.success('权限配置已保存');
    await loadAccessData();
    selectedRoleId.value = selectedRole.value.id;
    syncRoleSelection();
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '权限保存失败');
  }
};

onMounted(loadAccessData);
</script>

<style scoped>
.access-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  border-bottom: none;
  margin-bottom: 20px;
  padding-bottom: 0;
}

.header-actions,
.toolbar,
.card-header-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.card-header-row {
  justify-content: space-between;
  width: 100%;
}

.section-title {
  font-size: 16px;
  font-weight: 700;
  color: var(--text-primary);
}

.section-subtitle {
  margin-left: 6px;
  color: var(--text-secondary);
  font-size: 14px;
}

.permission-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 24px;
}

.permission-group {
  padding: 20px 24px;
  border: 1px solid var(--border-color);
  border-radius: 16px;
  background: linear-gradient(180deg, #ffffff 0%, #f9fbff 100%);
}

.permission-group h3 {
  font-size: 15px;
  margin-bottom: 14px;
}

.permission-group :deep(.el-checkbox-group) {
  display: grid;
  grid-template-columns: 1fr;
  gap: 14px;
}

.user-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.user-name {
  font-weight: 600;
  color: var(--text-primary);
}

.user-meta {
  font-size: 12px;
  color: var(--text-secondary);
}
</style>
