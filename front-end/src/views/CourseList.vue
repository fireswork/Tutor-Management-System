<template>
  <div
    class="course-list-container container animate__animated animate__fadeIn"
  >
    <div class="course-list-header">
      <div class="header-content">
        <div>
          <h2>浏览课程</h2>
          <p>探索各类优质课程，找到适合您的学习方案</p>
        </div>
        <!-- 教师角色显示发布课程按钮 -->
        <a-button
          v-if="userRole === 'teacher'"
          type="primary"
          @click="showPublishModal"
        >
          <plus-outlined />
          发布新课程
        </a-button>
      </div>
    </div>

    <!-- 筛选区域 -->
    <div class="filter-section">
      <a-form layout="inline" :model="filterForm">
        <a-form-item>
          <a-input-search
            v-model:value="filterForm.keyword"
            placeholder="搜索课程名称、教师"
            style="width: 250px"
            @search="handleFilterChange"
            @change="handleInputChange"
          />
        </a-form-item>
        <a-form-item>
          <a-select
            v-model:value="filterForm.category"
            style="width: 120px"
            placeholder="选择科目"
            allowClear
            @change="handleFilterChange"
          >
            <a-select-option
              v-for="category in categories"
              :key="category"
              :value="category"
            >
              {{ category }}
            </a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item>
          <a-select
            v-model:value="filterForm.level"
            style="width: 120px"
            placeholder="课程难度"
            allowClear
            @change="handleFilterChange"
          >
            <a-select-option value="初级">初级</a-select-option>
            <a-select-option value="中级">中级</a-select-option>
            <a-select-option value="高级">高级</a-select-option>
          </a-select>
        </a-form-item>
      </a-form>
    </div>

    <!-- 课程列表 -->
    <div class="course-list-content">
      <a-row :gutter="[24, 24]">
        <a-col
          :xs="24"
          :sm="12"
          :md="8"
          :xl="6"
          v-for="(course, index) in filteredCourses"
          :key="course.id"
        >
          <a-card
            hoverable
            class="course-card animate__animated animate__fadeIn"
          >
            <template #cover>
              <div class="course-cover">
                <img :src="course.cover" :alt="course.title" />
                <div class="course-pricing">
                  <span>¥{{ course.price }}/课时</span>
                </div>
              </div>
            </template>

            <a-card-meta :title="course.title">
              <template #description>
                <div class="course-info">
                  <div class="course-tags">
                    <a-tag color="blue">{{ course.category }}</a-tag>
                    <a-tag color="green">{{ course.level }}</a-tag>
                    <a-tag color="orange">{{ course.duration }}分钟/课时</a-tag>
                    <div class="course-status">
                      <a-tag
                        :color="
                          course.status === 'pending'
                            ? 'orange'
                            : course.status === 'approved'
                            ? 'green'
                            : 'red'
                        "
                      >
                        {{
                          course.status === "pending"
                            ? "待审核"
                            : course.status === "approved"
                            ? "已通过"
                            : "已拒绝"
                        }}
                      </a-tag>
                    </div>
                  </div>
                  <div class="course-teacher">
                    <span>教师：{{ course.teacherName }}</span>
                    <a-rate :value="course.rating" disabled allow-half />
                  </div>
                  <a-tooltip :title="course.description">
                    <p class="course-desc">{{ course.description }}</p>
                  </a-tooltip>
                </div>
              </template>
            </a-card-meta>

            <template #actions>
              <!-- 管理员操作按钮 -->
              <template
                v-if="userRole === 'admin' && course.status === 'pending'"
              >
                <a-button type="link" @click="handleAudit(course, 'approved')">
                  <template #icon><CheckCircleOutlined /></template>
                  通过
                </a-button>
                <a-button
                  type="link"
                  danger
                  @click="handleAudit(course, 'rejected')"
                >
                  <template #icon><CloseCircleOutlined /></template>
                  拒绝
                </a-button>
              </template>

              <!-- 非管理员操作按钮 -->
              <template v-else>
                <a-button type="link" @click="viewCourseDetail(course.id)">
                  查看详情
                </a-button>
                <a-button
                  type="link"
                  @click="handleBookCourse(course)"
                  :disabled="course.status !== 'approved'"
                >
                  立即预约
                </a-button>
              </template>
            </template>
          </a-card>
        </a-col>
      </a-row>

      <!-- 空状态 -->
      <a-empty
        v-if="filteredCourses.length === 0"
        description="未找到符合条件的课程"
      />

      <!-- 分页 -->
      <div class="pagination-container">
        <a-pagination
          v-model:current="pagination.current"
          :total="totalCourses"
          :pageSize="pagination.pageSize"
          show-size-changer
          show-quick-jumper
          :show-total="(total) => `共 ${total} 条记录`"
          @change="handlePageChange"
          @showSizeChange="handleSizeChange"
        />
      </div>
    </div>

    <!-- 发布课程的Modal -->
    <a-modal
      v-model:visible="publishModalVisible"
      title="发布新课程"
      :width="700"
      @ok="handlePublishCourse"
      :confirmLoading="publishLoading"
    >
      <a-form
        :model="publishForm"
        :rules="publishRules"
        ref="publishFormRef"
        layout="vertical"
      >
        <a-form-item label="课程名称" name="title" required>
          <a-input
            v-model:value="publishForm.title"
            placeholder="请输入课程名称"
          />
        </a-form-item>

        <a-form-item label="课程分类" name="category" required>
          <a-select
            v-model:value="publishForm.category"
            placeholder="请选择课程分类"
          >
            <a-select-option
              v-for="category in categories"
              :key="category"
              :value="category"
            >
              {{ category }}
            </a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item label="课程难度" name="level" required>
          <a-select
            v-model:value="publishForm.level"
            placeholder="请选择课程难度"
          >
            <a-select-option value="初级">初级</a-select-option>
            <a-select-option value="中级">中级</a-select-option>
            <a-select-option value="高级">高级</a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item label="课时时长(分钟)" name="duration" required>
          <a-input-number
            v-model:value="publishForm.duration"
            :min="30"
            :max="180"
            :step="15"
            style="width: 100%"
          />
        </a-form-item>

        <a-form-item label="课时价格(元)" name="price" required>
          <a-input-number
            v-model:value="publishForm.price"
            :min="0"
            :precision="2"
            style="width: 100%"
          />
        </a-form-item>

        <a-form-item label="课程封面" name="cover" required>
          <a-upload
            v-model:file-list="coverFileList"
            list-type="picture-card"
            :before-upload="beforeUpload"
            @preview="handlePreview"
            :maxCount="1"
          >
            <div v-if="coverFileList.length < 1">
              <plus-outlined />
              <div style="margin-top: 8px">上传</div>
            </div>
          </a-upload>
        </a-form-item>

        <a-form-item label="课程简介" name="description" required>
          <a-textarea
            v-model:value="publishForm.description"
            :rows="4"
            placeholder="请输入课程简介"
          />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 图片预览Modal -->
    <a-modal v-model:visible="previewVisible" title="预览图片" :footer="null">
      <img alt="example" style="width: 100%" :src="previewImage" />
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from "vue";
import { useRouter } from "vue-router";
import {
  PlusOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined,
} from "@ant-design/icons-vue";
import { message } from "ant-design-vue";

const router = useRouter();

// 筛选表单数据
const filterForm = reactive({
  category: undefined,
  level: undefined,
  keyword: "",
});

// 分页配置
const pagination = reactive({
  current: 1,
  pageSize: 8,
});

// 可选科目
const categories = [
  "语文",
  "数学",
  "英语",
  "物理",
  "化学",
  "生物",
  "历史",
  "地理",
  "政治",
  "音乐",
  "美术",
  "体育",
  "计算机",
];

// 模拟课程数据
const allCourses = ref([
  {
    id: 1,
    title: "高中数学强化班",
    cover: "https://picsum.photos/300/200?random=101",
    teacherName: "张老师",
    teacherId: 1,
    category: "数学",
    level: "高级",
    price: 200,
    duration: 90,
    rating: 4.8,
    studentCount: 156,
    description: "针对高考数学，从基础到难题逐步突破，提高解题能力和思维水平。",
    status: "pending",
  },
  {
    id: 2,
    title: "英语口语提升课程",
    cover: "https://picsum.photos/300/200?random=102",
    teacherName: "李老师",
    teacherId: 2,
    category: "英语",
    level: "中级",
    price: 180,
    duration: 60,
    rating: 4.9,
    studentCount: 204,
    description:
      "地道英语口语培训，提升口语表达能力，着重培养日常交流和应试能力。",
  },
  {
    id: 3,
    title: "物理概念强化班",
    cover: "https://picsum.photos/300/200?random=103",
    teacherName: "王老师",
    teacherId: 3,
    category: "物理",
    level: "中级",
    price: 220,
    duration: 90,
    rating: 4.7,
    studentCount: 132,
    description: "物理基础概念讲解，培养物理思维，掌握解题技巧和方法。",
  },
  {
    id: 4,
    title: "化学实验解析",
    cover: "https://picsum.photos/300/200?random=104",
    teacherName: "赵老师",
    teacherId: 4,
    category: "化学",
    level: "高级",
    price: 190,
    duration: 120,
    rating: 4.6,
    studentCount: 98,
    description: "详解化学实验原理，提高实验操作技能，加深对化学反应的理解。",
  },
  {
    id: 5,
    title: "初中语文阅读写作",
    cover: "https://picsum.photos/300/200?random=105",
    teacherName: "钱老师",
    teacherId: 5,
    category: "语文",
    level: "初级",
    price: 160,
    duration: 90,
    rating: 4.9,
    studentCount: 175,
    description: "提高阅读理解能力，培养写作思维和技巧，打好语文基础。",
  },
  {
    id: 6,
    title: "中国历史文化精讲",
    cover: "https://picsum.photos/300/200?random=106",
    teacherName: "孙老师",
    teacherId: 6,
    category: "历史",
    level: "中级",
    price: 150,
    duration: 90,
    rating: 4.5,
    studentCount: 142,
    description: "系统讲解中国历史发展脉络，深入了解重要历史事件和人物。",
  },
  {
    id: 7,
    title: "生物学奥赛预备班",
    cover: "https://picsum.photos/300/200?random=107",
    teacherName: "周老师",
    teacherId: 7,
    category: "生物",
    level: "高级",
    price: 240,
    duration: 120,
    rating: 4.7,
    studentCount: 86,
    description: "针对生物学奥林匹克竞赛，培养科学思维和实验能力。",
  },
  {
    id: 8,
    title: "世界地理知识总览",
    cover: "https://picsum.photos/300/200?random=108",
    teacherName: "吴老师",
    teacherId: 8,
    category: "地理",
    level: "中级",
    price: 170,
    duration: 90,
    rating: 4.6,
    studentCount: 124,
    description: "全面了解世界各地地理知识，掌握地理学基本原理和方法。",
  },
  {
    id: 9,
    title: "政治思想与时事分析",
    cover: "https://picsum.photos/300/200?random=109",
    teacherName: "郑老师",
    teacherId: 9,
    category: "政治",
    level: "高级",
    price: 180,
    duration: 90,
    rating: 4.8,
    studentCount: 112,
    description: "结合当前时事热点，深入分析政治理论，提高思辨能力。",
  },
  {
    id: 10,
    title: "数学竞赛技巧与方法",
    cover: "https://picsum.photos/300/200?random=110",
    teacherName: "冯老师",
    teacherId: 10,
    category: "数学",
    level: "高级",
    price: 250,
    duration: 120,
    rating: 4.9,
    studentCount: 78,
    description: "针对各类数学竞赛，讲解解题思路和技巧，培养数学思维。",
  },
  {
    id: 11,
    title: "商务英语写作与口语",
    cover: "https://picsum.photos/300/200?random=111",
    teacherName: "陈老师",
    teacherId: 11,
    category: "英语",
    level: "高级",
    price: 220,
    duration: 90,
    rating: 4.7,
    studentCount: 146,
    description: "针对商务场景的英语写作和口语训练，提高职场英语水平。",
  },
  {
    id: 12,
    title: "钢琴基础入门",
    cover: "https://picsum.photos/300/200?random=112",
    teacherName: "楚老师",
    teacherId: 12,
    category: "音乐",
    level: "初级",
    price: 280,
    duration: 60,
    rating: 5.0,
    studentCount: 94,
    description: "从零开始学习钢琴，掌握基本的演奏技巧和乐理知识。",
  },
  {
    id: 13,
    title: "初中数学思维培养",
    cover: "https://picsum.photos/300/200?random=113",
    teacherName: "张老师",
    teacherId: 1,
    category: "数学",
    level: "初级",
    price: 180,
    duration: 90,
    rating: 4.8,
    studentCount: 187,
    description: "培养数学思维，打好初中数学基础，提高解题能力。",
  },
  {
    id: 14,
    title: "英语阅读理解技巧",
    cover: "https://picsum.photos/300/200?random=114",
    teacherName: "李老师",
    teacherId: 2,
    category: "英语",
    level: "中级",
    price: 160,
    duration: 90,
    rating: 4.7,
    studentCount: 165,
    description: "提高英语阅读理解能力，掌握解题技巧，提升阅读速度。",
  },
  {
    id: 15,
    title: "高中化学专题突破",
    cover: "https://picsum.photos/300/200?random=115",
    teacherName: "赵老师",
    teacherId: 4,
    category: "化学",
    level: "高级",
    price: 200,
    duration: 90,
    rating: 4.8,
    studentCount: 105,
    description: "针对高中化学难点专题进行深入讲解，突破学习瓶颈。",
  },
  {
    id: 16,
    title: "小学语文阅读写作启蒙",
    cover: "https://picsum.photos/300/200?random=116",
    teacherName: "钱老师",
    teacherId: 5,
    category: "语文",
    level: "初级",
    price: 140,
    duration: 60,
    rating: 4.9,
    studentCount: 206,
    description: "培养阅读兴趣和写作能力，打好语文基础，激发学习热情。",
  },
]);

// 处理筛选条件变化
const handleFilterChange = () => {
  pagination.current = 1;
  // 这里可以调用API获取数据
  // fetchCourseList()
};

// 处理输入框变化（使用防抖）
const handleInputChange = debounce((e) => {
  handleFilterChange();
}, 500);

// 防抖函数
function debounce(fn, delay) {
  let timer = null;
  return function (...args) {
    if (timer) clearTimeout(timer);
    timer = setTimeout(() => {
      fn.apply(this, args);
    }, delay);
  };
}

// 修改 filteredCourses computed 属性
const filteredCourses = computed(() => {
  let result = [...allCourses.value];

  // 应用筛选条件
  if (filterForm.category) {
    result = result.filter((course) => course.category === filterForm.category);
  }

  if (filterForm.level) {
    result = result.filter((course) => course.level === filterForm.level);
  }

  if (filterForm.keyword) {
    const keyword = filterForm.keyword.toLowerCase();
    result = result.filter(
      (course) =>
        course.title.toLowerCase().includes(keyword) ||
        course.teacherName.toLowerCase().includes(keyword) ||
        course.description.toLowerCase().includes(keyword)
    );
  }

  // 分页处理
  const startIndex = (pagination.current - 1) * pagination.pageSize;
  const endIndex = startIndex + pagination.pageSize;

  return result.slice(startIndex, endIndex);
});

// 修改 totalCourses computed 属性
const totalCourses = computed(() => {
  let result = [...allCourses.value];

  if (filterForm.category) {
    result = result.filter((course) => course.category === filterForm.category);
  }

  if (filterForm.level) {
    result = result.filter((course) => course.level === filterForm.level);
  }

  if (filterForm.keyword) {
    const keyword = filterForm.keyword.toLowerCase();
    result = result.filter(
      (course) =>
        course.title.toLowerCase().includes(keyword) ||
        course.teacherName.toLowerCase().includes(keyword) ||
        course.description.toLowerCase().includes(keyword)
    );
  }

  return result.length;
});

// 页面变化处理
const handlePageChange = (page, pageSize) => {
  pagination.current = page;
  pagination.pageSize = pageSize;
};

// 每页条数变化处理
const handleSizeChange = (current, size) => {
  pagination.current = 1;
  pagination.pageSize = size;
};

// 查看课程详情
const viewCourseDetail = (id) => {
  router.push({
    name: "CourseDetail",
    params: { id: id },
  });
};

// 预约课程
const handleBookCourse = (course) => {
  if (!localStorage.getItem("token")) {
    message.info("请先登录后再预约课程");
    router.push({
      name: "Login",
      query: { redirect: router.currentRoute.value.fullPath },
    });
    return;
  }

  message.success(`即将为您预约课程: ${course.title}`);
  // 实际项目中可以弹出预约表单或跳转到预约页面
};

// 用户角色（实际项目中应从用户状态或vuex中获取）
const userRole = ref("teacher"); // 临时写死为teacher便于测试

// 发布课程相关
const publishModalVisible = ref(false);
const publishLoading = ref(false);
const publishFormRef = ref(null);
const coverFileList = ref([]);
const previewVisible = ref(false);
const previewImage = ref("");

const publishForm = reactive({
  title: "",
  category: undefined,
  level: undefined,
  duration: 60,
  price: 0,
  description: "",
  cover: "",
});

const publishRules = {
  title: [{ required: true, message: "请输入课程名称" }],
  category: [{ required: true, message: "请选择课程分类" }],
  level: [{ required: true, message: "请选择课程难度" }],
  duration: [{ required: true, message: "请输入课时时长" }],
  price: [{ required: true, message: "请输入课时价格" }],
  description: [{ required: true, message: "请输入课程简介" }],
};

// 显示发布课程modal
const showPublishModal = () => {
  publishModalVisible.value = true;
};

// 处理图片上传前的验证
const beforeUpload = (file) => {
  const isImage = file.type.startsWith("image/");
  if (!isImage) {
    message.error("只能上传图片文件!");
    return false;
  }
  const isLt2M = file.size / 1024 / 1024 < 2;
  if (!isLt2M) {
    message.error("图片大小不能超过2MB!");
    return false;
  }
  return true;
};

// 处理图片预览
const handlePreview = async (file) => {
  if (!file.url && !file.preview) {
    file.preview = await getBase64(file.originFileObj);
  }
  previewImage.value = file.url || file.preview;
  previewVisible.value = true;
};

// 发布课程
const handlePublishCourse = () => {
  publishFormRef.value.validate().then(() => {
    if (coverFileList.value.length === 0) {
      message.error("请上传课程封面");
      return;
    }

    publishLoading.value = true;
    // 模拟发布请求
    setTimeout(() => {
      const newCourse = {
        id: Date.now(),
        ...publishForm,
        teacherName: "当前教师", // 实际应该是当前登录教师的名字
        teacherId: 1, // 实际应该是当前登录教师的ID
        rating: 5.0,
        studentCount: 0,
        cover: coverFileList.value[0].url || "https://picsum.photos/300/200",
      };

      allCourses.value.unshift(newCourse);
      publishModalVisible.value = false;
      publishLoading.value = false;
      message.success("课程发布成功");

      // 重置表单
      publishFormRef.value.resetFields();
      coverFileList.value = [];
    }, 1500);
  });
};

// 将File对象转换为base64
const getBase64 = (file) => {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.readAsDataURL(file);
    reader.onload = () => resolve(reader.result);
    reader.onerror = (error) => reject(error);
  });
};

// 添加审核处理函数
const handleAudit = (course, status) => {
  if (!localStorage.getItem("token")) {
    message.info("请先登录");
    router.push({
      name: "Login",
      query: { redirect: router.currentRoute.value.fullPath },
    });
    return;
  }

  // 更新课程状态
  const targetCourse = allCourses.value.find((c) => c.id === course.id);
  if (targetCourse) {
    targetCourse.status = status;
    const action = status === "approved" ? "通过" : "拒绝";
    message.success(`已${action}课程: ${course.title}`);
  }
};

// 页面加载时的处理
onMounted(() => {
  // 实际项目中可能需要从API获取课程列表数据
  // 这里使用模拟数据
});
</script>

<style lang="less" scoped>
@import "../assets/styles/variables.less";

.course-list-container {
  width: 100%;

  .course-status {
    margin-top: 10px;
  }
  .course-list-header {
    margin-bottom: @padding-lg;

    .header-content {
      display: flex;
      justify-content: space-between;
      align-items: center;

      h2 {
        font-size: 24px;
        margin-bottom: @padding-xs;
      }

      p {
        color: @text-color-secondary;
        margin: 0;
      }
    }
  }

  .filter-section {
    background: white;
    padding: @padding-md;
    border-radius: @border-radius-base;
    margin-bottom: @padding-lg;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
    width: 100%;
  }

  .course-list-content {
    width: 100%;

    .course-card {
      height: 100%;
      transition: transform 0.3s ease;
      display: flex;
      flex-direction: column;
      justify-content: space-between;

      &:hover {
        transform: translateY(-5px);
      }

      .course-cover {
        position: relative;
        height: 200px;
        overflow: hidden;

        img {
          width: 100%;
          height: 100%;
          object-fit: cover;
        }

        .course-pricing {
          position: absolute;
          bottom: 0;
          right: 0;
          background: rgba(0, 0, 0, 0.6);
          color: white;
          padding: 4px 8px;
          font-size: @font-size-sm;
        }
      }

      .course-info {
        .course-tags {
          margin-bottom: @padding-xs;
        }

        .course-teacher {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: @padding-xs;
          flex-wrap: wrap;

          .ant-rate {
            font-size: 12px;
          }
        }

        .course-desc {
          margin-top: @padding-xs;
          color: @text-color-secondary;
          overflow: hidden;
          text-overflow: ellipsis;
          display: -webkit-box;
          -webkit-line-clamp: 2;
          -webkit-box-orient: vertical;
          cursor: pointer;
        }
      }
    }

    .pagination-container {
      text-align: center;
      margin-top: @padding-lg;
    }
  }
}

@media (max-width: 768px) {
  .course-list-container {
    .filter-section {
      .ant-form-item {
        margin-bottom: @padding-xs;
      }
    }

    .course-list-content {
      .course-card {
        .course-info {
          .course-teacher {
            flex-direction: column;
            align-items: flex-start;

            .ant-rate {
              margin-top: @padding-xs;
            }
          }
        }
      }
    }
  }
}
</style>
