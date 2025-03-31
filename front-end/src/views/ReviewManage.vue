<template>
  <div class="review-manage-container container animate__animated animate__fadeIn">
    <div class="review-manage-header">
      <div class="header-content">
        <div>
          <h2>评价管理</h2>
          <p>管理您的课程评价记录</p>
        </div>
      </div>
    </div>

    <!-- 搜索区域 -->
    <div class="search-section">
      <a-form layout="inline" :model="searchForm">
        <a-form-item>
          <a-input-search
            v-model:value="searchForm.keyword"
            placeholder="搜索课程名称/教师"
            style="width: 250px"
            @search="handleSearch"
            allowClear
          />
        </a-form-item>
        <a-form-item>
          <a-select
            v-model:value="searchForm.rating"
            style="width: 120px"
            placeholder="评分筛选"
            allowClear
            @change="handleSearch"
          >
            <a-select-option value="5">5星</a-select-option>
            <a-select-option value="4">4星及以上</a-select-option>
            <a-select-option value="3">3星及以上</a-select-option>
          </a-select>
        </a-form-item>
      </a-form>
    </div>

    <!-- 评价列表 -->
    <a-table
      :dataSource="reviews"
      :columns="columns"
      :pagination="pagination"
      @change="handleTableChange"
      :loading="loading"
      bordered
    >
      <!-- 课程信息 -->
      <template #courseName="{ record }">
        <div class="course-info">
          <img :src="record.courseCover" :alt="record.courseTitle" class="course-cover" />
          <div class="course-detail">
            <div class="course-title">{{ record.courseTitle }}</div>
            <div class="course-teacher">教师：{{ record.teacherName }}</div>
          </div>
        </div>
      </template>

      <!-- 评分 -->
      <template #rating="{ record }">
        <a-rate :value="record.rating" disabled allow-half />
      </template>

      <!-- 评价内容 -->
      <template #content="{ record }">
        <a-typography-paragraph
          :content="record.content"
          :ellipsis="{ rows: 2, expandable: true }"
        />
      </template>

      <!-- 操作按钮 -->
      <template #action="{ record }">
        <a-space>
          <a-button type="primary" size="small" @click="handleEdit(record)">
            编辑
          </a-button>
          <a-popconfirm
            title="确定要删除这条评价吗？"
            @confirm="handleDelete(record)"
            okText="确定"
            cancelText="取消"
          >
            <a-button danger size="small">
              删除
            </a-button>
          </a-popconfirm>
        </a-space>
      </template>
    </a-table>

    <!-- 编辑评价弹窗 -->
    <a-modal
      v-model:visible="editModalVisible"
      title="编辑评价"
      @ok="submitEdit"
      :confirmLoading="editLoading"
    >
      <a-form :model="editForm" layout="vertical">
        <a-form-item label="评分">
          <a-rate v-model:value="editForm.rating" allow-half />
        </a-form-item>
        <a-form-item label="评价内容">
          <a-textarea
            v-model:value="editForm.content"
            :rows="4"
            placeholder="请分享您的学习体验"
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { api } from '../utils/axios'
import dayjs from 'dayjs'

// 表格加载状态
const loading = ref(false)

// 分页配置
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true
})

// 表格列定义
const columns = [
  {
    title: '课程信息',
    dataIndex: 'course',
    key: 'courseName',
    slots: { customRender: 'courseName' }
  },
  {
    title: '评分',
    dataIndex: 'rating',
    key: 'rating',
    width: 180,
    slots: { customRender: 'rating' }
  },
  {
    title: '评价内容',
    dataIndex: 'content',
    key: 'content',
    slots: { customRender: 'content' }
  },
  {
    title: '评价时间',
    dataIndex: 'createdAt',
    key: 'time',
    width: 180,
    customRender: ({ text }) => text ? dayjs(text).format('YYYY-MM-DD HH:mm') : '-'
  },
  {
    title: '操作',
    key: 'action',
    width: 150,
    slots: { customRender: 'action' }
  }
]

// 评价数据
const reviews = ref([])

// 获取用户评价列表
const fetchReviews = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.current - 1,
      size: pagination.pageSize,
      rating: searchForm.rating,
      keyword: searchForm.keyword
    }
    
    const response = await api.getStudentReviews(params)
    reviews.value = response.reviews || []
    pagination.total = response.totalItems || 0
  } catch (error) {
    console.error('获取评价列表失败:', error)
    message.error('获取评价列表失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 搜索表单
const searchForm = reactive({
  keyword: '',
  rating: undefined
})

// 处理搜索
const handleSearch = () => {
  pagination.current = 1
  fetchReviews()
}

// 表格变化处理
const handleTableChange = (pag) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  fetchReviews()
}

// 编辑相关
const editModalVisible = ref(false)
const editLoading = ref(false)
const currentReview = ref(null)
const editForm = reactive({
  rating: 5,
  content: ''
})

// 打开编辑弹窗
const handleEdit = (record) => {
  currentReview.value = record
  editForm.rating = record.rating
  editForm.content = record.content
  editModalVisible.value = true
}

// 提交编辑
const submitEdit = async () => {
  if (!editForm.content.trim()) {
    message.error('请填写评价内容')
    return
  }

  editLoading.value = true
  try {
    // 调用更新评价API
    await api.updateReview(currentReview.value.id, {
      rating: editForm.rating,
      content: editForm.content
    })
    
    message.success('评价修改成功')
    editModalVisible.value = false
    
    // 重新获取列表
    fetchReviews()
  } catch (error) {
    console.error('更新评价失败:', error)
    message.error(error.response?.data?.message || '更新评价失败，请稍后重试')
  } finally {
    editLoading.value = false
  }
}

// 删除评价
const handleDelete = async (record) => {
  try {
    // 调用删除评价API
    await api.deleteReview(record.id)
    
    message.success('评价已删除')
    
    // 重新获取列表
    fetchReviews()
  } catch (error) {
    console.error('删除评价失败:', error)
    message.error(error.response?.data?.message || '删除评价失败，请稍后重试')
  }
}

onMounted(() => {
  fetchReviews()
})
</script>

<style lang="less" scoped>
@import "../assets/styles/variables.less";

.review-manage-container {
  padding: 24px;

  .review-manage-header {
    margin-bottom: 24px;

    h2 {
      margin-bottom: 8px;
    }

    p {
      color: @text-color-secondary;
      margin: 0;
    }
  }

  .search-section {
    background: white;
    padding: @padding-md;
    border-radius: @border-radius-base;
    margin-bottom: @padding-lg;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  }

  .course-info {
    display: flex;
    align-items: center;

    .course-cover {
      width: 60px;
      height: 60px;
      object-fit: cover;
      border-radius: 4px;
      margin-right: 12px;
    }

    .course-detail {
      .course-title {
        font-weight: 500;
        margin-bottom: 4px;
      }

      .course-teacher {
        color: @text-color-secondary;
        font-size: 12px;
      }
    }
  }
}
</style> 