<template>
  <div class="qualification-review">
    <div class="page-header">
      <div class="header-content">
        <div>
          <h2>{{ teacher ? teacher.name : '教师' }}资质审核</h2>
          <p>审核教师提交的资质证明材料</p>
        </div>
      </div>
      <a-button @click="goBack">返回教师列表</a-button>
    </div>

    <div v-if="loading" class="loading-container">
      <a-spin size="large" />
    </div>

    <div v-else-if="error" class="error-container">
      <a-alert :message="error" type="error" />
    </div>

    <div v-else class="content-container">
      <!-- 基本信息 -->
      <a-card title="教师基本信息" class="info-card">
        <a-descriptions bordered :column="2">
          <a-descriptions-item label="姓名">{{ teacher?.name }}</a-descriptions-item>
          <a-descriptions-item label="邮箱">{{ teacher.email }}</a-descriptions-item>
          <a-descriptions-item label="手机号">{{ teacher.phone }}</a-descriptions-item>
          <a-descriptions-item label="教学经验">{{ teacher.experience }}年</a-descriptions-item>
          <a-descriptions-item label="教学科目" :span="2">
            {{ formatSubjects(teacher.subjects) }}
          </a-descriptions-item>
        </a-descriptions>
      </a-card>

      <!-- 资质列表 -->
      <a-card title="资质证明材料" class="qual-card">
        <a-list itemLayout="horizontal" :dataSource="qualifications" :loading="qualLoading">
          <template #empty>
            <div class="empty-container">
              <a-empty description="该教师暂无资质证明材料" />
            </div>
          </template>
          <template #renderItem="{ item }">
            <a-list-item>
              <a-card class="qual-item-card" :bordered="false">
                <template #title>
                  <div class="qual-item-title">
                    <span>{{ item.name }}</span>
                    <a-tag :color="getStatusColor(item.status)">{{
                      getStatusText(item.status.toUpperCase())
                    }}</a-tag>
                  </div>
                </template>
                <template #extra>
                  <div class="qual-actions">
                    <a-button type="primary" @click="viewDocument(item)" style="margin-right: 8px">
                      查看文件
                    </a-button>
                    <a-button
                      v-if="item.status.toUpperCase() === 'PENDING'"
                      type="primary"
                      @click="approveQualification(item)"
                      style="margin-right: 8px"
                    >
                      通过
                    </a-button>
                    <a-button
                      v-if="item.status.toUpperCase() === 'PENDING'"
                      danger
                      @click="showRejectModal(item)"
                    >
                      拒绝
                    </a-button>
                  </div>
                </template>
                <a-descriptions :column="1" size="small">
                  <a-descriptions-item label="文件名">{{ item.name }}</a-descriptions-item>
                  <a-descriptions-item label="上传时间">{{ item.uploadTime }}</a-descriptions-item>
                  <a-descriptions-item v-if="item.reviewComment" label="审核意见">
                    {{ item.reviewComment }}
                  </a-descriptions-item>
                </a-descriptions>
              </a-card>
            </a-list-item>
          </template>
        </a-list>
      </a-card>
    </div>

    <!-- 文件查看弹窗 -->
    <a-modal v-model:visible="documentModalVisible" title="资质文件" :footer="null" width="800px">
      <div class="document-container">
        <img :src="selectedQualification.fileUrl" alt="资质文件" />
      </div>
    </a-modal>

    <!-- 拒绝理由弹窗 -->
    <a-modal
      v-model:visible="rejectModalVisible"
      title="拒绝资质"
      @ok="confirmReject"
      okText="确认拒绝"
      cancelText="取消"
    >
      <a-form :model="rejectForm" layout="vertical">
        <a-form-item
          label="拒绝理由"
          name="reason"
          :rules="[{ required: true, message: '请输入拒绝理由' }]"
        >
          <a-textarea
            v-model:value="rejectForm.reason"
            :rows="4"
            placeholder="请输入拒绝该资质的理由..."
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import axios, { api } from '../../utils/axios'

const route = useRoute()
const router = useRouter()
const teacherId = route.params.teacherId

// 状态
const teacher = ref(null)
const qualifications = ref([])
const loading = ref(true)
const qualLoading = ref(true)
const error = ref(null)

// 文件查看相关
const documentModalVisible = ref(false)
const documentUrl = ref(null)

// 拒绝弹窗相关
const rejectModalVisible = ref(false)
const selectedQualification = ref(null)
const rejectForm = ref({
  reason: ''
})

// 获取教师信息
const fetchTeacher = async () => {
  loading.value = true
  error.value = null

  try {
    // 调用API获取教师信息
    const response = await api.getTeacherById(teacherId)
    teacher.value = response
  } catch (err) {
    console.error('获取教师信息失败:', err)
    error.value = '获取教师信息失败: ' + (err.response?.data?.error || err.message)
  } finally {
    loading.value = false
  }
}

// 获取资质列表
const fetchQualifications = async () => {
  qualLoading.value = true

  try {
    // 调用API获取教师资质列表
    const response = await api.getTeacherQualifications(teacherId)
    qualifications.value = response.qualifications || []
  } catch (err) {
    console.error('获取资质列表失败:', err)
    message.error('获取资质列表失败')
  } finally {
    qualLoading.value = false
  }
}

// 格式化科目
const formatSubjects = (subjects) => {
  if (!subjects || !subjects.length) return '无'

  const subjectMap = {
    math: '数学',
    english: '英语',
    chinese: '语文',
    physics: '物理',
    chemistry: '化学'
  }

  return subjects.map((subject) => subjectMap[subject] || subject).join(', ')
}

// 获取状态文本
const getStatusText = (status) => {
  const statusMap = {
    PENDING: '待审核',
    APPROVED: '已通过',
    REJECTED: '已拒绝'
  }

  return statusMap[status] || status
}

// 获取状态颜色
const getStatusColor = (status) => {
  const colorMap = {
    PENDING: 'orange',
    APPROVED: 'green',
    REJECTED: 'red'
  }

  return colorMap[status] || 'default'
}

// 查看文件
const viewDocument = (qualification) => {
  selectedQualification.value = qualification

  documentModalVisible.value = true
}

// 通过资质
const approveQualification = async (qualification) => {
  try {
    // 调用API审核资质
    const response = await api.reviewQualification(qualification.id, {
      status: 'APPROVED',
      comment: ''
    })

    // 更新本地数据
    const index = qualifications.value.findIndex((q) => q.id === qualification.id)
    if (index !== -1) {
      qualifications.value[index] = response
    }
    message.success('资质审核通过')
    fetchQualifications()
  } catch (err) {
    console.error('审核资质失败:', err)
    message.error(err.response?.data?.error || '审核失败，请重试')
  }
}

// 显示拒绝弹窗
const showRejectModal = (qualification) => {
  selectedQualification.value = qualification
  rejectForm.value.reason = ''
  rejectModalVisible.value = true
}

// 确认拒绝
const confirmReject = async () => {
  if (!rejectForm.value.reason) {
    message.warning('请输入拒绝理由')
    return
  }

  try {
    // 调用API拒绝资质
    const response = await api.reviewQualification(selectedQualification.value.id, {
      status: 'REJECTED',
      comment: rejectForm.value.reason
    })

    // 更新本地数据
    const index = qualifications.value.findIndex((q) => q.id === selectedQualification.value.id)
    if (index !== -1) {
      qualifications.value[index] = response
    }

    rejectModalVisible.value = false
    message.success('已拒绝该资质')
    fetchQualifications()
  } catch (err) {
    console.error('拒绝资质失败:', err)
    message.error(err.response?.data?.error || '操作失败，请重试')
  }
}

// 返回
const goBack = () => {
  router.push('/teachers')
}

// 页面加载
onMounted(() => {
  fetchTeacher()
  fetchQualifications()
})
</script>

<style lang="less" scoped>
img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.qualification-review {
  padding: 24px;
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 24px;

    .header-content {
      h2 {
        margin-bottom: 8px;
      }

      p {
        color: rgba(0, 0, 0, 0.45);
        margin: 0;
      }
    }
  }

  .loading-container,
  .error-container {
    display: flex;
    justify-content: center;
    align-items: center;
    min-height: 300px;
  }

  .content-container {
    .info-card {
      margin-bottom: 24px;
    }

    .qual-card {
      .empty-container {
        padding: 48px 0;
      }

      .qual-item-card {
        width: 100%;

        .qual-item-title {
          display: flex;
          align-items: center;

          .ant-tag {
            margin-left: 12px;
          }
        }

        .qual-actions {
          display: flex;
        }
      }
    }
  }

  .document-container {
    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
    .no-preview {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      height: 300px;
      background-color: #f0f2f5;
      border-radius: 4px;

      p {
        margin-bottom: 16px;
        color: rgba(0, 0, 0, 0.45);
      }
    }
  }
}
</style>
