<template>
  <div class="teacher-center-container container">
    <a-row :gutter="24">
      <!-- 左侧菜单 -->
      <a-col :xs="24" :sm="6">
        <a-card class="menu-card">
          <div class="teacher-profile">
            <a-avatar :size="80" src="https://picsum.photos/id/1005/200/200" />
            <h3>{{ teacherInfo.name }}</h3>
            <p>{{ teacherInfo.subject }} 教师</p>
          </div>
          
          <a-menu
            v-model:selectedKeys="selectedKeys"
            mode="inline"
            style="border-right: 0;"
          >
            <a-menu-item key="profile">
              <template #icon><user-outlined /></template>
              个人信息
            </a-menu-item>
            <a-menu-item key="courses">
              <template #icon><book-outlined /></template>
              我的课程
            </a-menu-item>
            <a-menu-item key="appointments">
              <template #icon><calendar-outlined /></template>
              预约管理
            </a-menu-item>
            <a-menu-item key="students">
              <template #icon><team-outlined /></template>
              学生管理
            </a-menu-item>
            <a-menu-item key="settings">
              <template #icon><setting-outlined /></template>
              账号设置
            </a-menu-item>
          </a-menu>
        </a-card>
      </a-col>
      
      <!-- 右侧内容 -->
      <a-col :xs="24" :sm="18">
        <!-- 教师信息 -->
        <div v-show="selectedKeys.includes('profile')" class="content-section">
          <a-card title="教师信息" :bordered="false">
            <a-descriptions :column="{ xxl: 4, xl: 3, lg: 3, md: 3, sm: 2, xs: 1 }">
              <a-descriptions-item label="姓名">{{ teacherInfo.name }}</a-descriptions-item>
              <a-descriptions-item label="教学科目">{{ teacherInfo.subject }}</a-descriptions-item>
              <a-descriptions-item label="教龄">{{ teacherInfo.experience }}年</a-descriptions-item>
              <a-descriptions-item label="评分">
                <a-rate :value="teacherInfo.rating" disabled allow-half />
              </a-descriptions-item>
              <a-descriptions-item label="电子邮箱" :span="2">{{ teacherInfo.email }}</a-descriptions-item>
              <a-descriptions-item label="电话号码" :span="2">{{ teacherInfo.phone }}</a-descriptions-item>
              <a-descriptions-item label="教师简介" :span="3">
                {{ teacherInfo.introduction }}
              </a-descriptions-item>
            </a-descriptions>
            
            <a-divider />
            
            <div class="action-buttons">
              <a-button type="primary">
                编辑资料
              </a-button>
            </div>
          </a-card>
        </div>
        
        <!-- 今日概览 -->
        <div v-show="selectedKeys.includes('profile')" class="content-section">
          <a-card title="今日概览" :bordered="false">
            <a-row :gutter="16">
              <a-col :span="6">
                <a-statistic title="今日课程" :value="dashboardData.todayClasses" />
              </a-col>
              <a-col :span="6">
                <a-statistic title="待处理预约" :value="dashboardData.pendingAppointments" />
              </a-col>
              <a-col :span="6">
                <a-statistic title="本月收入" :value="dashboardData.monthlyIncome" prefix="¥" />
              </a-col>
              <a-col :span="6">
                <a-statistic title="总学生数" :value="dashboardData.totalStudents" />
              </a-col>
            </a-row>
          </a-card>
        </div>
        
        <!-- 我的课程 -->
        <div v-show="selectedKeys.includes('courses')" class="content-section">
          <a-card title="我的课程" :bordered="false">
            <a-table :columns="courseColumns" :data-source="myCourses" :pagination="{ pageSize: 5 }">
              <template #bodyCell="{ column, record }">
                <template v-if="column.key === 'action'">
                  <a-space>
                    <a @click="editCourse(record.id)">编辑</a>
                    <a-divider type="vertical" />
                    <a @click="viewStudents(record.id)">查看学生</a>
                  </a-space>
                </template>
              </template>
            </a-table>
            
            <div class="action-buttons">
              <a-button type="primary">
                新增课程
              </a-button>
            </div>
          </a-card>
        </div>
        
        <!-- 预约管理 -->
        <div v-show="selectedKeys.includes('appointments')" class="content-section">
          <a-card title="预约管理" :bordered="false">
            <a-tabs default-active-key="pending">
              <a-tab-pane key="pending" tab="待处理">
                <a-table :columns="appointmentColumns" :data-source="pendingAppointments" :pagination="{ pageSize: 5 }">
                  <template #bodyCell="{ column, record }">
                    <template v-if="column.key === 'action'">
                      <a-space>
                        <a-button type="primary" size="small" @click="approveAppointment(record.id)">接受</a-button>
                        <a-button danger size="small" @click="rejectAppointment(record.id)">拒绝</a-button>
                      </a-space>
                    </template>
                  </template>
                </a-table>
              </a-tab-pane>
              <a-tab-pane key="upcoming" tab="即将上课">
                <a-table :columns="appointmentColumns" :data-source="upcomingAppointments" :pagination="{ pageSize: 5 }" />
              </a-tab-pane>
              <a-tab-pane key="history" tab="历史记录">
                <a-table :columns="appointmentColumns" :data-source="historyAppointments" :pagination="{ pageSize: 5 }" />
              </a-tab-pane>
            </a-tabs>
          </a-card>
        </div>
        
        <!-- 学生管理 -->
        <div v-show="selectedKeys.includes('students')" class="content-section">
          <a-card title="学生管理" :bordered="false">
            <a-table :columns="studentColumns" :data-source="myStudents" :pagination="{ pageSize: 5 }">
              <template #bodyCell="{ column, record }">
                <template v-if="column.key === 'action'">
                  <a-space>
                    <a @click="viewStudentDetail(record.id)">详情</a>
                    <a-divider type="vertical" />
                    <a @click="contactStudent(record.id)">联系</a>
                  </a-space>
                </template>
              </template>
            </a-table>
          </a-card>
        </div>
        
        <!-- 账号设置 -->
        <div v-show="selectedKeys.includes('settings')" class="content-section">
          <a-card title="账号设置" :bordered="false">
            <a-form :model="settingsForm" layout="vertical">
              <a-form-item label="修改密码">
                <a-input-password v-model:value="settingsForm.oldPassword" placeholder="旧密码" />
              </a-form-item>
              <a-form-item>
                <a-input-password v-model:value="settingsForm.newPassword" placeholder="新密码" />
              </a-form-item>
              <a-form-item>
                <a-input-password v-model:value="settingsForm.confirmPassword" placeholder="确认新密码" />
              </a-form-item>
              
              <a-divider />
              
              <a-form-item label="通知设置">
                <a-checkbox v-model:checked="settingsForm.emailNotification">邮件通知</a-checkbox>
                <a-checkbox v-model:checked="settingsForm.smsNotification">短信通知</a-checkbox>
              </a-form-item>
              
              <div class="action-buttons">
                <a-button type="primary" @click="saveSettings">
                  保存设置
                </a-button>
              </div>
            </a-form>
          </a-card>
        </div>
      </a-col>
    </a-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { 
  UserOutlined, 
  BookOutlined, 
  CalendarOutlined,
  TeamOutlined,
  SettingOutlined
} from '@ant-design/icons-vue';

const router = useRouter();
const selectedKeys = ref(['profile']);

// 教师信息数据
const teacherInfo = reactive({
  name: '张明',
  subject: '数学',
  experience: 8,
  rating: 4.8,
  email: 'zhangming@example.com',
  phone: '13812345678',
  introduction: '清华大学数学系毕业，拥有8年高中数学教学经验，专注于高考数学辅导，注重培养学生的数学思维和解题能力。'
});

// 仪表盘数据
const dashboardData = reactive({
  todayClasses: 3,
  pendingAppointments: 5,
  monthlyIncome: 12800,
  totalStudents: 35
});

// 课程表格列
const courseColumns = [
  {
    title: '课程名称',
    dataIndex: 'title',
    key: 'title',
  },
  {
    title: '科目',
    dataIndex: 'subject',
    key: 'subject',
  },
  {
    title: '单价',
    dataIndex: 'price',
    key: 'price',
    customRender: ({ text }) => `¥${text}`,
  },
  {
    title: '学生数',
    dataIndex: 'studentCount',
    key: 'studentCount',
  },
  {
    title: '操作',
    key: 'action',
  },
];

// 课程数据
const myCourses = ref([
  {
    id: 1,
    title: '高中数学强化班',
    subject: '数学',
    price: 200,
    studentCount: 15,
  },
  {
    id: 2,
    title: '高考数学冲刺班',
    subject: '数学',
    price: 280,
    studentCount: 10,
  },
  {
    id: 3,
    title: '初中数学提高班',
    subject: '数学',
    price: 180,
    studentCount: 18,
  },
]);

// 预约表格列
const appointmentColumns = [
  {
    title: '学生姓名',
    dataIndex: 'studentName',
    key: 'studentName',
  },
  {
    title: '课程',
    dataIndex: 'courseName',
    key: 'courseName',
  },
  {
    title: '日期',
    dataIndex: 'date',
    key: 'date',
  },
  {
    title: '时间',
    dataIndex: 'time',
    key: 'time',
  },
  {
    title: '状态',
    dataIndex: 'status',
    key: 'status',
  },
  {
    title: '操作',
    key: 'action',
  },
];

// 预约数据
const pendingAppointments = ref([
  {
    id: 1,
    studentName: '李华',
    courseName: '高中数学强化班',
    date: '2023-05-20',
    time: '15:00-16:30',
    status: '待处理',
  },
  {
    id: 2,
    studentName: '王明',
    courseName: '高考数学冲刺班',
    date: '2023-05-21',
    time: '10:00-11:30',
    status: '待处理',
  },
]);

const upcomingAppointments = ref([
  {
    id: 3,
    studentName: '张三',
    courseName: '高中数学强化班',
    date: '2023-05-18',
    time: '15:00-16:30',
    status: '已确认',
  },
  {
    id: 4,
    studentName: '李四',
    courseName: '高考数学冲刺班',
    date: '2023-05-19',
    time: '10:00-11:30',
    status: '已确认',
  },
]);

const historyAppointments = ref([
  {
    id: 5,
    studentName: '赵六',
    courseName: '高中数学强化班',
    date: '2023-05-10',
    time: '15:00-16:30',
    status: '已完成',
  },
  {
    id: 6,
    studentName: '钱七',
    courseName: '高考数学冲刺班',
    date: '2023-05-12',
    time: '10:00-11:30',
    status: '已完成',
  },
]);

// 学生表格列
const studentColumns = [
  {
    title: '学生姓名',
    dataIndex: 'name',
    key: 'name',
  },
  {
    title: '课程',
    dataIndex: 'course',
    key: 'course',
  },
  {
    title: '联系方式',
    dataIndex: 'contact',
    key: 'contact',
  },
  {
    title: '报名时间',
    dataIndex: 'enrollDate',
    key: 'enrollDate',
  },
  {
    title: '操作',
    key: 'action',
  },
];

// 学生数据
const myStudents = ref([
  {
    id: 1,
    name: '李华',
    course: '高中数学强化班',
    contact: '13911112222',
    enrollDate: '2023-03-15',
  },
  {
    id: 2,
    name: '王明',
    course: '高考数学冲刺班',
    contact: '13922223333',
    enrollDate: '2023-04-10',
  },
  {
    id: 3,
    name: '张三',
    course: '高中数学强化班',
    contact: '13933334444',
    enrollDate: '2023-02-20',
  },
]);

// 账号设置表单
const settingsForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
  emailNotification: true,
  smsNotification: false,
});

// 方法
const editCourse = (id) => {
  console.log('编辑课程', id);
};

const viewStudents = (id) => {
  console.log('查看课程学生', id);
  selectedKeys.value = ['students'];
};

const approveAppointment = (id) => {
  console.log('接受预约', id);
};

const rejectAppointment = (id) => {
  console.log('拒绝预约', id);
};

const viewStudentDetail = (id) => {
  console.log('查看学生详情', id);
};

const contactStudent = (id) => {
  console.log('联系学生', id);
};

const saveSettings = () => {
  console.log('保存设置', settingsForm);
};

onMounted(() => {
  // 获取教师数据
});
</script>

<style lang="less" scoped>
.teacher-center-container {
  padding: 20px 0;
  
  .menu-card {
    margin-bottom: 20px;
    
    .teacher-profile {
      text-align: center;
      padding: 20px 0;
      border-bottom: 1px solid #f0f0f0;
      margin-bottom: 20px;
      
      h3 {
        margin-top: 15px;
        margin-bottom: 5px;
      }
      
      p {
        color: #666;
      }
    }
  }
  
  .content-section {
    margin-bottom: 20px;
  }
  
  .action-buttons {
    margin-top: 20px;
    text-align: right;
  }
  
  @media (max-width: 576px) {
    .menu-card {
      margin-bottom: 20px;
    }
  }
}
</style>