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
              <a-tag color="orange">{{ course?.duration }}分钟/课时</a-tag>
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
            <p class="stats">教龄 {{ teacherYears }} 年</p>
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
                {{ teacherInfo.education }}
              </a-descriptions-item>
              <a-descriptions-item label="专业">
                {{ teacherInfo.major }}
              </a-descriptions-item>
              <a-descriptions-item label="证书">
                <div class="certificates">
                  <a-tag
                    v-for="cert in teacherQualifications.certificates"
                    :key="cert"
                    color="blue"
                    >{{ cert || "未上传" }}</a-tag
                  >
                  <a-button
                    type="link"
                    size="small"
                    @click="showQualificationsModal"
                  >
                    <template #icon><EyeOutlined /></template>
                    查看详情
                  </a-button>
                </div>
              </a-descriptions-item>
            </a-descriptions>
          </a-card>
        </div>
      </div>

      <!-- 学生评价 -->
      <div class="student-reviews section-card">
        <h2>学生评价</h2>
        <a-list :data-source="courseReviews" :pagination="{ pageSize: 3 }" :loading="reviewsLoading">
          <template #renderItem="{ item }">
            <a-list-item>
              <a-comment
                :author="item.studentName"
                :content="item.content"
                :datetime="dayjs(item.createdAt).format('YYYY-MM-DD HH:mm')"
              >
                <template #avatar>
                  <a-avatar>{{ item.studentName.charAt(0) }}</a-avatar>
                </template>
                <template #actions>
                  <a-rate :value="item.rating" disabled allow-half />
                </template>
              </a-comment>
            </a-list-item>
          </template>
          <template #empty>
            <div class="empty-data">
              <a-empty description="暂无评价数据" />
            </div>
          </template>
        </a-list>
      </div>

      <!-- 资质详情弹窗 -->
      <a-modal
        v-model:visible="qualificationsModalVisible"
        title="教师资质详情"
        width="700px"
        :footer="null"
      >
        <div v-if="!teacherQualificationsData.length" class="empty-data">
          <a-empty description="暂无资质数据" />
        </div>
        <div v-else class="qualifications-grid">
          <a-card
            v-for="(qual, index) in teacherQualificationsData"
            :key="index"
            class="qualification-card"
          >
            <template #cover>
              <div class="image-container">
                <img
                  v-if="qual.fileUrl"
                  :src="qual.fileUrl"
                  :alt="qual.title"
                  class="qualification-image"
                />
                <a-empty v-else description="暂无图片" />
              </div>
            </template>
            <div class="card-extra-info">
              <p>发证机构: {{ qual.issuer }}</p>
              <p>
                状态:
                <a-tag :color="getStatusColor(qual.status)">{{
                  getStatusText(qual.status.toLowerCase())
                }}</a-tag>
              </p>
            </div>
          </a-card>
        </div>
      </a-modal>

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
import { ref, onMounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import { message } from "ant-design-vue";
import { api } from "../utils/axios";
import {
  LeftOutlined,
  CalendarOutlined,
  EyeOutlined,
} from "@ant-design/icons-vue";
import dayjs from "dayjs";

const route = useRoute();
const router = useRouter();

// 课程数据
const course = ref(null);
const teacherQualifications = ref({
  education: "",
  major: "",
  certificates: [],
});

const teacherInfo = ref({});

// 模拟教师相关数据
const teacherAvatar = "https://picsum.photos/200";
const teacherYears = 5;

// 课程评价
const courseReviews = ref([]);
const reviewsLoading = ref(false);

// 获取课程详情
const fetchCourseDetail = async (id) => {
  try {
    const response = await api.getCourseDetail(id);
    course.value = response.course;
    teacherInfo.value = response.teacherInfo;

    // 存储完整的教师资质数据，用于弹窗展示
    teacherQualificationsData.value = response.teacherQualifications || [];

    // 处理教师资质信息
    const qualifications = response.teacherQualifications || [];
    teacherQualifications.value = {
      education:
        qualifications.find((q) => q.type === "EDUCATION")?.title || "未填写",
      major: qualifications.find((q) => q.type === "MAJOR")?.title || "未填写",
      certificates: qualifications
        .filter((q) => q.type === "CERTIFICATE")
        .map((q) => q.title),
    };
    
    // 获取课程评价
    fetchCourseReviews(id);
  } catch (error) {
    console.error("获取课程详情失败:", error);
    message.error("获取课程详情失败，请稍后重试");
  }
};

// 获取课程评价
const fetchCourseReviews = async (courseId) => {
  reviewsLoading.value = true;
  try {
    const response = await api.getCourseReviews(courseId, { page: 0, size: 5 });
    courseReviews.value = response.reviews || [];
  } catch (error) {
    console.error("获取课程评价失败:", error);
    message.error("获取课程评价失败");
  } finally {
    reviewsLoading.value = false;
  }
};

// 资质详情弹窗
const qualificationsModalVisible = ref(false);
const teacherQualificationsData = ref([]);

// 显示资质详情弹窗
const showQualificationsModal = () => {
  qualificationsModalVisible.value = true;
};

// 获取状态文本
const getStatusText = (status) => {
  const statusMap = {
    pending: "待审核",
    approved: "已通过",
    rejected: "已拒绝",
  };
  return statusMap[status] || status;
};

// 获取状态颜色
const getStatusColor = (status) => {
  const colorMap = {
    pending: "orange",
    approved: "green",
    rejected: "red",
  };
  return colorMap[status] || "default";
};

// 处理预约课程
const handleBookCourse = async () => {
  if (!localStorage.getItem("token")) {
    message.info("请先登录后再预约课程");
    router.push({
      name: "Login",
      query: { redirect: route.fullPath },
    });
    return;
  }

  const orderData = {
    courseId: course.value.id,
    bookingTime: new Date().toISOString(),
  };

  await api.createOrder(orderData);
  message.success(`预约课程成功: ${course.title}，请去订单管理支付对应的订单`);
};

onMounted(() => {
  const courseId = route.params.id;
  fetchCourseDetail(courseId);
});
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

.qualifications-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 16px;

  .qualification-card {
    margin-bottom: 16px;

    .image-container {
      height: 200px;
      display: flex;
      align-items: center;
      justify-content: center;
      overflow: hidden;
      background-color: #f5f5f5;

      .qualification-image {
        width: 100%;
        height: 100%;
        object-fit: contain;
      }
    }

    .card-extra-info {
      margin-top: 12px;

      p {
        margin-bottom: 4px;
      }
    }
  }
}

.empty-data {
  padding: 40px 0;
  text-align: center;
}
</style>
