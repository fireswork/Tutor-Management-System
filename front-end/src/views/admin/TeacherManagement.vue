<template>
  <div class="teacher-management">
    <div class="page-header">
      <div class="header-content">
        <div>
          <h2>教师管理</h2>
          <p>管理系统中的所有教师信息</p>
        </div>
      </div>
      <div>
        <a-button type="primary" @click="showAddModal" style="margin-right: 10px;">添加教师</a-button>
      </div>
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
            <a-select-option v-for="option in SUBJECT_OPTIONS" :key="option.value" :value="option.value">
              {{ option.label }}
            </a-select-option>
          </a-select>
        </a-form-item>
      </a-form>
    </div>

    <!-- 教师列表 -->
    <a-table :columns="columns" :data-source="filteredTeacherList" :loading="loading" bordered>
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'action'">
          <a-space>
            <a-button type="link" @click="handleEdit(record)">编辑</a-button>
            <a-button type="link" @click="handleReviewQualifications(record)">审核资质</a-button>
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
      :confirmLoading="loading"
      @ok="handleModalOk"
      @cancel="handleModalCancel"
      okText="保存"
      cancelText="取消"
      width="550px"
    >
      <a-form
        ref="formRef"
        :model="formState"
        :rules="rules"
        :label-col="{ span: 6 }"
        :wrapper-col="{ span: 16 }"
      >
        <a-form-item label="姓名" name="name">
          <a-input v-model:value="formState.name" placeholder="请输入教师姓名" />
        </a-form-item>
        <a-form-item label="邮箱" name="email">
          <a-input v-model:value="formState.email" placeholder="请输入教师邮箱" />
        </a-form-item>
        <a-form-item label="手机号" name="phone">
          <a-input v-model:value="formState.phone" placeholder="请输入手机号码" />
        </a-form-item>
        <a-form-item label="学历" name="education">
          <a-select
            v-model:value="formState.education"
            style="width: 100%"
            placeholder="请选择学历"
            :options="educationOptions"
          >
          </a-select>
        </a-form-item>
        <a-form-item label="教学科目" name="subjects">
          <a-select
            v-model:value="formState.subjects"
            mode="multiple"
            placeholder="请选择教学科目"
            :options="SUBJECT_OPTIONS"
          >
          </a-select>
        </a-form-item>
        <a-form-item label="教学经验" name="experience">
          <a-input-number v-model:value="formState.experience" :min="0" style="width: 100%" addonAfter="年" placeholder="请输入教学经验" />
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
import { useRouter } from 'vue-router'
import axios, { api } from '../../utils/axios'
import { SUBJECT_OPTIONS, SUBJECTS } from '../../utils/constants'

const router = useRouter()

// 搜索表单数据
const searchForm = ref({
  name: '',
  subject: undefined
})

// 数据状态
const loading = ref(false)
const teacherList = ref([])
const modalVisible = ref(false)
const modalTitle = ref('添加教师')
const formRef = ref(null)
const qualificationsModalVisible = ref(false)
const selectedTeacher = ref(null)
const formState = ref({
  name: '',
  email: '',
  phone: '',
  subjects: [],
  experience: 0,
  education: ''
})

// 学历选项
const educationOptions = [
  { value: '大学本科', label: '大学本科' },
  { value: '硕士研究生', label: '硕士研究生' },
  { value: '博士研究生', label: '博士研究生' },
  { value: '大专', label: '大专' },
  { value: '其他', label: '其他' }
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
    title: '学历',
    dataIndex: 'education',
    key: 'education',
  },
  {
    title: '教学科目',
    dataIndex: 'subjects',
    key: 'subjects',
    customRender: ({ text }) => {
      if (!text || text.length === 0) return '';
      return text.join(', ');
    }
  },
  {
    title: '教学经验',
    dataIndex: 'experience',
    key: 'experience',
    customRender: ({ text }) => `${text}年`,
  },
  {
    title: '操作',
    key: 'action',
  },
]

// 表单验证规则
const rules = {
  name: [
    { required: true, message: '请输入教师姓名', trigger: 'blur' },
    { min: 2, message: '姓名长度至少为2个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入有效的邮箱地址', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号码', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入有效的手机号码', trigger: 'blur' }
  ],
  subjects: [
    { required: true, type: 'array', min: 1, message: '请至少选择一个教学科目', trigger: 'change' }
  ],
  experience: [
    { required: true, type: 'number', message: '请输入教学经验年数', trigger: 'change' }
  ],
  education: [
    { required: true, message: '请选择学历', trigger: 'change' }
  ]
}

// 筛选后的教师列表
const filteredTeacherList = computed(() => {
  return teacherList.value?.filter(teacher => {
    const nameMatch = !searchForm.value.name || 
      teacher.name.toLowerCase().includes(searchForm.value.name.toLowerCase())
    const subjectMatch = !searchForm.value.subject || 
      teacher.subjects.includes(searchForm.value.subject)
    return nameMatch && subjectMatch
  })
})

// 搜索
const handleSearch = () => {
  fetchTeacherList()
}

// 重置搜索
const handleReset = () => {
  searchForm.value = {
    name: '',
    subject: undefined
  }
  fetchTeacherList()
}

// 获取教师列表
const fetchTeacherList = async () => {
  loading.value = true
  try {
    // 构建查询参数
    const params = {}
    if (searchForm.value.name) {
      params.name = searchForm.value.name
    }
    if (searchForm.value.subject) {
      params.subject = searchForm.value.subject
    }
    
    // 调用API获取教师列表
    const response = await api.getAllTeachers(params)
    teacherList.value = response
  } catch (error) {
    console.error('获取教师列表失败:', error)
    message.error('获取教师列表失败')
    teacherList.value = [] // 失败时设置空列表
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
    education: ''
  }
  if (formRef.value) {
    formRef.value.resetFields()
  }
  modalVisible.value = true
}

// 显示编辑教师弹窗
const handleEdit = (record) => {
  modalTitle.value = '编辑教师'
  formState.value = { 
    id: record.id,
    name: record.name,
    email: record.email,
    phone: record.phone,
    subjects: [...record.subjects], // 创建副本以避免直接修改原始数据
    experience: record.experience,
    education: record.education
  }
  if (formRef.value) {
    formRef.value.resetFields()
  }
  modalVisible.value = true
}

// 查看教师资质
const handleViewQualifications = async (record) => {
  selectedTeacher.value = record
  qualificationsModalVisible.value = true
}

// 跳转到资质审核页面
const handleReviewQualifications = (record) => {
  router.push({
    name: 'TeacherQualificationReview',
    params: { teacherId: record.id }
  })
}

// 删除教师
const handleDelete = async (id) => {
  try {
    // 调用后端API删除教师
    await api.deleteTeacher(id)
    message.success('删除成功')
    fetchTeacherList()
  } catch (error) {
    console.error('删除教师失败:', error)
    message.error(error.response?.data?.error || '删除失败')
  }
}

// 处理弹窗确认
const handleModalOk = async () => {
  try {
    // 表单验证
    await formRef.value.validate()
    
    // 显示加载状态
    loading.value = true
    
    // 准备数据
    const teacherData = {
      id: formState.value.id,
      name: formState.value.name,
      email: formState.value.email,
      phone: formState.value.phone,
      subjects: formState.value.subjects,
      experience: formState.value.experience,
      education: formState.value.education,
      status: 'active' // 默认设置为活跃状态
    }
    
    // 调用后端API保存教师
    const isAdd = modalTitle.value === '添加教师'
    
    if (isAdd) {
      // 添加教师
      await api.addTeacher(teacherData)
      message.success('添加教师成功')
    } else {
      // 更新教师
      await api.updateTeacher(teacherData.id, teacherData)
      message.success('编辑教师成功')
    }
    
    // 关闭弹窗并刷新列表
    modalVisible.value = false
    fetchTeacherList()
  } catch (error) {
    if (error.errorFields) {
      // 表单验证错误
      message.error('请检查表单填写是否正确')
    } else {
      // API请求错误
      console.error(error)
      message.error(error.response?.data?.error || `${modalTitle.value}失败`)
    }
  } finally {
    loading.value = false
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