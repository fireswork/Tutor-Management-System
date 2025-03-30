<template>
  <div class="user-profile-container">
    <!-- 用户信息模块 -->
    <a-card title="用户信息" :bordered="false" class="profile-card">
      <template #extra>
        <a-button type="primary" @click="showPasswordModal">
          修改密码
        </a-button>
      </template>
      <a-descriptions bordered>
        <a-descriptions-item label="用户名" span="3">
          {{ userInfo.username }}
        </a-descriptions-item>
        <a-descriptions-item label="用户角色" span="3">
          {{ userInfo.role === 'teacher' ? '教师' : '学生' }}
        </a-descriptions-item>
        <a-descriptions-item label="注册时间" span="3">
          {{ userInfo.registerTime }}
        </a-descriptions-item>
      </a-descriptions>
  
    </a-card>
    
    <!-- 个人信息模块 -->
    <a-card title="个人资料" :bordered="false" class="profile-card">
      <template #extra v-if="!editMode">
        <a-button type="primary" @click="editMode = true"> 编辑信息 </a-button>
      </template>
      <template v-if="!editMode">
        <a-descriptions bordered>
          <a-descriptions-item label="真实姓名" span="3">
            {{ userInfo.realName }}
          </a-descriptions-item>
          <a-descriptions-item label="电子邮箱" span="3">
            {{ userInfo.email }}
          </a-descriptions-item>
          <a-descriptions-item label="手机号码" span="3">
            {{ userInfo.phone }}
          </a-descriptions-item>
        </a-descriptions>
      </template>

      <template v-else>
        <a-form :model="editForm" name="user-form" layout="vertical">
          <a-form-item label="真实姓名" name="realName">
            <a-input v-model:value="editForm.realName" />
          </a-form-item>
          <a-form-item label="电子邮箱" name="email">
            <a-input v-model:value="editForm.email" />
          </a-form-item>
          <a-form-item label="手机号码" name="phone">
            <a-input v-model:value="editForm.phone" />
          </a-form-item>

          <div class="action-buttons">
            <a-button type="primary" @click="saveUserInfo"> 保存 </a-button>
            <a-button style="margin-left: 10px" @click="cancelEdit">
              取消
            </a-button>
          </div>
        </a-form>
      </template>
    </a-card>
    
    <!-- 修改密码对话框 -->
    <a-modal
      v-model:visible="passwordModalVisible"
      title="修改密码"
      @ok="changePassword"
      :confirmLoading="passwordLoading"
      okText="确认修改"
      cancelText="取消"
    >
      <a-form :model="passwordForm" layout="vertical">
        <a-form-item 
          label="当前密码" 
          name="currentPassword"
          :rules="[{ required: true, message: '请输入当前密码' }]"
        >
          <a-input-password v-model:value="passwordForm.currentPassword" placeholder="请输入当前密码" />
        </a-form-item>
        <a-form-item 
          label="新密码" 
          name="newPassword"
          :rules="[
            { required: true, message: '请输入新密码' },
            { min: 6, message: '密码长度不能少于6个字符' }
          ]"
        >
          <a-input-password v-model:value="passwordForm.newPassword" placeholder="请输入新密码" />
        </a-form-item>
        <a-form-item 
          label="确认新密码" 
          name="confirmPassword"
          :rules="[
            { required: true, message: '请确认新密码' },
            { validator: validateConfirmPassword }
          ]"
        >
          <a-input-password v-model:value="passwordForm.confirmPassword" placeholder="请确认新密码" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from "vue";
import { message } from "ant-design-vue";

const editMode = ref(false);
const passwordModalVisible = ref(false);
const passwordLoading = ref(false);

// 用户信息
const userInfo = reactive({
  username: "zhang_student",
  realName: "张三",
  email: "zhang@example.com",
  phone: "13800138000",
  role: "teacher", // 可以通过localStorage获取
  registerTime: "2023-06-15",
});

// 修改信息表单
const editForm = reactive({
  username: userInfo.username,
  realName: userInfo.realName,
  email: userInfo.email,
  phone: userInfo.phone,
});

// 修改密码表单
const passwordForm = reactive({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
});

// 确认密码验证
const validateConfirmPassword = async (rule, value) => {
  if (value !== passwordForm.newPassword) {
    throw new Error('两次输入的密码不一致');
  }
  return Promise.resolve();
};

// 保存用户信息
const saveUserInfo = () => {
  // 实际项目中应该发送请求到后端保存数据
  // 这里仅模拟更新本地数据
  userInfo.realName = editForm.realName;
  userInfo.email = editForm.email;
  userInfo.phone = editForm.phone;

  message.success("个人信息修改成功");
  editMode.value = false;
};

// 取消编辑
const cancelEdit = () => {
  // 重置表单数据
  editForm.realName = userInfo.realName;
  editForm.email = userInfo.email;
  editForm.phone = userInfo.phone;

  editMode.value = false;
};

// 显示密码修改模态框
const showPasswordModal = () => {
  passwordModalVisible.value = true;
  // 清空表单
  passwordForm.currentPassword = '';
  passwordForm.newPassword = '';
  passwordForm.confirmPassword = '';
};

// 修改密码
const changePassword = () => {
  // 表单验证
  if (!passwordForm.currentPassword || !passwordForm.newPassword || !passwordForm.confirmPassword) {
    message.error('请填写完整密码信息');
    return;
  }
  
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    message.error('两次输入的新密码不一致');
    return;
  }
  
  passwordLoading.value = true;
  
  // 实际项目中应该发送请求到后端验证并修改密码
  setTimeout(() => {
    passwordLoading.value = false;
    passwordModalVisible.value = false;
    message.success('密码修改成功');
  }, 1000);
};

onMounted(() => {
  // 实际项目中可能需要从API获取用户数据
  // 也可以从localStorage获取部分用户信息
  const storedUserName = localStorage.getItem("userName");
  const storedUserRole =
    localStorage.getItem("userRole") ||
    localStorage.getItem("isTeacher") === "true"
      ? "teacher"
      : "student";

  if (storedUserName) {
    userInfo.username = storedUserName;
  }

  if (storedUserRole) {
    userInfo.role = storedUserRole;
  }
});
</script>

<style lang="less" scoped>
@import "../../assets/styles/variables.less";

.user-profile-container {
  width: 100%;

  .profile-card {
    margin-bottom: 24px;
  }

  .action-buttons {
    margin-top: @padding-md;
  }
}
</style>
