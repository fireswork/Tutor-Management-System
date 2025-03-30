<template>
  <div class="order-list-container container animate__animated animate__fadeIn">
    <div class="order-list-header">
      <div class="header-content">
        <div>
          <h2>订单管理</h2>
          <p>{{ userRole === 'student' ? '查看您的课程预约记录' : '管理您的课程预约订单' }}</p>
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
            v-model:value="searchForm.status"
            style="width: 120px"
            placeholder="订单状态"
            allowClear
            @change="handleSearch"
          >
            <a-select-option value="pending">待支付</a-select-option>
            <a-select-option value="paid">已支付</a-select-option>
            <a-select-option value="completed">已完成</a-select-option>
            <a-select-option value="cancelled">已取消</a-select-option>
          </a-select>
        </a-form-item>
      </a-form>
    </div>

    <!-- 订单列表 -->
    <a-table
      :dataSource="orders"
      :columns="columns"
      :pagination="pagination"
      @change="handleTableChange"
      :loading="loading"
      bordered
    >
      <!-- 课程信息 -->
      <template #courseName="{ record }">
        <div class="course-info">
          <img :src="record.course.cover" :alt="record.course.title" class="course-cover" />
          <div class="course-detail">
            <div class="course-title">{{ record.course.title }}</div>
            <div class="course-teacher">教师：{{ record.course.teacherName }}</div>
          </div>
        </div>
      </template>

      <!-- 订单状态 -->
      <template #status="{ record }">
        <a-tag :color="getStatusColor(record.status)">
          {{ getStatusText(record.status) }}
        </a-tag>
      </template>

      <!-- 订单金额 -->
      <template #amount="{ record }">
        <span class="amount">¥{{ record.amount }}</span>
      </template>

      <!-- 评价信息 -->
      <template #review="{ record }">
        <template v-if="record.status === 'completed'">
          <template v-if="record.hasReview">
            <div class="review-info">
              <a-rate :value="record.review.rating" disabled allow-half />
              <a-button type="link" size="small" @click="viewReview(record)">
                查看评价
              </a-button>
            </div>
          </template>
          <span v-else class="no-review">未评价</span>
        </template>
        <span v-else>-</span>
      </template>

      <!-- 操作按钮 -->
      <template #action="{ record }">
        <!-- 学生操作 -->
        <template v-if="userRole === 'student'">
          <a-space>
            <!-- 待支付状态可以支付或取消 -->
            <template v-if="record.status === 'pending'">
              <a-button type="primary" size="small" @click="handlePay(record)">
                支付
              </a-button>
              <a-button size="small" @click="handleCancel(record)">
                取消预约
              </a-button>
            </template>
            <!-- 已完成状态且未评价可以评价 -->
            <template v-if="record.status === 'completed' && !record.hasReview">
              <a-button type="primary" size="small" @click="handleReview(record)">
                评价课程
              </a-button>
            </template>
          </a-space>
        </template>

        <!-- 教师操作 -->
        <template v-if="userRole === 'teacher'">
          <a-space>
            <!-- 已支付状态可以完成或取消 -->
            <template v-if="record.status === 'paid'">
              <a-button type="primary" size="small" @click="handleComplete(record)">
                完成课程
              </a-button>
              <a-button danger size="small" @click="handleCancel(record)">
                取消课程
              </a-button>
            </template>
          </a-space>
        </template>
      </template>
    </a-table>

    <!-- 评价弹窗 -->
    <a-modal
      v-model:visible="reviewModalVisible"
      title="课程评价"
      @ok="submitReview"
      :confirmLoading="reviewLoading"
    >
      <a-form :model="reviewForm" layout="vertical">
        <a-form-item label="评分">
          <a-rate v-model:value="reviewForm.rating" allow-half />
        </a-form-item>
        <a-form-item label="评价内容">
          <a-textarea
            v-model:value="reviewForm.content"
            :rows="4"
            placeholder="请分享您的学习体验"
          />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 添加评价查看弹窗 -->
    <a-modal
      v-model:visible="viewReviewModalVisible"
      title="课程评价详情"
      :footer="null"
    >
      <template v-if="currentOrder?.review">
        <div class="review-detail">
          <div class="review-rating">
            <span class="label">评分：</span>
            <a-rate :value="currentOrder.review.rating" disabled allow-half />
          </div>
          <div class="review-content">
            <div class="label">评价内容：</div>
            <p>{{ currentOrder.review.content }}</p>
          </div>
          <div class="review-time">
            <span class="label">评价时间：</span>
            <span>{{ currentOrder.review.time }}</span>
          </div>
        </div>
      </template>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'

// 用户角色
const userRole = ref('student') // 实际项目中从用户状态获取

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

// 订单状态定义
const orderStatus = {
  pending: { color: 'orange', text: '待支付' },
  paid: { color: 'blue', text: '已支付' },
  completed: { color: 'green', text: '已完成' },
  cancelled: { color: 'red', text: '已取消' }
}

// 获取状态颜色
const getStatusColor = (status) => orderStatus[status]?.color || 'default'

// 获取状态文本
const getStatusText = (status) => orderStatus[status]?.text || '未知状态'

// 表格列定义
const columns = [
  {
    title: '课程信息',
    dataIndex: 'courseName',
    key: 'courseName',
    slots: { customRender: 'courseName' }
  },
  {
    title: '预约时间',
    dataIndex: 'bookingTime',
    key: 'bookingTime'
  },
  {
    title: '订单金额',
    dataIndex: 'amount',
    key: 'amount',
    slots: { customRender: 'amount' }
  },
  {
    title: '订单状态',
    dataIndex: 'status',
    key: 'status',
    slots: { customRender: 'status' }
  },
  {
    title: '评价',
    key: 'review',
    slots: { customRender: 'review' }
  },
  {
    title: '操作',
    key: 'action',
    slots: { customRender: 'action' }
  }
]

// 模拟订单数据
const orders = ref([
  {
    id: 1,
    course: {
      title: '高中数学强化班',
      cover: 'https://picsum.photos/100/100?random=1',
      teacherName: '张老师'
    },
    bookingTime: '2024-03-15 14:30',
    amount: 200,
    status: 'pending',
    hasReview: false
  },
  {
    id: 2,
    course: {
      title: '英语口语提升课程',
      cover: 'https://picsum.photos/100/100?random=2',
      teacherName: '李老师'
    },
    bookingTime: '2024-03-14 10:00',
    amount: 180,
    status: 'paid',
    hasReview: false
  },
  {
    id: 3,
    course: {
      title: '物理概念强化班',
      cover: 'https://picsum.photos/100/100?random=3',
      teacherName: '王老师'
    },
    bookingTime: '2024-03-13 16:00',
    amount: 220,
    status: 'completed',
    hasReview: false
  }
])

// 表格变化处理
const handleTableChange = (pag) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  // 实际项目中这里调用API重新获取数据
}

// 支付处理
const handlePay = (record) => {
  // 实际项目中跳转到支付页面或调用支付接口
  message.success('正在跳转到支付页面...')
}

// 取消订单
const handleCancel = (record) => {
  record.status = 'cancelled'
  message.success('订单已取消')
}

// 完成订单
const handleComplete = (record) => {
  record.status = 'completed'
  message.success('课程已完成')
}

// 评价相关
const reviewModalVisible = ref(false)
const reviewLoading = ref(false)
const currentOrder = ref(null)
const reviewForm = reactive({
  rating: 5,
  content: ''
})

// 打开评价弹窗
const handleReview = (record) => {
  currentOrder.value = record
  reviewModalVisible.value = true
}

// 提交评价
const submitReview = () => {
  if (!reviewForm.content.trim()) {
    message.error('请填写评价内容')
    return
  }

  reviewLoading.value = true
  // 模拟提交评价
  setTimeout(() => {
    if (currentOrder.value) {
      currentOrder.value.hasReview = true
      currentOrder.value.review = {
        rating: reviewForm.rating,
        content: reviewForm.content,
        time: new Date().toLocaleString()
      }
      message.success('评价提交成功')
      reviewModalVisible.value = false
      reviewForm.content = ''
      reviewForm.rating = 5
    }
    reviewLoading.value = false
  }, 1000)
}

// 搜索表单
const searchForm = reactive({
  keyword: '',
  status: undefined,
})

// 处理搜索
const handleSearch = () => {
  pagination.current = 1
  // 实际项目中这里调用API重新获取数据
  // 模拟搜索过滤
  const filteredOrders = orders.value.filter(order => {
    const matchKeyword = searchForm.keyword ? 
      (order.course.title.includes(searchForm.keyword) || 
       order.course.teacherName.includes(searchForm.keyword)) : true
    
    const matchStatus = searchForm.status ? 
      order.status === searchForm.status : true

    return matchKeyword && matchStatus
  })

  orders.value = filteredOrders
}

// 评价查看相关
const viewReviewModalVisible = ref(false)

// 查看评价详情
const viewReview = (record) => {
  currentOrder.value = record
  viewReviewModalVisible.value = true
}

onMounted(() => {
  // 实际项目中这里调用API获取订单列表
})
</script>

<style lang="less" scoped>
@import "../assets/styles/variables.less";

.order-list-container {
  padding: 24px;

  .order-list-header {
    margin-bottom: 24px;

    h2 {
      margin-bottom: 8px;
    }

    p {
      color: @text-color-secondary;
      margin: 0;
    }
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

  .amount {
    color: @primary-color;
    font-weight: 500;
  }

  .search-section {
    background: white;
    padding: @padding-md;
    border-radius: @border-radius-base;
    margin-bottom: @padding-lg;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  }

  .review-info {
    display: flex;
    align-items: center;
    gap: 8px;

    .ant-rate {
      font-size: 12px;
    }
  }

  .no-review {
    color: @text-color-secondary;
  }

  .review-detail {
    .label {
      color: @text-color-secondary;
      margin-right: 8px;
    }

    .review-rating {
      margin-bottom: 16px;
    }

    .review-content {
      margin-bottom: 16px;

      p {
        margin-top: 8px;
        white-space: pre-wrap;
      }
    }

    .review-time {
      color: @text-color-secondary;
      font-size: @font-size-sm;
    }
  }
}
</style> 