<template>
  <div
    class="course-list-container container animate__animated animate__fadeIn"
  >
    <div class="course-list-header">
      <div class="header-content">
        <div>
          <h2>{{ headerContent.title }}</h2>
          <p>{{ headerContent.description }}</p>
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
      </a-form>
    </div>

    <!-- 课程列表 -->
    <div class="course-list-content">
      <a-spin :spinning="loading">
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
                      <a-tag color="orange"
                        >{{ course.duration }}分钟/课时</a-tag
                      >
                    </div>
                    <div class="course-teacher">
                      <span>教师：{{ course.teacherName || "-" }}</span>
                      <a-rate :value="course.rating" disabled allow-half />
                    </div>
                    <a-tooltip :title="course.description">
                      <p class="course-desc">{{ course.description }}</p>
                    </a-tooltip>
                  </div>
                </template>
              </a-card-meta>

              <template #actions>
                <!-- 教师只能看到自己课程的编辑和删除按钮 -->
                <template v-if="userRole === 'teacher'">
                  <a-button type="link" @click="handleEditCourse(course)">
                    <template #icon><EditOutlined /></template>
                    编辑
                  </a-button>
                  <a-popconfirm
                    title="确定要删除这个课程吗？"
                    ok-text="确定"
                    cancel-text="取消"
                    @confirm="handleDeleteCourse(course.id)"
                  >
                    <a-button type="link" danger>
                      <template #icon><DeleteOutlined /></template>
                      删除
                    </a-button>
                  </a-popconfirm>
                </template>
                <!-- 非教师（学生）只能看到查看详情和预约按钮 -->
                <template v-else>
                  <a-button type="link" @click="viewCourseDetail(course.id)">
                    查看详情
                  </a-button>
                  <a-button type="link" @click="handleBookCourse(course)">
                    立即预约
                  </a-button>
                </template>
              </template>
            </a-card>
          </a-col>
        </a-row>

        <!-- 空状态 -->
        <a-empty
          v-if="!loading && filteredCourses.length === 0"
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
      </a-spin>
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

        <a-form-item label="课程封面" name="cover">
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

    <!-- 添加编辑课程的Modal -->
    <a-modal
      v-model:visible="editModalVisible"
      title="编辑课程"
      :width="700"
      @ok="handleUpdateCourse"
      :confirmLoading="editLoading"
    >
      <a-form
        :model="editForm"
        :rules="publishRules"
        ref="editFormRef"
        layout="vertical"
      >
        <a-form-item label="课程名称" name="title" required>
          <a-input
            v-model:value="editForm.title"
            placeholder="请输入课程名称"
          />
        </a-form-item>

        <a-form-item label="课程分类" name="category" required>
          <a-select
            v-model:value="editForm.category"
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

        <a-form-item label="课时时长(分钟)" name="duration" required>
          <a-input-number
            v-model:value="editForm.duration"
            :min="30"
            :max="180"
            :step="15"
            style="width: 100%"
          />
        </a-form-item>

        <a-form-item label="课时价格(元)" name="price" required>
          <a-input-number
            v-model:value="editForm.price"
            :min="0"
            :precision="2"
            style="width: 100%"
          />
        </a-form-item>

        <a-form-item label="课程封面" name="cover">
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
            v-model:value="editForm.description"
            :rows="4"
            placeholder="请输入课程简介"
          />
        </a-form-item>
      </a-form>
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
  EditOutlined,
  DeleteOutlined,
} from "@ant-design/icons-vue";
import { message } from "ant-design-vue";
import { api } from "../utils/axios"; // 导入 API

const router = useRouter();

// 筛选表单数据
const filterForm = reactive({
  category: undefined,
  keyword: "",
});

const editFormRef = ref(null);

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

// 课程数据
const allCourses = ref([]);
const loading = ref(false);

// 获取课程列表
const fetchCourses = async () => {
  loading.value = true;
  try {
    const params = {
      page: pagination.current - 1,
      size: pagination.pageSize,
      category: filterForm.category,
      keyword: filterForm.keyword,
    };

    let response;
    // 根据用户角色决定获取哪种课程列表
    if (userRole.value === "teacher") {
      response = await api.getTeacherCourses(params);
    } else {
      // 普通用户获取所有课程
      response = await api.getAllCourses(params);
    }

    filteredCourses.value = response.courses;
    totalCourses.value = response.totalItems;
  } catch (error) {
    console.error("获取课程列表失败:", error);
    message.error("获取课程列表失败，请稍后重试");
  } finally {
    loading.value = false;
  }
};

// 处理筛选条件变化
const handleFilterChange = () => {
  pagination.current = 1;
  fetchCourses();
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
const filteredCourses = ref([]);

// 修改 totalCourses computed 属性
const totalCourses = ref(0);

// 页面变化处理
const handlePageChange = (page, pageSize) => {
  pagination.current = page;
  pagination.pageSize = pageSize;
  fetchCourses();
};

// 每页条数变化处理
const handleSizeChange = (current, size) => {
  pagination.current = 1;
  pagination.pageSize = size;
  fetchCourses();
};

// 查看课程详情
const viewCourseDetail = (id) => {
  router.push({
    name: "CourseDetail",
    params: { id: id },
  });
};

// 预约课程
const handleBookCourse = async (course) => {
  if (!localStorage.getItem("token")) {
    message.info("请先登录后再预约课程");
    router.push({
      name: "Login",
      query: { redirect: router.currentRoute.value.fullPath },
    });
    return;
  }

  const orderData = {
    courseId: course.id,
    bookingTime: new Date().toISOString(),
  };

  await api.createOrder(orderData);
  message.success(`预约课程成功: ${course.title}，请去订单管理支付对应的订单`);
};

// 用户角色（实际项目中应从用户状态或vuex中获取）
const userRole = ref(localStorage.getItem("userRole").toLowerCase()); // 临时写死为teacher便于测试

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
  duration: 60,
  price: 0,
  description: "",
  cover: "",
});

const publishRules = {
  title: [{ required: true, message: "请输入课程名称" }],
  category: [{ required: true, message: "请选择课程分类" }],
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
  publishFormRef.value.validate().then(async () => {
    if (coverFileList.value.length === 0) {
      message.error("请上传课程封面");
      return;
    }

    publishLoading.value = true;

    try {
      // 处理图片上传
      let coverUrl;
      if (coverFileList.value[0].originFileObj) {
        // 如果是新上传的文件，需要转换为base64
        coverUrl = await getBase64(coverFileList.value[0].originFileObj);
      } else {
        // 如果已经有URL（可能是之前上传的）
        coverUrl =
          coverFileList.value[0].url || coverFileList.value[0].thumbUrl;
      }

      if (!coverUrl) {
        message.error("课程封面处理失败");
        publishLoading.value = false;
        return;
      }

      // 准备课程数据
      const courseData = {
        ...publishForm,
        cover: coverUrl,
      };

      // 发送创建课程请求
      const createdCourse = await api.createCourse(courseData);

      message.success("课程发布成功");
      publishModalVisible.value = false;

      // 重新获取课程列表
      fetchCourses();

      // 重置表单
      publishFormRef.value.resetFields();
      coverFileList.value = [];
    } catch (error) {
      console.error("发布课程失败:", error);
      message.error("发布课程失败，请稍后重试");
    } finally {
      publishLoading.value = false;
    }
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

// 添加编辑相关的状态
const editModalVisible = ref(false);
const editLoading = ref(false);
const editForm = reactive({});
const currentEditingCourse = ref(null);

// 处理编辑按钮点击
const handleEditCourse = (course) => {
  currentEditingCourse.value = course;
  Object.assign(editForm, {
    title: course.title,
    category: course.category,
    duration: course.duration,
    price: course.price,
    description: course.description,
  });
  coverFileList.value = [
    {
      uid: "-1",
      name: "cover.png",
      status: "done",
      url: course.cover,
    },
  ];
  editModalVisible.value = true;
};

// 处理更新课程
const handleUpdateCourse = () => {
  editFormRef.value.validate().then(async () => {
    if (coverFileList.value.length === 0) {
      message.error("请上传课程封面");
      return;
    }

    editLoading.value = true;

    try {
      // 处理图片上传
      let coverUrl;
      if (coverFileList.value[0].originFileObj) {
        // 如果是新上传的文件，需要转换为base64
        coverUrl = await getBase64(coverFileList.value[0].originFileObj);
      } else {
        // 如果已经有URL（可能是之前上传的）
        coverUrl =
          coverFileList.value[0].url || coverFileList.value[0].thumbUrl;
      }

      if (!coverUrl) {
        message.error("课程封面处理失败");
        editLoading.value = false;
        return;
      }

      const courseData = {
        ...editForm,
        cover: coverUrl,
      };

      await api.updateCourse(currentEditingCourse.value.id, courseData);

      message.success("课程更新成功");
      editModalVisible.value = false;

      // 重新获取课程列表
      fetchCourses();

      // 重置表单
      editFormRef.value.resetFields();
      coverFileList.value = [];
    } catch (error) {
      console.error("更新课程失败:", error);
      message.error("更新课程失败，请稍后重试");
    } finally {
      editLoading.value = false;
    }
  });
};

// 处理删除课程
const handleDeleteCourse = async (id) => {
  try {
    await api.deleteCourse(id);
    message.success("课程删除成功");
    // 重新获取课程列表
    fetchCourses();
  } catch (error) {
    console.error("删除课程失败:", error);
    message.error("删除课程失败，请稍后重试");
  }
};

// 修改页面标题和描述
const getHeaderContent = () => {
  if (userRole.value === "teacher") {
    return {
      title: "我的课程",
      description: "管理您发布的课程",
    };
  }
  return {
    title: "浏览课程",
    description: "探索各类优质课程，找到适合您的学习方案",
  };
};

// 在模板中使用
const headerContent = computed(() => getHeaderContent());

// 页面加载时的处理
onMounted(() => {
  fetchCourses();
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
    min-height: 400px; // 添加最小高度，避免加载时页面跳动

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
