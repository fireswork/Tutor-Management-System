<template>
  <div class="register-container animate__animated animate__fadeIn">
    <div class="register-box animate__animated animate__zoomIn">
      <div class="register-header">
        <h2>家教管理系统</h2>
        <p>创建您的账户</p>
      </div>
      
      <a-steps 
        :current="currentStep" 
        size="small" 
        class="register-steps"
      >
        <a-step title="基本信息" />
        <a-step title="角色选择" />
        <a-step title="完成" />
      </a-steps>
      
      <!-- 步骤1：基本信息 -->
      <div v-if="currentStep === 0" class="step-content animate__animated animate__fadeIn">
        <a-form 
          :model="formState" 
          name="register-form" 
          :rules="basicRules"
          @finish="nextStep"
          layout="vertical"
        >
          <a-form-item name="username" label="用户名">
            <a-input v-model:value="formState.username" placeholder="请输入用户名">
              <template #prefix><user-outlined /></template>
            </a-input>
          </a-form-item>
          
          <a-form-item name="realName" label="真实姓名">
            <a-input v-model:value="formState.realName" placeholder="请输入真实姓名">
              <template #prefix><user-outlined /></template>
            </a-input>
          </a-form-item>
          
          <a-form-item name="email" label="电子邮箱">
            <a-input v-model:value="formState.email" placeholder="请输入电子邮箱">
              <template #prefix><mail-outlined /></template>
            </a-input>
          </a-form-item>
          
          <a-form-item name="phone" label="手机号码">
            <a-input v-model:value="formState.phone" placeholder="请输入手机号码">
              <template #prefix><phone-outlined /></template>
            </a-input>
          </a-form-item>
          
          <a-form-item name="password" label="密码">
            <a-input-password v-model:value="formState.password" placeholder="请输入密码">
              <template #prefix><lock-outlined /></template>
            </a-input-password>
          </a-form-item>
          
          <a-form-item name="confirmPassword" label="确认密码">
            <a-input-password v-model:value="formState.confirmPassword" placeholder="请确认密码">
              <template #prefix><safety-outlined /></template>
            </a-input-password>
          </a-form-item>
          
          <a-form-item>
            <a-button type="primary" html-type="submit" block>下一步</a-button>
          </a-form-item>
        </a-form>
      </div>
      
      <!-- 步骤2：角色选择 -->
      <div v-if="currentStep === 1" class="step-content animate__animated animate__fadeIn second-step">
        <a-form 
          :model="formState" 
          name="role-form" 
          @finish="nextStep"
          layout="vertical"
        >
          <a-form-item name="role" label="请选择您的角色">
            <a-radio-group v-model:value="formState.role" button-style="solid">
              <a-radio-button value="student">学生/家长</a-radio-button>
              <a-radio-button value="teacher">教师</a-radio-button>
            </a-radio-group>
          </a-form-item>
          
          <!-- 教师特有字段 -->
          <template v-if="formState.role === 'teacher'">
            <a-form-item name="education" label="学历">
              <a-select v-model:value="formState.education" placeholder="请选择您的学历">
                <a-select-option value="大学本科">大学本科</a-select-option>
                <a-select-option value="硕士研究生">硕士研究生</a-select-option>
                <a-select-option value="博士研究生">博士研究生</a-select-option>
                <a-select-option value="其他">其他</a-select-option>
              </a-select>
            </a-form-item>
            
            <a-form-item name="major" label="专业">
              <a-input v-model:value="formState.major" placeholder="请输入您的专业" />
            </a-form-item>
            
            <a-form-item name="subjects" label="教授科目">
              <a-select 
                v-model:value="formState.subjects" 
                mode="multiple" 
                placeholder="请选择您可以教授的科目"
                :options="SUBJECT_OPTIONS"
              ></a-select>
            </a-form-item>
            
            <a-form-item name="experience" label="教学经验">
              <a-input-number v-model:value="formState.experience" placeholder="请输入教学年限" addon-after="年" style="width: 100%" />
            </a-form-item>
            
            <a-form-item name="bio" label="个人简介">
              <a-textarea v-model:value="formState.bio" placeholder="请输入您的个人简介" :rows="4" />
            </a-form-item>
          </template>
          
          <a-form-item>
            <a-space style="width: 100%">
              <a-button style="flex: 1" @click="prevStep">上一步</a-button>
              <a-button type="primary" html-type="submit" style="flex: 1" :loading="loading">{{ formState.role === 'teacher' ? '提交审核' : '注册' }}</a-button>
            </a-space>
          </a-form-item>
        </a-form>
      </div>
      
      <!-- 步骤3：完成 -->
      <div v-if="currentStep === 2" class="step-content animate__animated animate__fadeIn">
        <div class="register-success">
          <check-circle-outlined class="success-icon" />
          <h3>注册成功！</h3>
          <p v-if="formState.role === 'student'">
            您已成功注册为学生/家长账户，现在可以开始使用平台服务了。
          </p>
          <p v-else>
            您已成功注册，稍后可在个人中心完成资质认证
          </p>
          
          <div class="success-actions">
            <a-button type="primary" @click="goToLogin">前往登录</a-button>
          </div>
        </div>
      </div>
      
      <div class="register-footer">
        已有账户？<a @click="goToLogin">立即登录</a>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { 
  UserOutlined, 
  LockOutlined, 
  MailOutlined, 
  PhoneOutlined, 
  SafetyOutlined, 
  CheckCircleOutlined
} from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import { api } from '../utils/axios'
import { SUBJECT_OPTIONS } from '../utils/constants'

const router = useRouter()
const currentStep = ref(0)
const loading = ref(false)

// 表单数据
const formState = reactive({
  username: '',
  realName: '',
  email: '',
  phone: '',
  password: '',
  confirmPassword: '',
  role: 'student',
  education: '',
  major: '',
  subjects: [],
  experience: 0,
  bio: ''
})

// 表单验证规则
const basicRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'change' },
    { min: 3, message: '用户名长度至少为3个字符', trigger: 'change' }
  ],
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'change' },
    { min: 2, message: '姓名长度至少为2个字符', trigger: 'change' }
  ],
  email: [
    { required: true, message: '请输入电子邮箱', trigger: 'change' },
    { type: 'email', message: '请输入有效的电子邮箱', trigger: 'change' }
  ],
  phone: [
    { required: true, message: '请输入手机号码', trigger: 'change' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入有效的手机号码', trigger: 'change' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'change' },
    { min: 6, message: '密码长度至少为6个字符', trigger: 'change' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'change' },
  ]
}

// 确认密码验证
function validateConfirmPassword(rule, value) {
  if (value === '') {
    return Promise.reject('请确认密码')
  } else if (value !== formState.password) {
    return Promise.reject('两次输入的密码不一致')
  } else {
    return Promise.resolve()
  }
}

// 下一步
const nextStep = () => {
  if (currentStep.value < 2) {
    // 第一步到第二步直接进行
    if (currentStep.value === 0) {
      currentStep.value++
    } 
    // 第二步提交到后端
    else if (currentStep.value === 1) {
      loading.value = true
      
      // 调用注册接口
      api.register({
        username: formState.username,
        realName: formState.realName,
        password: formState.password,
        email: formState.email,
        phone: formState.phone,
        education: formState.role === 'teacher' ? formState.education : null,
        major: formState.role === 'teacher' ? formState.major : null,
        subjects: formState.role === 'teacher' ? formState.subjects : null,
        experience: formState.role === 'teacher' ? formState.experience : null,
        bio: formState.role === 'teacher' ? formState.bio : null
      }).then(() => {
        currentStep.value++
        message.success('注册成功')
      }).catch(error => {
        console.error('Registration failed:', error)
        // 错误处理由axios拦截器完成
      }).finally(() => {
        loading.value = false
      })
    }
  }
}

// 上一步
const prevStep = () => {
  if (currentStep.value > 0) {
    currentStep.value--
  }
}

// 前往登录页
const goToLogin = () => {
  router.push({ name: 'Login' })
}

// 返回首页
const goToHome = () => {
  router.push({ name: 'TeacherList' })
}
</script>

<style lang="less" scoped>
@import '../assets/styles/variables.less';

.register-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #1890ff 0%, #722ed1 100%);
  padding: @padding-lg;
  background-size: 400% 400%;
  animation: gradientBG 15s ease infinite;
  
  @keyframes gradientBG {
    0% {
      background-position: 0% 50%;
    }
    50% {
      background-position: 100% 50%;
    }
    100% {
      background-position: 0% 50%;
    }
  }
  
  &:before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: radial-gradient(circle, transparent 20%, rgba(0,0,0,0.1) 20%, rgba(0,0,0,0.1) 80%, transparent 80%, transparent),
                radial-gradient(circle, transparent 20%, rgba(0,0,0,0.1) 20%, rgba(0,0,0,0.1) 80%, transparent 80%, transparent) 50px 50px,
                linear-gradient(rgba(255,255,255,0.1) 2px, transparent 2px),
                linear-gradient(90deg, rgba(255,255,255,0.1) 2px, transparent 2px);
    background-size: 100px 100px, 100px 100px, 50px 50px, 50px 50px;
    animation: patternMove 8s linear infinite;
  }
  
  @keyframes patternMove {
    0% {
      background-position: 0 0, 50px 50px, 0 0, 0 0;
    }
    100% {
      background-position: 100px 0, 150px 50px, 50px 0, 0 50px;
    }
  }
  
  .register-box {
    width: 100%;
    max-width: 520px;
    background: white;
    border-radius: @border-radius-base;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
    padding: @padding-lg;
    position: relative;
    z-index: 1;
    
    .register-header {
      text-align: center;
      margin-bottom: @padding-md;
      
      h2 {
        font-size: 24px;
        color: @primary-color;
        margin-bottom: @padding-xs;
      }
      
      p {
        color: @text-color-secondary;
      }
    }
    
    .register-steps {
      margin-bottom: @padding-lg;
    }
    
    .step-content {
      min-height: 300px;
    }

    .second-step {
      display: flex;
      align-items: center;
      justify-content: center;
    }
    
    .register-success {
      text-align: center;
      padding: @padding-lg 0;
      
      .success-icon {
        font-size: 64px;
        color: @success-color;
        margin-bottom: @padding-md;
      }
      
      h3 {
        font-size: 20px;
        margin-bottom: @padding-sm;
      }
      
      p {
        color: @text-color-secondary;
        margin-bottom: @padding-lg;
      }
      
      .success-actions {
        margin-top: @padding-lg;
      }
    }
    
    .register-footer {
      text-align: center;
      margin-top: @padding-md;
      color: @text-color-secondary;
      
      a {
        color: @primary-color;
        margin-left: @padding-xs;
      }
    }
  }
}
</style> 