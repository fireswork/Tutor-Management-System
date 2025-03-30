<template>
  <Layout>
    <div class="teacher-detail-container animate__animated animate__fadeIn" v-if="teacher">
      <!-- 返回按钮 -->
      <div class="back-button">
        <a-button @click="goBack">
          <template #icon><left-outlined /></template>
          返回列表
        </a-button>
      </div>
      
      <!-- 教师信息卡 -->
      <a-row :gutter="24" class="teacher-info-section">
        <a-col :xs="24" :md="8" class="left-column">
          <div class="teacher-avatar animate__animated animate__fadeInLeft">
            <img :src="teacher.avatar" :alt="teacher.name" />
          </div>
          
          <div class="teacher-contact animate__animated animate__fadeInLeft animate__delay-1s">
            <a-button type="primary" block @click="handleContactTeacher">
              <template #icon><message-outlined /></template>
              联系老师
            </a-button>
            <a-button style="margin-top: 16px" block @click="handleBookLesson">
              <template #icon><calendar-outlined /></template>
              预约课程
            </a-button>
          </div>
        </a-col>
        
        <a-col :xs="24" :md="16" class="right-column">
          <div class="teacher-basic-info animate__animated animate__fadeInRight">
            <div class="teacher-name-rating">
              <h1>{{ teacher.name }}</h1>
              <div class="teacher-rating">
                <a-rate :value="teacher.rating" disabled allow-half />
                <span class="rating-text">{{ teacher.rating }}分</span>
                <span class="student-count">({{ teacher.studentCount }}名学生)</span>
              </div>
            </div>
            
            <div class="teacher-tags">
              <a-tag color="blue">{{ teacher.subject }}</a-tag>
              <a-tag color="green">{{ teacher.education }}</a-tag>
              <a-tag color="orange">{{ teacher.experience }}年经验</a-tag>
              <a-tag color="purple" v-for="(tag, index) in teacher.tags" :key="index">{{ tag }}</a-tag>
            </div>
            
            <div class="teacher-price">
              <span class="price-label">课时费：</span>
              <span class="price-value">¥{{ teacher.priceRange[0] }} - {{ teacher.priceRange[1] }}</span>
              <span class="price-unit">元/小时</span>
            </div>
            
            <div class="teacher-description">
              <p>{{ teacher.description }}</p>
            </div>
          </div>
          
          <a-divider />
          
          <!-- 教师详细信息 -->
          <div class="teacher-details animate__animated animate__fadeInRight animate__delay-1s">
            <h2>教师资料</h2>
            
            <a-descriptions :column="{ xxl: 2, xl: 2, lg: 2, md: 2, sm: 1, xs: 1 }">
              <a-descriptions-item label="教授科目">{{ teacher.subjects.join(', ') }}</a-descriptions-item>
              <a-descriptions-item label="教学年限">{{ teacher.experience }}年</a-descriptions-item>
              <a-descriptions-item label="学历背景">{{ teacher.education }}</a-descriptions-item>
              <a-descriptions-item label="授课区域">{{ teacher.teachingArea.join(', ') }}</a-descriptions-item>
              <a-descriptions-item label="授课方式">{{ teacher.teachingMethods.join(', ') }}</a-descriptions-item>
              <a-descriptions-item label="可授课时间">{{ teacher.availableTime }}</a-descriptions-item>
            </a-descriptions>
          </div>
          
          <a-divider />
          
          <!-- 教师介绍 -->
          <div class="teacher-introduction animate__animated animate__fadeInRight animate__delay-2s">
            <h2>自我介绍</h2>
            <p>{{ teacher.introduction }}</p>
          </div>
          
          <a-divider />
          
          <!-- 教学经历 -->
          <div class="teacher-experience-section animate__animated animate__fadeInRight animate__delay-3s">
            <h2>教学经历</h2>
            <a-timeline>
              <a-timeline-item v-for="(exp, index) in teacher.experiences" :key="index">
                <template #dot>
                  <trophy-outlined v-if="exp.highlight" style="font-size: 16px;" />
                </template>
                <div class="experience-item">
                  <h3>{{ exp.title }}</h3>
                  <p class="experience-time">{{ exp.period }}</p>
                  <p>{{ exp.description }}</p>
                </div>
              </a-timeline-item>
            </a-timeline>
          </div>
          
          <a-divider />
          
          <!-- 学生评价 -->
          <div class="teacher-reviews animate__animated animate__fadeInRight animate__delay-4s">
            <div class="reviews-header">
              <h2>学生评价</h2>
              <a-button type="link" @click="showAllReviews">查看全部 {{ teacher.reviews.length }} 条评价</a-button>
            </div>
            
            <div class="reviews-content">
              <a-list
                itemLayout="horizontal"
                :dataSource="displayedReviews"
              >
                <template #renderItem="{ item }">
                  <a-list-item>
                    <a-comment
                      :author="item.author"
                      :avatar="item.avatar"
                      :content="item.content"
                      :datetime="item.datetime"
                    >
                      <template #actions>
                        <span>
                          <a-rate :value="item.rating" disabled allow-half />
                        </span>
                      </template>
                    </a-comment>
                  </a-list-item>
                </template>
              </a-list>
            </div>
          </div>
        </a-col>
      </a-row>
      
      <!-- 推荐教师 -->
      <div class="recommended-teachers animate__animated animate__fadeIn animate__delay-5s">
        <h2>您可能还喜欢</h2>
        <a-row :gutter="[24, 24]">
          <a-col :xs="24" :sm="12" :md="8" :xl="6" v-for="(rec, index) in recommendedTeachers" :key="rec.id">
            <a-card hoverable class="teacher-card" @click="viewTeacherDetail(rec.id)">
              <template #cover>
                <img :src="rec.avatar" :alt="rec.name" class="recommended-avatar" />
              </template>
              <a-card-meta :title="rec.name">
                <template #description>
                  <div>
                    <div>
                      <a-tag color="blue">{{ rec.subject }}</a-tag>
                      <a-rate :value="rec.rating" disabled allow-half style="font-size: 12px;" />
                    </div>
                    <p class="recommended-bio">{{ rec.bio }}</p>
                  </div>
                </template>
              </a-card-meta>
            </a-card>
          </a-col>
        </a-row>
      </div>
    </div>
    
    <div v-else class="loading-container">
      <a-spin size="large" />
    </div>
    
    <!-- 预约课程抽屉 -->
    <a-drawer
      title="预约课程"
      :visible="bookingDrawerVisible"
      @close="closeBookingDrawer"
      width="400"
    >
      <div class="booking-drawer-content">
        <h3>{{ teacher?.name }} - 课程预约</h3>
        
        <a-form :model="bookingForm" layout="vertical">
          <a-form-item label="选择科目" name="subject">
            <a-select v-model:value="bookingForm.subject" placeholder="请选择科目">
              <a-select-option v-for="subject in teacher?.subjects" :key="subject" :value="subject">
                {{ subject }}
              </a-select-option>
            </a-select>
          </a-form-item>
          
          <a-form-item label="选择日期" name="date">
            <a-date-picker 
              v-model:value="bookingForm.date" 
              style="width: 100%" 
              :disabledDate="disabledDate"
            />
          </a-form-item>
          
          <a-form-item label="选择时间段" name="timeSlot">
            <a-select v-model:value="bookingForm.timeSlot" placeholder="请选择时间段">
              <a-select-option v-for="slot in availableTimeSlots" :key="slot" :value="slot">
                {{ slot }}
              </a-select-option>
            </a-select>
          </a-form-item>
          
          <a-form-item label="课时数" name="hours">
            <a-input-number v-model:value="bookingForm.hours" :min="1" :max="10" style="width: 100%" />
          </a-form-item>
          
          <a-form-item label="备注" name="remark">
            <a-textarea v-model:value="bookingForm.remark" placeholder="请输入备注信息，如教学需求、重点等" :rows="4" />
          </a-form-item>
          
          <a-form-item>
            <a-button type="primary" @click="submitBooking" block :disabled="!isLoggedIn" :loading="bookingSubmitting">
              {{ isLoggedIn ? '确认预约' : '请先登录' }}
            </a-button>
            
            <a-button v-if="!isLoggedIn" type="link" block @click="goToLogin">
              去登录
            </a-button>
          </a-form-item>
        </a-form>
      </div>
    </a-drawer>
    
    <!-- 评价模态框 -->
    <a-modal
      title="全部评价"
      :visible="reviewModalVisible"
      @cancel="closeReviewModal"
      footer={null}
      width="700px"
    >
      <a-list
        itemLayout="horizontal"
        :dataSource="teacher?.reviews"
        :pagination="{ pageSize: 5 }"
      >
        <template #renderItem="{ item }">
          <a-list-item>
            <a-comment
              :author="item.author"
              :avatar="item.avatar"
              :content="item.content"
              :datetime="item.datetime"
            >
              <template #actions>
                <span>
                  <a-rate :value="item.rating" disabled allow-half />
                </span>
              </template>
            </a-comment>
          </a-list-item>
        </template>
      </a-list>
    </a-modal>
  </Layout>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import Layout from '../components/Layout.vue'
import { 
  LeftOutlined, 
  MessageOutlined, 
  CalendarOutlined, 
  TrophyOutlined
} from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'

const router = useRouter()
const route = useRoute()
const teacherId = ref(parseInt(route.params.id))
const teacher = ref(null)
const isLoggedIn = ref(!!localStorage.getItem('token'))

// 抽屉和模态框状态
const bookingDrawerVisible = ref(false)
const reviewModalVisible = ref(false)
const bookingSubmitting = ref(false)

// 预约表单
const bookingForm = reactive({
  subject: '',
  date: null,
  timeSlot: '',
  hours: 1,
  remark: ''
})

// 显示的评论（默认显示前3条）
const displayedReviews = computed(() => {
  if (!teacher.value?.reviews) return []
  return teacher.value.reviews.slice(0, 3)
})

// 可选时间段
const availableTimeSlots = [
  '08:00-10:00',
  '10:00-12:00',
  '14:00-16:00',
  '16:00-18:00',
  '19:00-21:00'
]

// 推荐教师
const recommendedTeachers = ref([
  {
    id: 5,
    name: '钱老师',
    avatar: 'https://picsum.photos/300/200?random=5',
    subject: '语文',
    rating: 4.9,
    bio: '资深语文教师，教学方法活泼生动，注重阅读与写作能力培养。'
  },
  {
    id: 8,
    name: '吴老师',
    avatar: 'https://picsum.photos/300/200?random=8',
    subject: '地理',
    rating: 4.6,
    bio: '地理学专业，擅长地理知识教学和地理思维培养。'
  },
  {
    id: 11,
    name: '陈老师',
    avatar: 'https://picsum.photos/300/200?random=11',
    subject: '英语',
    rating: 4.7,
    bio: '英语专业，拥有丰富的海外教学经验，擅长英语口语和写作教学。'
  },
  {
    id: 12,
    name: '楚老师',
    avatar: 'https://picsum.photos/300/200?random=12',
    subject: '音乐',
    rating: 5.0,
    bio: '钢琴和声乐双专业，曾获多项音乐大赛奖项，教学经验丰富。'
  }
])

// 获取教师详情
const fetchTeacherDetail = () => {
  // 模拟API请求
  setTimeout(() => {
    // 根据ID匹配教师数据
    teacher.value = {
      id: teacherId.value,
      name: '王老师',
      avatar: 'https://picsum.photos/300/200?random=3',
      subject: '物理',
      subjects: ['物理', '数学'],
      education: '北京大学博士',
      experience: 7,
      rating: 4.7,
      studentCount: 156,
      description: '物理博士，有丰富的高中物理教学经验，擅长深入浅出地讲解物理概念和原理，帮助学生建立物理思维。',
      introduction: '我毕业于北京大学物理系，师从著名物理学家，主要研究方向为理论物理。在校期间，曾获得多项物理竞赛奖项，并参与多项国家级科研项目。毕业后致力于中学物理教育，希望能够通过浅显易懂的方式让学生爱上物理，理解物理学的美妙。我相信每个学生都有学好物理的潜力，只是需要找到适合自己的学习方法。',
      priceRange: [200, 400],
      teachingArea: ['海淀区', '朝阳区', '西城区', '线上教学'],
      teachingMethods: ['一对一', '小班教学', '线上教学'],
      availableTime: '周一至周五晚上，周末全天',
      tags: ['竞赛指导', '概念讲解', '中高考押题'],
      experiences: [
        {
          title: '北京XX中学物理教师',
          period: '2018年至今',
          description: '任教高中物理，负责高三毕业班教学工作，连续3年高考成绩居校首位。',
          highlight: true
        },
        {
          title: '全国物理竞赛指导教师',
          period: '2019年至今',
          description: '培养多名学生获得全国物理竞赛省级一等奖，其中2名学生获得全国一等奖。',
          highlight: true
        },
        {
          title: '北京大学物理系助教',
          period: '2015年-2018年',
          description: '担任本科生物理实验课助教，负责实验指导和答疑工作。',
          highlight: false
        }
      ],
      reviews: [
        {
          author: '张同学',
          avatar: 'https://picsum.photos/64/64?random=20',
          content: '王老师的物理课非常生动，通过实验和比喻让我理解了很多以前难以理解的概念，成绩从60分提升到了95分，非常感谢！',
          datetime: '2023-12-15',
          rating: 5
        },
        {
          author: '李家长',
          avatar: 'https://picsum.photos/64/64?random=21',
          content: '孩子原来非常讨厌物理，跟了王老师半年后，现在每天都主动学习物理，期中考试还得了全班第一，老师教学方法确实很厉害。',
          datetime: '2023-10-20',
          rating: 5
        },
        {
          author: '赵同学',
          avatar: 'https://picsum.photos/64/64?random=22',
          content: '王老师能够把复杂的知识点讲得特别通俗易懂，而且很耐心。在他的帮助下，我顺利通过了高考物理。',
          datetime: '2023-08-10',
          rating: 4.5
        },
        {
          author: '刘家长',
          avatar: 'https://picsum.photos/64/64?random=23',
          content: '王老师很负责任，每次课后都会留作业并及时批改，还会针对孩子的薄弱环节进行重点辅导。',
          datetime: '2023-07-15',
          rating: 4.8
        },
        {
          author: '陈同学',
          avatar: 'https://picsum.photos/64/64?random=24',
          content: '原本物理是我的弱项，但在王老师的指导下，我在最近的物理竞赛中获得了省级二等奖，真的很感谢王老师！',
          datetime: '2023-05-25',
          rating: 5
        }
      ]
    }
  }, 800)
}

// 禁用已过日期
const disabledDate = (current) => {
  return current && current < dayjs().startOf('day')
}

// 返回上一页
const goBack = () => {
  router.back()
}

// 前往登录页
const goToLogin = () => {
  router.push({ 
    name: 'Login', 
    query: { redirect: route.fullPath } 
  })
}

// 联系教师
const handleContactTeacher = () => {
  if (!isLoggedIn.value) {
    message.info('请先登录后再联系教师')
    goToLogin()
    return
  }
  
  message.success(`即将为您联系${teacher.value.name}`)
}

// 预约课程
const handleBookLesson = () => {
  if (teacher.value) {
    bookingForm.subject = teacher.value.subjects[0]
  }
  bookingDrawerVisible.value = true
}

// 关闭预约抽屉
const closeBookingDrawer = () => {
  bookingDrawerVisible.value = false
  // 重置表单
  bookingForm.subject = teacher.value?.subjects[0] || ''
  bookingForm.date = null
  bookingForm.timeSlot = ''
  bookingForm.hours = 1
  bookingForm.remark = ''
}

// 提交预约
const submitBooking = () => {
  if (!isLoggedIn.value) {
    message.info('请先登录后再预约课程')
    return
  }
  
  if (!bookingForm.subject || !bookingForm.date || !bookingForm.timeSlot) {
    message.error('请填写完整的预约信息')
    return
  }
  
  bookingSubmitting.value = true
  
  // 模拟提交请求
  setTimeout(() => {
    bookingSubmitting.value = false
    message.success('预约成功！教师会尽快与您确认课程安排')
    closeBookingDrawer()
  }, 1500)
}

// 显示所有评价
const showAllReviews = () => {
  reviewModalVisible.value = true
}

// 关闭评价模态框
const closeReviewModal = () => {
  reviewModalVisible.value = false
}

// 查看其他教师详情
const viewTeacherDetail = (id) => {
  router.push({ name: 'TeacherDetail', params: { id } })
}

// 页面加载时获取教师详情
onMounted(() => {
  fetchTeacherDetail()
})
</script>

<style lang="less" scoped>
@import '../assets/styles/variables.less';

.teacher-detail-container {
  .back-button {
    margin-bottom: @padding-md;
  }
  
  .teacher-info-section {
    margin-bottom: @padding-lg;
    
    .left-column {
      margin-bottom: @padding-lg;
      
      .teacher-avatar {
        background: white;
        padding: @padding-sm;
        border-radius: @border-radius-base;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
        margin-bottom: @padding-lg;
        
        img {
          width: 100%;
          object-fit: cover;
          border-radius: @border-radius-base;
        }
      }
      
      .teacher-contact {
        background: white;
        padding: @padding-md;
        border-radius: @border-radius-base;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
      }
    }
    
    .right-column {
      .teacher-basic-info {
        background: white;
        padding: @padding-lg;
        border-radius: @border-radius-base;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
        
        .teacher-name-rating {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: @padding-md;
          flex-wrap: wrap;
          
          h1 {
            font-size: 24px;
            margin-bottom: 0;
            margin-right: @padding-md;
          }
          
          .teacher-rating {
            display: flex;
            align-items: center;
            
            .rating-text {
              margin: 0 @padding-xs;
              color: @warning-color;
              font-weight: bold;
            }
            
            .student-count {
              color: @text-color-secondary;
            }
          }
        }
        
        .teacher-tags {
          margin-bottom: @padding-md;
          
          .ant-tag {
            margin-bottom: @padding-xs;
          }
        }
        
        .teacher-price {
          margin-bottom: @padding-md;
          
          .price-label {
            color: @text-color-secondary;
          }
          
          .price-value {
            font-size: 18px;
            color: #ff4d4f;
            font-weight: bold;
            margin: 0 @padding-xs;
          }
          
          .price-unit {
            color: @text-color-secondary;
          }
        }
        
        .teacher-description {
          color: @text-color;
          
          p {
            margin-bottom: 0;
          }
        }
      }
      
      .teacher-details, 
      .teacher-introduction, 
      .teacher-experience-section, 
      .teacher-reviews {
        background: white;
        padding: @padding-lg;
        border-radius: @border-radius-base;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
        
        h2 {
          font-size: 18px;
          margin-bottom: @padding-md;
          position: relative;
          
          &:after {
            content: '';
            display: block;
            width: 30px;
            height: 2px;
            background: @primary-color;
            margin-top: 8px;
          }
        }
      }
      
      .teacher-experience-section {
        .experience-item {
          h3 {
            font-size: 16px;
            margin-bottom: @padding-xs;
          }
          
          .experience-time {
            color: @text-color-secondary;
            margin-bottom: @padding-xs;
          }
        }
      }
      
      .teacher-reviews {
        .reviews-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: @padding-md;
        }
      }
    }
  }
  
  .recommended-teachers {
    margin-top: @padding-lg * 2;
    
    h2 {
      font-size: 20px;
      margin-bottom: @padding-lg;
      text-align: center;
      position: relative;
      
      &:after {
        content: '';
        display: block;
        width: 40px;
        height: 2px;
        background: @primary-color;
        margin: 8px auto 0;
      }
    }
    
    .teacher-card {
      transition: transform 0.3s ease;
      
      &:hover {
        transform: translateY(-5px);
      }
      
      .recommended-avatar {
        height: 200px;
        object-fit: cover;
      }
      
      .recommended-bio {
        margin-top: @padding-xs;
        color: @text-color-secondary;
        overflow: hidden;
        text-overflow: ellipsis;
        display: -webkit-box;
        -webkit-line-clamp: 2;
        -webkit-box-orient: vertical;
      }
    }
  }
}

.loading-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 400px;
}

@media (max-width: 768px) {
  .teacher-detail-container {
    .teacher-info-section {
      .right-column {
        .teacher-name-rating {
          flex-direction: column;
          align-items: flex-start;
          
          h1 {
            margin-bottom: @padding-xs;
          }
        }
      }
    }
  }
}
</style> 