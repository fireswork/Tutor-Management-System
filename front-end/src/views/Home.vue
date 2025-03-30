<template>
  <Layout>
    <div class="home-container container">
      <!-- 顶部横幅 -->
      <div class="banner animate__animated animate__fadeIn">
        <div class="banner-content">
          <h1 class="animate__animated animate__fadeInDown">家教服务管理系统</h1>
          <p class="animate__animated animate__fadeInUp animate__delay-1s">连接优质教师资源，提供个性化学习体验</p>
          <div class="banner-actions animate__animated animate__fadeInUp animate__delay-2s">
            <a-button type="primary" size="large" @click="goTo('TeacherList')">寻找教师</a-button>
            <a-button size="large" @click="goTo('Register')" class="register-btn">立即注册</a-button>
          </div>
        </div>
      </div>
      
      <!-- 平台优势 -->
      <div class="section advantages-section">
        <h2 class="section-title">平台优势</h2>
        <div class="advantages-list">
          <div class="advantage-item animate__animated animate__fadeInUp" v-for="(item, index) in advantages" :key="index">
            <div class="advantage-icon">
              <component :is="item.icon" />
            </div>
            <h3>{{ item.title }}</h3>
            <p>{{ item.desc }}</p>
          </div>
        </div>
      </div>
      
      <!-- 推荐教师 -->
      <div class="section teachers-section">
        <h2 class="section-title">推荐教师</h2>
        <div class="teachers-list">
          <a-row :gutter="[24, 24]">
            <a-col :xs="24" :sm="12" :md="8" :xl="6" v-for="(teacher, index) in featuredTeachers" :key="index">
              <a-card hoverable class="teacher-card animate__animated animate__fadeIn" @click="viewTeacherDetail(teacher.id)">
                <template #cover>
                  <img :src="teacher.avatar" :alt="teacher.name" class="teacher-avatar" />
                </template>
                <template #actions>
                  <span @click.stop="viewTeacherDetail(teacher.id)">查看详情</span>
                  <span @click.stop="handleContact(teacher)">联系老师</span>
                </template>
                <a-card-meta :title="teacher.name">
                  <template #description>
                    <div>
                      <p>{{ teacher.subject }} | {{ teacher.education }}</p>
                      <a-rate :value="teacher.rating" disabled allow-half />
                      <p class="teacher-bio">{{ teacher.bio }}</p>
                    </div>
                  </template>
                </a-card-meta>
              </a-card>
            </a-col>
          </a-row>
          <div class="more-btn-container">
            <a-button type="primary" @click="goTo('TeacherList')">查看更多教师</a-button>
          </div>
        </div>
      </div>
      
      <!-- 热门课程 -->
      <div class="section courses-section">
        <h2 class="section-title">热门课程</h2>
        <div class="courses-list">
          <a-row :gutter="[24, 24]">
            <a-col :xs="24" :sm="12" :md="8" :xl="6" v-for="(course, index) in featuredCourses" :key="index">
              <a-card hoverable class="course-card animate__animated animate__fadeIn">
                <template #cover>
                  <img :src="course.cover" :alt="course.title" class="course-cover" />
                </template>
                <a-card-meta :title="course.title">
                  <template #description>
                    <div>
                      <p>{{ course.teacherName }} | {{ course.category }}</p>
                      <p>
                        <span class="course-price">¥{{ course.price }}/课时</span>
                        <span class="course-duration">{{ course.duration }}分钟/课时</span>
                      </p>
                      <p class="course-desc">{{ course.description }}</p>
                    </div>
                  </template>
                </a-card-meta>
                <template #actions>
                  <span @click="viewCourseDetail(course.id)">查看详情</span>
                  <span @click="handleBookCourse(course)">立即预约</span>
                </template>
              </a-card>
            </a-col>
          </a-row>
          <div class="more-btn-container">
            <a-button type="primary" @click="goTo('CourseList')">查看更多课程</a-button>
          </div>
        </div>
      </div>
      
      <!-- 用户评价 -->
      <div class="section testimonials-section">
        <h2 class="section-title">用户评价</h2>
        <a-carousel autoplay class="testimonial-carousel">
          <div v-for="(testimonial, index) in testimonials" :key="index" class="testimonial-item">
            <div class="testimonial-content">
              <div class="testimonial-avatar">
                <img :src="testimonial.avatar" :alt="testimonial.name" />
              </div>
              <div class="testimonial-text">
                <p class="testimonial-quote">"{{ testimonial.quote }}"</p>
                <p class="testimonial-author">— {{ testimonial.name }}，{{ testimonial.role }}</p>
                <a-rate :value="testimonial.rating" disabled allow-half />
              </div>
            </div>
          </div>
        </a-carousel>
      </div>
      
      <!-- 联系我们 -->
      <div class="section contact-section">
        <h2 class="section-title">联系我们</h2>
        <div class="contact-container">
          <div class="contact-form">
            <a-form :model="contactForm" layout="vertical">
              <a-form-item label="姓名" name="name" :rules="[{ required: true, message: '请输入您的姓名' }]">
                <a-input v-model:value="contactForm.name" placeholder="请输入您的姓名" />
              </a-form-item>
              <a-form-item label="电子邮箱" name="email" :rules="[{ required: true, message: '请输入您的电子邮箱' }, { type: 'email', message: '请输入有效的电子邮箱' }]">
                <a-input v-model:value="contactForm.email" placeholder="请输入您的电子邮箱" />
              </a-form-item>
              <a-form-item label="电话" name="phone">
                <a-input v-model:value="contactForm.phone" placeholder="请输入您的电话" />
              </a-form-item>
              <a-form-item label="留言" name="message" :rules="[{ required: true, message: '请输入您的留言' }]">
                <a-textarea v-model:value="contactForm.message" :rows="4" placeholder="请输入您的留言" />
              </a-form-item>
              <a-form-item>
                <a-button type="primary" @click="submitContactForm">提交留言</a-button>
              </a-form-item>
            </a-form>
          </div>
          <div class="contact-info">
            <h3>联系方式</h3>
            <p><environment-outlined /> 地址：XXX省XXX市XXX区XXX街XXX号</p>
            <p><phone-outlined /> 电话：123-4567-8901</p>
            <p><mail-outlined /> 邮箱：contact@tutoringsystem.com</p>
            <h3 class="mt-4">关注我们</h3>
            <div class="social-icons">
              <a href="#" target="_blank"><wechat-outlined /></a>
              <a href="#" target="_blank"><weibo-outlined /></a>
              <a href="#" target="_blank"><qq-outlined /></a>
            </div>
          </div>
        </div>
      </div>
    </div>
  </Layout>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import Layout from '../components/Layout.vue'
import { 
  CheckCircleOutlined, 
  SafetyCertificateOutlined, 
  TeamOutlined, 
  RocketOutlined,
  EnvironmentOutlined,
  PhoneOutlined,
  MailOutlined,
  WechatOutlined,
  WeiboOutlined
} from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'

const router = useRouter()

// 页面跳转
const goTo = (name) => {
  router.push({ name })
}

// 平台优势数据
const advantages = [
  {
    icon: CheckCircleOutlined,
    title: '优质教师资源',
    desc: '严格筛选的教师团队，确保教学质量'
  },
  {
    icon: SafetyCertificateOutlined,
    title: '安全可靠',
    desc: '实名认证，保障学员和教师的安全'
  },
  {
    icon: TeamOutlined,
    title: '个性化教学',
    desc: '根据学员需求匹配合适的教师和课程'
  },
  {
    icon: RocketOutlined,
    title: '高效便捷',
    desc: '在线预约，随时随地学习'
  }
]

// 推荐教师数据
const featuredTeachers = [
  {
    id: 1,
    name: '张老师',
    avatar: 'https://picsum.photos/300/200?random=1',
    subject: '数学',
    education: '清华大学硕士',
    rating: 4.8,
    bio: '5年教学经验，擅长高中数学'
  },
  {
    id: 2,
    name: '李老师',
    avatar: 'https://picsum.photos/300/200?random=2',
    subject: '英语',
    education: '北京外国语大学',
    rating: 4.9,
    bio: '英语专业八级，3年海外工作经验'
  },
  {
    id: 3,
    name: '王老师',
    avatar: 'https://picsum.photos/300/200?random=3',
    subject: '物理',
    education: '北京大学博士',
    rating: 4.7,
    bio: '曾获全国物理竞赛金牌，教学经验丰富'
  },
  {
    id: 4,
    name: '赵老师',
    avatar: 'https://picsum.photos/300/200?random=4',
    subject: '化学',
    education: '复旦大学',
    rating: 4.6,
    bio: '化学专业毕业，擅长初高中化学教学'
  }
]

// 热门课程数据
const featuredCourses = [
  {
    id: 1,
    title: '高中数学强化班',
    cover: 'https://picsum.photos/300/200?random=5',
    teacherName: '张老师',
    category: '数学',
    price: 200,
    duration: 90,
    description: '针对高考数学，从基础到难题逐步突破'
  },
  {
    id: 2,
    title: '英语口语提升课程',
    cover: 'https://picsum.photos/300/200?random=6',
    teacherName: '李老师',
    category: '英语',
    price: 180,
    duration: 60,
    description: '地道英语口语培训，提升口语表达能力'
  },
  {
    id: 3,
    title: '物理概念强化班',
    cover: 'https://picsum.photos/300/200?random=7',
    teacherName: '王老师',
    category: '物理',
    price: 220,
    duration: 90,
    description: '物理基础概念讲解，培养物理思维'
  },
  {
    id: 4,
    title: '化学实验解析',
    cover: 'https://picsum.photos/300/200?random=8',
    teacherName: '赵老师',
    category: '化学',
    price: 190,
    duration: 120,
    description: '详解化学实验原理，提高实验技能'
  }
]

// 用户评价数据
const testimonials = [
  {
    name: '小明',
    role: '高三学生',
    avatar: 'https://picsum.photos/100/100?random=9',
    quote: '通过平台找到了非常优秀的数学老师，短时间内成绩有了明显提升，非常感谢！',
    rating: 5
  },
  {
    name: '小红',
    role: '初中生家长',
    avatar: 'https://picsum.photos/100/100?random=10',
    quote: '孩子在这里学习了半年，英语成绩从60分提升到了90分，老师非常负责任。',
    rating: 4.5
  },
  {
    name: '小刚',
    role: '大学生',
    avatar: 'https://picsum.photos/100/100?random=11',
    quote: '在这里担任家教老师已经一年多了，平台非常规范，给了我很多锻炼机会。',
    rating: 5
  }
]

// 联系表单
const contactForm = ref({
  name: '',
  email: '',
  phone: '',
  message: ''
})

// 查看教师详情
const viewTeacherDetail = (id) => {
  router.push({ name: 'TeacherDetail', params: { id } })
}

// 联系教师
const handleContact = (teacher) => {
  message.info(`即将联系${teacher.name}，请先登录`)
  // 实际项目中可以弹出登录窗口或跳转到登录页面
}

// 查看课程详情
const viewCourseDetail = (id) => {
  message.info(`查看课程ID: ${id}的详情`)
  // 实际项目中可以跳转到课程详情页
}

// 预约课程
const handleBookCourse = (course) => {
  message.info(`即将预约课程: ${course.title}，请先登录`)
  // 实际项目中可以弹出登录窗口或跳转到登录页面
}

// 提交联系表单
const submitContactForm = () => {
  message.success('您的留言已提交，我们会尽快回复您！')
  contactForm.value = {
    name: '',
    email: '',
    phone: '',
    message: ''
  }
}
</script>

<style lang="less" scoped>
@import '../assets/styles/variables.less';

.home-container {
  width: 100%;
  max-width: 100%;
  
  .banner {
    width: 100%;
    height: 300px;
    background: linear-gradient(rgba(0, 0, 0, 0.5), rgba(0, 0, 0, 0.5)), url('https://picsum.photos/1920/300?random=20');
    background-size: cover;
    background-position: center;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-bottom: @padding-lg;
    
    .banner-content {
      text-align: center;
      color: white;
      
      h1 {
        font-size: 32px;
        margin-bottom: @padding-sm;
      }
      
      p {
        font-size: 16px;
        margin-bottom: @padding-md;
      }
      
      .banner-actions {
        .register-btn {
          margin-left: @padding-sm;
        }
      }
    }
  }
  
  .section {
    width: 100%;
    margin-bottom: @padding-xl;
    
    .section-title {
      text-align: center;
      margin-bottom: @padding-lg;
      font-size: 24px;
      position: relative;
      
      &::after {
        content: '';
        display: block;
        width: 60px;
        height: 3px;
        background: @primary-color;
        margin: @padding-xs auto 0;
      }
    }
  }
  
  .advantages-section {
    .advantages-list {
      display: flex;
      flex-wrap: wrap;
      justify-content: space-between;
      
      .advantage-item {
        width: 100%;
        max-width: 24%;
        padding: @padding-md;
        text-align: center;
        background: white;
        border-radius: @border-radius-base;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
        
        .advantage-icon {
          font-size: 36px;
          color: @primary-color;
          margin-bottom: @padding-sm;
        }
        
        h3 {
          margin-bottom: @padding-xs;
        }
        
        p {
          color: @text-color-secondary;
        }
      }
    }
  }
  
  .teachers-section, .courses-section {
    .teacher-card, .course-card {
      height: 100%;
      transition: transform 0.3s ease;
      
      &:hover {
        transform: translateY(-5px);
      }
      
      .teacher-avatar, .course-cover {
        height: 200px;
        object-fit: cover;
      }
      
      .teacher-bio, .course-desc {
        margin-top: @padding-xs;
        color: @text-color-secondary;
        overflow: hidden;
        text-overflow: ellipsis;
        display: -webkit-box;
        -webkit-line-clamp: 2;
        -webkit-box-orient: vertical;
      }
      
      .course-price {
        color: @error-color;
        font-weight: bold;
      }
      
      .course-duration {
        margin-left: @padding-sm;
        color: @text-color-secondary;
      }
    }
    
    .more-btn-container {
      text-align: center;
      margin-top: @padding-lg;
    }
  }
  
  .testimonials-section {
    background: #f5f7fa;
    padding: @padding-lg 0;
    
    .testimonial-carousel {
      width: 100%;
      max-width: 800px;
      margin: 0 auto;
      
      .testimonial-item {
        height: 200px;
        padding: @padding-lg;
        
        .testimonial-content {
          display: flex;
          align-items: center;
          height: 100%;
          
          .testimonial-avatar {
            flex: 0 0 100px;
            margin-right: @padding-lg;
            
            img {
              width: 100px;
              height: 100px;
              border-radius: 50%;
              object-fit: cover;
            }
          }
          
          .testimonial-text {
            flex: 1;
            
            .testimonial-quote {
              font-size: 16px;
              font-style: italic;
              margin-bottom: @padding-sm;
            }
            
            .testimonial-author {
              font-weight: bold;
              margin-bottom: @padding-xs;
            }
          }
        }
      }
    }
  }
  
  .contact-section {
    .contact-container {
      display: flex;
      flex-wrap: wrap;
      
      .contact-form {
        flex: 1;
        min-width: 300px;
        padding-right: @padding-lg;
      }
      
      .contact-info {
        flex: 0 0 300px;
        padding: @padding-lg;
        background: #f5f7fa;
        border-radius: @border-radius-base;
        
        h3 {
          margin-bottom: @padding-sm;
        }
        
        p {
          margin-bottom: @padding-xs;
        }
        
        .mt-4 {
          margin-top: @padding-md;
        }
        
        .social-icons {
          font-size: 24px;
          
          a {
            margin-right: @padding-md;
            color: @text-color;
            
            &:hover {
              color: @primary-color;
            }
          }
        }
      }
    }
  }
}

@media (max-width: 768px) {
  .home-container {
    .advantages-section {
      .advantages-list {
        .advantage-item {
          max-width: 48%;
          margin-bottom: @padding-md;
        }
      }
    }
    
    .contact-section {
      .contact-container {
        .contact-form {
          flex: 1 1 100%;
          padding-right: 0;
          margin-bottom: @padding-lg;
        }
        
        .contact-info {
          flex: 1 1 100%;
        }
      }
    }
  }
}

@media (max-width: 576px) {
  .home-container {
    .advantages-section {
      .advantages-list {
        .advantage-item {
          max-width: 100%;
        }
      }
    }
  }
}
</style> 