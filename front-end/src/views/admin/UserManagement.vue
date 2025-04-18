<template>
  <div class="user-management">
    <div class="page-header">
      <div class="header-content">
        <div>
          <h2>用户管理</h2>
          <p>管理系统中的所有用户账号</p>
        </div>
      </div>
      <a-button type="primary" @click="showAddModal">添加用户</a-button>
    </div>

    <!-- 搜索区域 -->
    <div class="search-section">
      <a-form layout="inline" :model="searchForm">
        <a-form-item>
          <a-input-search
            v-model:value="searchForm.username"
            placeholder="搜索用户名"
            style="width: 250px"
            @search="handleSearch"
            allowClear
          />
        </a-form-item>
      </a-form>
    </div>

    <!-- 用户列表 -->
    <a-table :columns="columns" :data-source="filteredUserList" :loading="loading" bordered>
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'action'">
          <a-space>
            <a-button type="link" @click="handleEdit(record)">编辑</a-button>
            <a-popconfirm
              title="确定要删除该用户吗？"
              @confirm="handleDelete(record.id)"
            >
              <a-button type="link" danger>删除</a-button>
            </a-popconfirm>
          </a-space>
        </template>
      </template>
    </a-table>

    <!-- 添加/编辑用户弹窗 -->
    <a-modal
      v-model:visible="modalVisible"
      :title="modalTitle"
      @ok="handleModalOk"
      @cancel="handleModalCancel"
    >
      <a-form
        ref="formRef"
        :model="formState"
        :rules="rules"
        :label-col="{ span: 6 }"
        :wrapper-col="{ span: 16 }"
      >
        <a-form-item label="邮箱" name="email">
          <a-input v-model:value="formState.email" />
        </a-form-item>
        <a-form-item label="真实姓名" name="realName">
          <a-input v-model:value="formState.realName" />
        </a-form-item>
        <a-form-item label="手机号" name="phone">
          <a-input v-model:value="formState.phone" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { api } from '../../utils/axios'

// 搜索表单数据
const searchForm = ref({
  username: ''
})

// 表格列定义
const columns = [
  {
    title: '用户名',
    dataIndex: 'username',
    key: 'username',
  },
  {
    title: '真实姓名',
    dataIndex: 'realName',
    key: 'realName',
  },
  {
    title: '邮箱',
    dataIndex: 'email',
    key: 'email',
  },
  {
    title: '手机号',
    dataIndex: 'phone',
    key: 'phone',
  },
  {
    title: '操作',
    key: 'action',
  },
]

// 数据状态
const loading = ref(false)
const userList = ref([])
const modalVisible = ref(false)
const modalTitle = ref('添加用户')
const formRef = ref(null)
const formState = ref({
  email: '',
  realName: '',
  phone: ''
})

// 表单验证规则
const rules = {
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入有效的邮箱地址', trigger: 'blur' }
  ],
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' }
  ],
  phone: [
    { required: false, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入有效的手机号码', trigger: 'blur' }
  ]
}

// 筛选后的用户列表
const filteredUserList = computed(() => {
  return userList.value.filter(user => {
    const usernameMatch = !searchForm.value.username || 
      user.username.toLowerCase().includes(searchForm.value.username.toLowerCase())
    return usernameMatch
  })
})

// 搜索
const handleSearch = () => {
  // 直接使用计算属性filteredUserList，无需额外处理
}

// 重置搜索
const handleReset = () => {
  searchForm.value = {
    username: ''
  }
}

// 获取用户列表
const fetchUserList = async () => {
  loading.value = true
  try {
    const response = await api.getAllUsers()
    userList.value = response
  } catch (error) {
    message.error('获取用户列表失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

// 显示添加用户弹窗
const showAddModal = () => {
  modalTitle.value = '添加用户'
  formState.value = {
    email: '',
    realName: '',
    phone: ''
  }
  modalVisible.value = true
}

// 显示编辑用户弹窗
const handleEdit = (record) => {
  modalTitle.value = '编辑用户'
  formState.value = { ...record }
  modalVisible.value = true
}

// 删除用户
const handleDelete = async (id) => {
  try {
    await api.deleteUser(id)
    message.success('删除成功')
    fetchUserList()
  } catch (error) {
    message.error('删除失败')
    console.error(error)
  }
}

// 处理弹窗确认
const handleModalOk = async () => {
  try {
    await formRef.value.validate()
    if (modalTitle.value === '添加用户') {
      await api.createUser(formState.value)
      message.success('添加用户成功')
    } else {
      await api.updateUser(formState.value.id, formState.value)
      message.success('编辑用户成功')
    }
    modalVisible.value = false
    fetchUserList()
  } catch (error) {
    console.error(error)
  }
}

// 处理弹窗取消
const handleModalCancel = () => {
  formRef.value?.resetFields()
  modalVisible.value = false
}

onMounted(() => {
  fetchUserList()
})
</script>

<style lang="less" scoped>
.user-management {
  padding: 24px;

  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 24px;

    .header-content {
      h2 {
        margin-bottom: 8px;
      }

      p {
        color: rgba(0, 0, 0, 0.45);
        margin: 0;
      }
    }
  }

  .search-section {
    background: white;
    padding: 24px;
    border-radius: 4px;
    margin-bottom: 24px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);

    :deep(.ant-form-item) {
      margin-bottom: 0;
      margin-right: 16px;

      &:last-child {
        margin-right: 0;
      }
    }
  }
}
</style> 