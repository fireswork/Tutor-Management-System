<template>
  <div class="subject-management">
    <div class="page-header">
      <div class="header-content">
        <div>
          <h2>科目管理</h2>
          <p>管理系统中的所有教学科目</p>
        </div>
      </div>
      <a-button type="primary" @click="showAddModal">添加科目</a-button>
    </div>

    <!-- 搜索区域 -->
    <div class="search-section">
      <a-form layout="inline" :model="searchForm">
        <a-form-item>
          <a-input-search
            v-model:value="searchForm.name"
            placeholder="搜索科目名称"
            style="width: 250px"
            @search="handleSearch"
            allowClear
          />
        </a-form-item>
      </a-form>
    </div>

    <!-- 科目列表 -->
    <a-table :columns="columns" :data-source="filteredSubjectList" :loading="loading" bordered>
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'action'">
          <a-space>
            <a-button type="link" @click="handleEdit(record)">编辑</a-button>
            <a-popconfirm
              title="确定要删除该科目吗？"
              @confirm="handleDelete(record.id)"
            >
              <a-button type="link" danger>删除</a-button>
            </a-popconfirm>
          </a-space>
        </template>
      </template>
    </a-table>

    <!-- 添加/编辑科目弹窗 -->
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
        <a-form-item label="科目名称" name="name">
          <a-input v-model:value="formState.name" />
        </a-form-item>
        <a-form-item label="科目描述" name="description">
          <a-textarea v-model:value="formState.description" :rows="4" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { message } from 'ant-design-vue'

// 搜索表单数据
const searchForm = ref({
  name: ''
})

// 模拟数据
const mockSubjects = [
  {
    id: 1,
    name: '数学',
    description: '培养学生的数学思维和解决问题的能力'
  },
  {
    id: 2,
    name: '英语',
    description: '培养学生的英语听说读写能力'
  },
  {
    id: 3,
    name: '语文',
    description: '培养学生的语言文字应用能力'
  },
  {
    id: 4,
    name: '物理',
    description: '培养学生对自然科学的理解和探索能力'
  },
  {
    id: 5,
    name: '化学',
    description: '培养学生对物质变化规律的认识和实验能力'
  }
]

// 表格列定义
const columns = [
  {
    title: '科目名称',
    dataIndex: 'name',
    key: 'name',
  },
  {
    title: '科目描述',
    dataIndex: 'description',
    key: 'description',
    ellipsis: true
  },
  {
    title: '操作',
    key: 'action',
    width: 150
  }
]

// 数据状态
const loading = ref(false)
const subjectList = ref([])
const modalVisible = ref(false)
const modalTitle = ref('添加科目')
const formRef = ref(null)

const formState = ref({
  name: '',
  description: ''
})

// 表单校验规则
const rules = {
  name: [{ required: true, message: '请输入科目名称' }],
  description: [{ required: true, message: '请输入科目描述' }]
}

// 筛选后的科目列表
const filteredSubjectList = computed(() => {
  return subjectList.value.filter(subject => {
    return !searchForm.value.name || 
      subject.name.toLowerCase().includes(searchForm.value.name.toLowerCase())
  })
})

// 搜索
const handleSearch = () => {
  // 直接使用计算属性filteredSubjectList，无需额外处理
}

// 获取科目列表
const fetchSubjectList = async () => {
  loading.value = true
  try {
    // 使用模拟数据
    subjectList.value = mockSubjects
  } catch (error) {
    message.error('获取科目列表失败')
  } finally {
    loading.value = false
  }
}

// 显示添加科目弹窗
const showAddModal = () => {
  modalTitle.value = '添加科目'
  formState.value = {
    name: '',
    description: ''
  }
  modalVisible.value = true
}

// 显示编辑科目弹窗
const handleEdit = (record) => {
  modalTitle.value = '编辑科目'
  formState.value = { ...record }
  modalVisible.value = true
}

// 删除科目
const handleDelete = async (id) => {
  try {
    // TODO: 调用后端API删除科目
    await fetch(`/api/subjects/${id}`, { method: 'DELETE' })
    message.success('删除成功')
    fetchSubjectList()
  } catch (error) {
    message.error('删除失败')
  }
}

// 处理弹窗确认
const handleModalOk = async () => {
  try {
    await formRef.value.validate()
    // TODO: 调用后端API保存科目
    const method = modalTitle.value === '添加科目' ? 'POST' : 'PUT'
    const url = modalTitle.value === '添加科目' ? '/api/subjects' : `/api/subjects/${formState.value.id}`
    await fetch(url, {
      method,
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(formState.value),
    })
    message.success(`${modalTitle.value}成功`)
    modalVisible.value = false
    fetchSubjectList()
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
  fetchSubjectList()
})
</script>

<style lang="less" scoped>
.subject-management {
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