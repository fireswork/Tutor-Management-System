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
          <img :src="record.courseCover" alt="课程封面" class="course-cover" />
          <div class="course-detail">
            <div class="course-title">{{ record.courseTitle }}</div>
            <div class="course-teacher">教师：{{ record.teacherName }}</div>
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
        <template v-if="userRole === 'user'">
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
            <!-- 已支付状态用户可以完成订单 -->
            <template v-if="record.status === 'paid' && userRole === 'teacher'">
              <a-button type="primary" size="small" @click="handleComplete(record)">
                完成订单
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
            <!-- 待支付状态教师可以取消 -->
            <template v-if="record.status === 'pending'">
              <a-button danger size="small" @click="handleCancel(record)">
                取消预约
              </a-button>
            </template>
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
import { api } from '../utils/axios'
import dayjs from 'dayjs'

// 用户角色
const userRole = ref(localStorage.getItem('userRole').toLowerCase() || 'user')

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
    dataIndex: 'course',
    key: 'courseName',
    slots: { customRender: 'courseName' }
  },
  {
    title: '预约时间',
    dataIndex: 'bookingTime',
    key: 'bookingTime',
    customRender: ({text}) => text ? dayjs(text).format('YYYY-MM-DD HH:mm') : '-'
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

// 订单数据
const orders = ref([])

// 获取订单列表
const fetchOrders = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.current - 1,
      size: pagination.pageSize,
      status: searchForm.status,
      keyword: searchForm.keyword
    }
    
    let response
    if (userRole.value === 'teacher') {
      response = await api.getTeacherOrders(params)
    } else {
      response = await api.getStudentOrders(params)
    }
    
    orders.value = response.orders || []
    pagination.total = response.totalItems || 0
  } catch (error) {
    console.error('获取订单列表失败:', error)
    message.error('获取订单列表失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 表格变化处理
const handleTableChange = (pag) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  fetchOrders()
}

// 支付订单 - 添加模拟支付功能
const handlePay = async (record) => {
  try {
    // 显示支付确认对话框
    const showPaymentDialog = () => {
      return new Promise((resolve) => {
        const modal = message.loading({
          content: '订单支付处理中...',
          duration: 0,
        });
        
        // 3秒后自动关闭
        setTimeout(() => {
          modal();
          resolve();
        }, 3000);
      });
    };
    
    await showPaymentDialog();
    await api.payOrder(record.id);
    message.success('订单支付成功');
    fetchOrders();
  } catch (error) {
    console.error('支付订单失败:', error);
    message.error(error.response?.data?.message || '支付订单失败，请稍后重试');
  }
}

// 取消订单
const handleCancel = async (record) => {
  try {
    const reason = ''  // 这里可以添加一个输入框让用户填写取消原因
    await api.cancelOrder(record.id, reason)
    message.success('订单已取消')
    fetchOrders()
  } catch (error) {
    console.error('取消订单失败:', error)
    message.error(error.response?.data?.message || '取消订单失败，请稍后重试')
  }
}

// 完成订单
const handleComplete = async (record) => {
  try {
    await api.completeOrder(record.id)
    message.success('课程已完成')
    fetchOrders()
  } catch (error) {
    console.error('完成订单失败:', error)
    message.error(error.response?.data?.message || '完成订单失败，请稍后重试')
  }
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
  reviewForm.rating = 5
  reviewForm.content = ''
  reviewModalVisible.value = true
}

// 提交评价
const submitReview = async () => {
  if (!reviewForm.content.trim()) {
    message.error('请填写评价内容')
    return
  }

  reviewLoading.value = true
  try {
    const reviewData = {
      orderId: currentOrder.value.id,
      rating: reviewForm.rating,
      content: reviewForm.content
    }
    
    await api.createReview(reviewData)
    message.success('评价提交成功')
    reviewModalVisible.value = false
    fetchOrders()
  } catch (error) {
    console.error('提交评价失败:', error)
    message.error(error.response?.data?.message || '提交评价失败，请稍后重试')
  } finally {
    reviewLoading.value = false
  }
}

// 搜索表单
const searchForm = reactive({
  keyword: '',
  status: undefined,
})

// 处理搜索
const handleSearch = () => {
  pagination.current = 1
  fetchOrders()
}

// 评价查看相关
const viewReviewModalVisible = ref(false)

// 查看评价详情
const viewReview = async (record) => {
  try {
    if (record.hasReview) {
      const reviewData = await api.getOrderReview(record.id)
      currentOrder.value = {
        ...record,
        review: {
          ...reviewData,
          time: new Date(reviewData.createdAt).toLocaleString()
        }
      }
      viewReviewModalVisible.value = true
    }
  } catch (error) {
    console.error('获取评价详情失败:', error)
    message.error('获取评价详情失败，请稍后重试')
  }
}

onMounted(() => {
  fetchOrders()
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