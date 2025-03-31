<template>
  <div class="user-qualifications-container">
    <a-card title="资质管理" :bordered="false">
      <p class="description">
        请上传您的资质证明文件，包括学历证书、教师资格证、专业技能证书等，这些将有助于提高您的认证等级和获得更多教学机会。
      </p>

      <!-- 资质状态 -->
      <a-alert
        :message="getVerificationStatusMessage(teacherInfo.verificationStatus)"
        :description="getVerificationDescription(teacherInfo.verificationStatus)"
        :type="getVerificationType(teacherInfo.verificationStatus)"
        show-icon
        style="margin-bottom: 20px"
      />

      <!-- 资质列表 -->
      <template v-if="qualifications.length">
        <a-divider orientation="left">已上传资质</a-divider>

        <a-list :data-source="qualifications" item-layout="horizontal">
          <template #renderItem="{ item }">
            <a-list-item>
              <a-list-item-meta :title="item.name">
                <template #avatar>
                  <file-outlined />
                </template>
                <template #description>
                  <div>上传时间: {{ item.uploadTime }}</div>
                  <div>
                    状态:
                    <a-tag :color="getStatusColor(item.status.toLowerCase())">
                      {{ getStatusText(item.status.toLowerCase()) }}
                    </a-tag>
                  </div>
                  <div v-if="item.comment">审核意见: {{ item.comment }}</div>
                </template>
              </a-list-item-meta>
              <template #actions>
                <a-button type="link" @click="previewFileFunction(item)">
                  <eye-outlined />
                  预览
                </a-button>
                <a-button
                  type="link"
                  danger
                  v-if="item.status !== 'approved'"
                  @click="deleteFile(item.id)"
                >
                  <delete-outlined />
                  删除
                </a-button>
              </template>
            </a-list-item>
          </template>

          <template #empty>
            <div class="empty-qualifications">
              <p>您还没有上传任何资质证明</p>
            </div>
          </template>
        </a-list>
      </template>
      <!-- 上传表单 -->
      <a-divider orientation="left">上传新资质</a-divider>

      <a-form :model="uploadForm" layout="vertical">
        <a-form-item label="证书类型" name="type" required>
          <a-select v-model:value="uploadForm.type" placeholder="请选择证书类型">
            <a-select-option value="education">学历证书</a-select-option>
            <a-select-option value="teaching">教师资格证</a-select-option>
            <a-select-option value="professional">专业技能证书</a-select-option>
            <a-select-option value="language">语言能力证书</a-select-option>
            <a-select-option value="other">其他证书</a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item label="证书名称" name="name" required>
          <a-input
            v-model:value="uploadForm.name"
            placeholder="请输入证书名称，如：教师资格证、学士学位证书等"
          />
        </a-form-item>

        <a-form-item label="发证机构" name="issuer" required>
          <a-input v-model:value="uploadForm.issuer" placeholder="请输入发证机构名称" />
        </a-form-item>

        <a-form-item label="证书文件" name="file" required>
          <div class="upload-wrapper">
            <div v-if="imagePreview" class="image-preview">
              <img :src="imagePreview" alt="预览" />
              <div class="preview-actions">
                <a-button type="primary" size="small" @click="selectNewImage"> 重新选择 </a-button>
              </div>
            </div>
            <div v-else class="upload-area">
              <input
                type="file"
                ref="fileInput"
                style="display: none"
                accept="image/jpeg,image/png,image/jpg"
                @change="handleFileChange"
              />
              <div class="upload-trigger" @click="triggerFileSelect">
                <p class="upload-icon">
                  <picture-outlined />
                </p>
                <p class="upload-text">点击上传图片</p>
                <p class="upload-hint">仅支持 JPG、PNG 格式图片，图片大小不超过5MB</p>
              </div>
            </div>
          </div>
        </a-form-item>

        <a-form-item label="备注说明" name="description">
          <a-textarea
            v-model:value="uploadForm.description"
            placeholder="请简要说明此证书的相关信息"
            :rows="4"
          />
        </a-form-item>

        <a-form-item>
          <a-button
            type="primary"
            :loading="uploading"
            @click="submitUpload"
            :disabled="!imagePreview"
          >
            提交
          </a-button>
        </a-form-item>
      </a-form>
    </a-card>

    <!-- 预览modal -->
    <a-modal v-model:visible="previewVisible" title="资质证书预览" :footer="null" :width="800">
      <div class="preview-container">
        <img :src="previewFile.url" alt="证书预览" />
      </div>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { api } from '../../utils/axios'
import { FileOutlined, EyeOutlined, DeleteOutlined, PictureOutlined } from '@ant-design/icons-vue'

const uploading = ref(false)
const previewVisible = ref(false)
const fileInput = ref(null)
const imagePreview = ref('')
const loading = ref(false)

// 教师信息
const teacherInfo = reactive({
  verificationStatus: 'pending' // 可能的值: unverified, pending, verified, rejected
})

// 上传表单
const uploadForm = reactive({
  type: undefined,
  name: '',
  issuer: '',
  description: '',
  fileUrl: ''
})

// 预览文件信息
const previewFile = reactive({
  url: '',
  type: 'image'
})

// 资质数据
const qualifications = ref([])

// 选择文件
const triggerFileSelect = () => {
  fileInput.value.click()
}

// 选择新图片
const selectNewImage = () => {
  imagePreview.value = ''
  uploadForm.fileUrl = ''
  fileInput.value.value = ''
  setTimeout(() => {
    triggerFileSelect()
  }, 100)
}

// 处理文件改变
const handleFileChange = (e) => {
  const file = e.target.files[0]
  if (!file) return

  // 检查文件类型
  const isImage =
    file.type === 'image/jpeg' || file.type === 'image/png' || file.type === 'image/jpg'
  if (!isImage) {
    message.error('只能上传JPG或PNG格式图片!')
    return
  }

  // 检查文件大小
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isLt5M) {
    message.error('图片大小不能超过5MB!')
    return
  }

  // 转换为base64
  const reader = new FileReader()
  reader.readAsDataURL(file)
  reader.onload = (e) => {
    const base64 = e.target.result
    imagePreview.value = base64
    uploadForm.fileUrl = base64
  }
}

// 获取用户资质
const fetchQualifications = async () => {
  loading.value = true
  try {
    const data = await api.getQualifications()
    qualifications.value = data
  } catch (error) {
    console.error('Failed to fetch qualifications:', error)
    message.error('获取资质信息失败')
  } finally {
    loading.value = false
  }
}

// 提交上传
const submitUpload = async () => {
  // 表单验证
  if (!uploadForm.type || !uploadForm.name || !uploadForm.issuer || !uploadForm.fileUrl) {
    message.error('请填写必填项并上传证书图片')
    return
  }

  uploading.value = true

  try {
    await api.addQualification({
      name: uploadForm.name,
      type: uploadForm.type,
      issuer: uploadForm.issuer,
      description: uploadForm.description,
      fileUrl: uploadForm.fileUrl
    })

    // 重置表单
    uploadForm.type = undefined
    uploadForm.name = ''
    uploadForm.issuer = ''
    uploadForm.description = ''
    uploadForm.fileUrl = ''
    imagePreview.value = ''

    message.success('资质证书上传成功，等待管理员审核')

    // 重新获取资质列表
    fetchQualifications()
  } catch (error) {
    console.error('Failed to upload qualification:', error)
    message.error('上传资质证书失败')
  } finally {
    uploading.value = false
  }
}

// 预览文件
const previewFileFunction = (item) => {
  previewVisible.value = true
  previewFile.url = item.fileUrl
}

// 删除文件
const deleteFile = async (id) => {
  try {
    await api.deleteQualification(id)
    message.success('已删除资质记录')
    // 重新获取资质列表
    fetchQualifications()
  } catch (error) {
    console.error('Failed to delete qualification:', error)
    message.error('删除资质记录失败')
  }
}

// 获取状态颜色
const getStatusColor = (status) => {
  const statusMap = {
    approved: 'success',
    pending: 'warning',
    rejected: 'error'
  }
  return statusMap[status] || 'default'
}

// 获取状态文本
const getStatusText = (status) => {
  const statusMap = {
    approved: '已审核通过',
    pending: '审核中',
    rejected: '审核不通过'
  }
  return statusMap[status] || status
}

// 获取验证状态信息
const getVerificationStatusMessage = (status) => {
  const statusMap = {
    unverified: '未提交资质验证',
    pending: '资质审核中',
    verified: '资质已验证',
    rejected: '资质验证失败'
  }
  return statusMap[status] || status
}

// 获取验证状态描述
const getVerificationDescription = (status) => {
  const descMap = {
    unverified: '请上传您的资质证明材料，以便平台对您的教师身份进行认证。',
    pending: '您的资质证明材料已提交，正在等待管理员审核，请耐心等待。',
    verified: '您的教师资质已通过验证，可以开始接单授课。',
    rejected: '您的资质验证未通过，请查看管理员反馈，并重新提交符合要求的资质材料。'
  }
  return descMap[status] || ''
}

// 获取验证类型
const getVerificationType = (status) => {
  const typeMap = {
    unverified: 'info',
    pending: 'warning',
    verified: 'success',
    rejected: 'error'
  }
  return typeMap[status] || 'info'
}

// 计算教师资质状态
const calculateTeacherStatus = (qualificationsList) => {
  // 如果没有资质，状态为未验证
  if (qualificationsList.length === 0) {
    return 'unverified'
  }

  // 如果有任何一个资质已通过，状态为已验证
  const hasApproved = qualificationsList.some((q) => q.status === 'approved')
  if (hasApproved) {
    return 'verified'
  }

  // 如果全部被拒绝，状态为被拒绝
  const allRejected = qualificationsList.every((q) => q.status === 'rejected')
  if (allRejected) {
    return 'rejected'
  }

  // 否则状态为待审核
  return 'pending'
}

// 获取教师资质状态
const updateTeacherStatus = () => {
  teacherInfo.verificationStatus = calculateTeacherStatus(qualifications.value)
}

onMounted(() => {
  // 获取用户资质列表
  fetchQualifications().then(() => {
    // 更新教师资质状态
    updateTeacherStatus()
  })
})
</script>

<style lang="less" scoped>
@import '../../assets/styles/variables.less';

img {
  width: 100%;
}

.user-qualifications-container {
  .description {
    margin-bottom: 20px;
    color: @text-color-secondary;
  }

  .empty-qualifications {
    text-align: center;
    padding: 30px 0;
    color: @text-color-secondary;
  }

  .upload-wrapper {
    border: 1px dashed #d9d9d9;
    border-radius: 4px;
    background-color: #fafafa;
    transition: border-color 0.3s;

    &:hover {
      border-color: @primary-color;
    }

    .upload-area {
      padding: 20px;
      cursor: pointer;

      .upload-icon {
        text-align: center;
        margin-bottom: 16px;

        .anticon {
          font-size: 48px;
          color: #999;
        }
      }

      .upload-text {
        text-align: center;
        font-size: 16px;
        color: @text-color;
        margin-bottom: 8px;
      }

      .upload-hint {
        text-align: center;
        font-size: 14px;
        color: @text-color-secondary;
      }
    }

    .image-preview {
      padding: 16px;
      text-align: center;

      img {
        max-width: 100%;
        max-height: 300px;
        margin-bottom: 16px;
        border-radius: 4px;
      }

      .preview-actions {
        margin-top: 8px;
      }
    }
  }

  .preview-container {
    display: flex;
    justify-content: center;
    align-items: center;
    min-height: 300px;

    img {
      max-width: 100%;
      border-radius: 4px;
      width: 100%;
    }
  }
  :deep(.ant-list-item-meta-description) {
    display: flex;
    flex-direction: column;
    gap: 10px;
  }
}
</style>
