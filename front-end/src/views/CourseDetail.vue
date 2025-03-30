<template>
    <div class="course-detail-page container animate__animated animate__fadeIn">
      <!-- 课程封面区域 -->
      <div class="cover-section">
        <div class="cover-wrapper">
          <img :src="course?.cover" :alt="course?.title" />
        </div>
      </div>
  
      <div class="content-wrapper">
        <!-- 课程基本信息 -->
        <div class="course-info section-card">
          <div class="info-header">
            <div class="title-section">
              <h1>{{ course?.title }}</h1>
              <div class="tags">
                <a-tag color="blue">{{ course?.category }}</a-tag>
                <a-tag color="green">{{ course?.level }}</a-tag>
                <a-tag color="orange">{{ course?.duration }}分钟/课时</a-tag>
                <a-tag color="purple">已报名 {{ course?.studentCount }} 人</a-tag>
              </div>
            </div>
            <div class="price-section">
              <div class="price">¥{{ course?.price }}</div>
              <div class="unit">/课时</div>
            </div>
          </div>
          
          <div class="info-description">
            <h2>课程简介</h2>
            <p>{{ course?.description }}</p>
          </div>
        </div>
  
        <!-- 教师信息 -->
        <div class="teacher-info section-card">
          <h2>授课教师</h2>
          <div class="teacher-header">
            <a-avatar :size="80" :src="teacherAvatar" />
            <div class="teacher-basic">
              <div class="name-rating">
                <h3>{{ course?.teacherName }}</h3>
                <a-rate :value="course?.rating" disabled allow-half />
              </div>
              <p class="stats">教龄 {{ teacherYears }} 年 | 已授课 {{ teacherCourseCount }} 节</p>
            </div>
          </div>
  
          <!-- 教师资质 -->
          <div class="teacher-qualifications">
            <h3>教师资质</h3>
            <a-card>
              <a-descriptions :column="{ xs: 1, sm: 2 }">
                <a-descriptions-item label="教师资格">
                  <a-tag color="green">已认证</a-tag>
                </a-descriptions-item>
                <a-descriptions-item label="学历">
                  {{ teacherQualifications.education }}
                </a-descriptions-item>
                <a-descriptions-item label="专业">
                  {{ teacherQualifications.major }}
                </a-descriptions-item>
                <a-descriptions-item label="证书">
                  <div class="certificates">
                    <a-tag v-for="cert in teacherQualifications.certificates" 
                          :key="cert" 
                          color="blue">{{ cert }}</a-tag>
                  </div>
                </a-descriptions-item>
              </a-descriptions>
            </a-card>
          </div>
        </div>
  
        <!-- 学生评价 -->
        <div class="student-reviews section-card">
          <h2>学生评价</h2>
          <a-list
            :data-source="teacherReviews"
            :pagination="{ pageSize: 3 }"
          >
            <template #renderItem="{ item }">
              <a-list-item>
                <a-comment
                  :author="item.studentName"
                  :content="item.content"
                  :datetime="item.datetime"
                >
                  <template #avatar>
                    <a-avatar :src="item.avatar" />
                  </template>
                  <template #actions>
                    <a-rate :value="item.rating" disabled allow-half />
                  </template>
                </a-comment>
              </a-list-item>
            </template>
          </a-list>
        </div>
  
        <!-- 预约按钮 -->
        <div class="float-buttons">
          <a-float-button-group shape="circle" style="right: 24px">
            <a-float-button @click="router.back()" tooltip="返回上一页">
                <template #icon>
                    <LeftOutlined />
                </template>
            </a-float-button>
            <a-float-button
              type="primary"
              @click="handleBookCourse"
              tooltip="预约课程"
            >
                <template #icon>
                    <CalendarOutlined />
                </template>
            </a-float-button>
          </a-float-button-group>
        </div>
      </div>
    </div>
  </template>
  
  <script setup>
  import { ref, onMounted } from 'vue'
  import { useRoute, useRouter } from 'vue-router'
  import { message } from 'ant-design-vue'
  import { 
    LeftOutlined, 
    CalendarOutlined 
  } from '@ant-design/icons-vue'
  
  const route = useRoute()
  const router = useRouter()
  
  // 模拟课程数据获取
  const course = ref(null)
  
  // 模拟教师相关数据
  const teacherAvatar = 'https://picsum.photos/200'
  const teacherYears = 5
  const teacherCourseCount = 1000
  const teacherQualifications = {
    education: '研究生学历',
    major: '数学教育',
    certificates: [
      '高级教师资格证',
      '心理咨询师证书',
      '教育学硕士'
    ]
  }
  
  const teacherReviews = [
    {
      studentName: '张同学',
      avatar: 'https://picsum.photos/200?random=1',
      content: '老师讲课非常细致，对知识点的讲解很透彻，让我对这门课程有了更深的理解。',
      datetime: '2024-03-10',
      rating: 5
    },
    {
      studentName: '李同学',
      avatar: 'https://picsum.photos/200?random=2',
      content: '课程内容安排合理，老师的教学方法很适合我，学习效果明显。',
      datetime: '2024-03-09',
      rating: 4.5
    },
    {
      studentName: '王同学',
      avatar: 'https://picsum.photos/200?random=3',
      content: '老师很有耐心，针对我的问题都会详细解答，让我学习更有信心。',
      datetime: '2024-03-08',
      rating: 5
    },
    {
      studentName: '赵同学',
      avatar: 'https://picsum.photos/200?random=4',
      content: '课程质量很高，老师的专业水平很强，讲解方式也很生动。',
      datetime: '2024-03-07',
      rating: 4.5
    }
  ]
  
  // 处理预约课程
  const handleBookCourse = () => {
    if (!localStorage.getItem('token')) {
      message.info('请先登录后再预约课程')
      router.push({ 
        name: 'Login', 
        query: { redirect: route.fullPath }
      })
      return
    }
    
    message.success(`即将为您预约课程: ${course.value.title}`)
  }
  
  onMounted(async () => {
    const courseId = route.params.id
    // 这里应该调用API获取课程详情
    // 暂时使用模拟数据
    course.value = {
      id: courseId,
      title: "高中数学强化班",
      cover: "https://picsum.photos/1200/400",
      teacherName: "张老师",
      category: "数学",
      level: "高级",
      price: 200,
      duration: 90,
      rating: 4.8,
      studentCount: 156,
      description: "针对高考数学，从基础到难题逐步突破，提高解题能力和思维水平。课程采用循序渐进的教学方法，结合实际例题，帮助学生建立数学思维体系。",
    }
  })
  </script>
  
  <style lang="less" scoped>
  @import "../assets/styles/variables.less";
  
  .course-detail-page {
    min-height: 100vh;
    background: #f5f5f5;
    padding-bottom: 10px;
  
    .cover-section {
      width: 100%;
      height: 400px;
      background: #000;
      margin-bottom: 24px;
  
      .cover-wrapper {
        width: 100%;
        height: 100%;
        
        img {
          width: 100%;
          height: 100%;
          object-fit: cover;
          opacity: 0.8;
        }
      }
    }
  
    .content-wrapper {
      max-width: 1000px;
      margin: 0 auto;
      padding: 0 16px;
  
      .section-card {
        background: white;
        border-radius: 8px;
        padding: 24px;
        margin-bottom: 24px;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
      }
  
      .course-info {
        .info-header {
          display: flex;
          justify-content: space-between;
          align-items: flex-start;
          margin-bottom: 24px;
  
          .title-section {
            flex: 1;
  
            h1 {
              font-size: 28px;
              margin: 0 0 16px;
              color: @heading-color;
            }
  
            .tags {
              .ant-tag {
                margin: 4px 8px 4px 0;
                padding: 4px 8px;
              }
            }
          }
  
          .price-section {
            text-align: right;
            
            .price {
              font-size: 32px;
              color: @primary-color;
              font-weight: bold;
            }
  
            .unit {
              color: @text-color-secondary;
            }
          }
        }
  
        .info-description {
          h2 {
            font-size: 20px;
            margin-bottom: 16px;
            color: @heading-color;
          }
  
          p {
            color: @text-color;
            line-height: 1.8;
            margin: 0;
          }
        }
      }
  
      .teacher-info {
        h2 {
          font-size: 20px;
          margin-bottom: 24px;
          color: @heading-color;
        }
  
        .teacher-header {
          display: flex;
          align-items: center;
          margin-bottom: 24px;
  
          .teacher-basic {
            margin-left: 24px;
            flex: 1;
  
            .name-rating {
              display: flex;
              align-items: center;
              margin-bottom: 8px;
  
              h3 {
                font-size: 18px;
                margin: 0 16px 0 0;
              }
            }
  
            .stats {
              color: @text-color-secondary;
              margin: 0;
            }
          }
        }
  
        .teacher-qualifications {
          h3 {
            font-size: 18px;
            margin-bottom: 16px;
            color: @heading-color;
          }
  
          .certificates {
            display: flex;
            flex-wrap: wrap;
            gap: 8px;
          }
        }
      }
  
      .student-reviews {
        h2 {
          font-size: 20px;
          margin-bottom: 24px;
          color: @heading-color;
        }
  
        .ant-list-pagination {
          text-align: center;
          margin-top: 24px;
        }
      }
    }
  }
  
  @media (max-width: 768px) {
    .course-detail-page {
      .cover-section {
        height: 250px;
      }
  
      .content-wrapper {
        .course-info {
          .info-header {
            flex-direction: column;
  
            .price-section {
              margin-top: 16px;
              text-align: left;
            }
          }
        }
  
        .teacher-info {
          .teacher-header {
            flex-direction: column;
            text-align: center;
  
            .teacher-basic {
              margin: 16px 0 0;
  
              .name-rating {
                justify-content: center;
              }
            }
          }
        }
      }
    }
  }
  
  .float-buttons {
    position: fixed;
    right: 24px;
    bottom: 24px;
    z-index: 100;
  }
  </style>