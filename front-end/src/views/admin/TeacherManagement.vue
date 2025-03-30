<template>
  <div class="teacher-management">
    <div class="page-header">
      <div class="header-content">
        <div>
          <h2>教师管理</h2>
          <p>管理系统中的所有教师信息</p>
        </div>
      </div>
      <a-button type="primary" @click="showAddModal">添加教师</a-button>
    </div>

    <!-- 搜索区域 -->
    <div class="search-section">
      <a-form layout="inline" :model="searchForm">
        <a-form-item>
          <a-input-search
            v-model:value="searchForm.name"
            placeholder="搜索教师姓名"
            style="width: 250px"
            @search="handleSearch"
            allowClear
          />
        </a-form-item>
        <a-form-item>
          <a-select
            v-model:value="searchForm.subject"
            style="width: 120px"
            placeholder="教学科目"
            allowClear
            @change="handleSearch"
          >
            <a-select-option value="math">数学</a-select-option>
            <a-select-option value="english">英语</a-select-option>
            <a-select-option value="chinese">语文</a-select-option>
            <a-select-option value="physics">物理</a-select-option>
            <a-select-option value="chemistry">化学</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-select
            v-model:value="searchForm.status"
            style="width: 120px"
            placeholder="在职状态"
            allowClear
            @change="handleSearch"
          >
            <a-select-option value="active">在职</a-select-option>
            <a-select-option value="inactive">离职</a-select-option>
          </a-select>
        </a-form-item>
      </a-form>
    </div>

    <!-- 教师列表 -->
    <a-table :columns="columns" :data-source="filteredTeacherList" :loading="loading" bordered>
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'status'">
          <a-tag :color="record.status === 'active' ? 'green' : 'red'">
            {{ record.status === 'active' ? '在职' : '离职' }}
          </a-tag>
        </template>
        <template v-if="column.key === 'action'">
          <a-space>
            <a-button type="link" @click="handleEdit(record)">编辑</a-button>
            <a-button type="link" @click="handleViewQualifications(record)">查看资质</a-button>
            <a-popconfirm
              title="确定要删除该教师吗？"
              @confirm="handleDelete(record.id)"
            >
              <a-button type="link" danger>删除</a-button>
            </a-popconfirm>
          </a-space>
        </template>
      </template>
    </a-table>

    <!-- 添加/编辑教师弹窗 -->
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
        <a-form-item label="姓名" name="name">
          <a-input v-model:value="formState.name" />
        </a-form-item>
        <a-form-item label="邮箱" name="email">
          <a-input v-model:value="formState.email" />
        </a-form-item>
        <a-form-item label="手机号" name="phone">
          <a-input v-model:value="formState.phone" />
        </a-form-item>
        <a-form-item label="教学科目" name="subjects">
          <a-select
            v-model:value="formState.subjects"
            mode="multiple"
            placeholder="请选择教学科目"
          >
            <a-select-option value="math">数学</a-select-option>
            <a-select-option value="english">英语</a-select-option>
            <a-select-option value="chinese">语文</a-select-option>
            <a-select-option value="physics">物理</a-select-option>
            <a-select-option value="chemistry">化学</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="教学经验" name="experience">
          <a-input-number v-model:value="formState.experience" :min="0" addonAfter="年" />
        </a-form-item>
        <a-form-item label="状态" name="status">
          <a-select v-model:value="formState.status">
            <a-select-option value="active">在职</a-select-option>
            <a-select-option value="inactive">离职</a-select-option>
          </a-select>
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 查看资质弹窗 -->
    <a-modal
      v-model:visible="qualificationsModalVisible"
      title="教师资质"
      :footer="null"
      width="800px"
    >
      <a-descriptions bordered>
        <a-descriptions-item label="教师资格证" :span="3">
          {{ selectedTeacher?.qualifications?.teachingCertificate || '暂无' }}
        </a-descriptions-item>
        <a-descriptions-item label="学历" :span="3">
          {{ selectedTeacher?.qualifications?.education || '暂无' }}
        </a-descriptions-item>
        <a-descriptions-item label="专业" :span="3">
          {{ selectedTeacher?.qualifications?.major || '暂无' }}
        </a-descriptions-item>
        <a-descriptions-item label="其他证书" :span="3">
          <template v-if="selectedTeacher?.qualifications?.otherCertificates?.length">
            <a-tag v-for="cert in selectedTeacher.qualifications.otherCertificates" :key="cert">
              {{ cert }}
            </a-tag>
          </template>
          <template v-else>暂无</template>
        </a-descriptions-item>
      </a-descriptions>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { message } from 'ant-design-vue'

// 搜索表单数据
const searchForm = ref({
  name: '',
  subject: undefined,
  status: undefined
})

// 模拟数据
const mockTeachers = [
  {
    id: 1,
    name: '张老师',
    email: 'zhang@example.com',
    phone: '13900139001',
    subjects: ['math', 'physics'],
    experience: 5,
    status: 'active',
    qualifications: {
      teachingCertificate: '高级教师资格证',
      education: '研究生',
      major: '数学教育',
      otherCertificates: ['奥数教练证']
    }
  },
  {
    id: 2,
    name: '李老师',
    email: 'li@example.com',
    phone: '13900139002',
    subjects: ['english'],
    experience: 8,
    status: 'active',
    qualifications: {
      teachingCertificate: '中级教师资格证',
      education: '本科',
      major: '英语教育',
      otherCertificates: ['托福教师资格证', '雅思教师资格证']
    }
  },
  {
    id: 3,
    name: '王老师',
    email: 'wang@example.com',
    phone: '13900139003',
    subjects: ['chinese', 'english'],
    experience: 3,
    status: 'inactive',
    qualifications: {
      teachingCertificate: '初级教师资格证',
      education: '本科',
      major: '汉语言文学',
      otherCertificates: []
    }
  }
]

// 表格列定义
const columns = [
  {
    title: '姓名',
    dataIndex: 'name',
    key: 'name',
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
    title: '教学科目',
    dataIndex: 'subjects',
    key: 'subjects',
    customRender: ({ text }) => {
      const subjectMap = {
        math: '数学',
        english: '英语',
        chinese: '语文',
        physics: '物理',
        chemistry: '化学'
      }
      return text.map(subject => subjectMap[subject] || subject).join(', ')
    }
  },
  {
    title: '教学经验',
    dataIndex: 'experience',
    key: 'experience',
    customRender: ({ text }) => `${text}年`,
  },
  {
    title: '状态',
    dataIndex: 'status',
    key: 'status',
  },
  {
    title: '操作',
    key: 'action',
  },
]

// 数据状态
const loading = ref(false)
const teacherList = ref([])
const modalVisible = ref(false)
const modalTitle = ref('添加教师')
const formRef = ref(null)
const qualificationsModalVisible = ref(false)
const selectedTeacher = ref(null)

// 筛选后的教师列表
const filteredTeacherList = computed(() => {
  return teacherList.value.filter(teacher => {
    const nameMatch = !searchForm.value.name || 
      teacher.name.toLowerCase().includes(searchForm.value.name.toLowerCase())
    const subjectMatch = !searchForm.value.subject || 
      teacher.subjects.includes(searchForm.value.subject)
    const statusMatch = !searchForm.value.status || 
      teacher.status === searchForm.value.status
    return nameMatch && subjectMatch && statusMatch
  })
})

// 搜索
const handleSearch = () => {
  // 直接使用计算属性filteredTeacherList，无需额外处理
}

// 重置搜索
const handleReset = () => {
  searchForm.value = {
    name: '',
    subject: undefined,
    status: undefined
  }
}

// 获取教师列表
const fetchTeacherList = async () => {
  loading.value = true
  try {
    // 使用模拟数据
    teacherList.value = mockTeachers
  } catch (error) {
    message.error('获取教师列表失败')
  } finally {
    loading.value = false
  }
}

// 显示添加教师弹窗
const showAddModal = () => {
  modalTitle.value = '添加教师'
  formState.value = {
    name: '',
    email: '',
    phone: '',
    subjects: [],
    experience: 0,
    status: 'active',
  }
  modalVisible.value = true
}

// 显示编辑教师弹窗
const handleEdit = (record) => {
  modalTitle.value = '编辑教师'
  formState.value = { ...record }
  modalVisible.value = true
}

// 查看教师资质
const handleViewQualifications = async (record) => {
  selectedTeacher.value = record
  qualificationsModalVisible.value = true
}

// 删除教师
const handleDelete = async (id) => {
  try {
    // TODO: 调用后端API删除教师
    await fetch(`/api/teachers/${id}`, { method: 'DELETE' })
    message.success('删除成功')
    fetchTeacherList()
  } catch (error) {
    message.error('删除失败')
  }
}

// 处理弹窗确认
const handleModalOk = async () => {
  try {
    await formRef.value.validate()
    // TODO: 调用后端API保存教师
    const method = modalTitle.value === '添加教师' ? 'POST' : 'PUT'
    const url = modalTitle.value === '添加教师' ? '/api/teachers' : `/api/teachers/${formState.value.id}`
    await fetch(url, {
      method,
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(formState.value),
    })
    message.success(`${modalTitle.value}成功`)
    modalVisible.value = false
    fetchTeacherList()
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
  fetchTeacherList()
})
</script>

<style lang="less" scoped>
.teacher-management {
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

  .qualifications-modal {
    :deep(.ant-descriptions-item-label) {
      width: 100px;
    }
  }
}
</style> 