<template>
  <div class="login-container animate__animated animate__fadeIn">
    <div class="floating-shapes">
      <div class="shape"></div>
      <div class="shape"></div>
      <div class="shape"></div>
      <div class="shape"></div>
      <div class="shape"></div>
      <div class="shape"></div>
      <div class="shape"></div>
      <div class="shape"></div>
      <div class="shape"></div>
      <div class="shape"></div>
    </div>
    <div class="login-box animate__animated animate__zoomIn">
      <div class="login-header">
        <h2>家教管理系统</h2>
        <p>登录您的账户</p>
      </div>
      
      <a-form 
        :model="formState" 
        name="login-form" 
        :rules="rules"
        @finish="onFinish"
        layout="vertical"
      >
        <a-form-item name="username" label="用户名/邮箱">
          <a-input v-model:value="formState.username" placeholder="请输入用户名或邮箱">
            <template #prefix><user-outlined /></template>
          </a-input>
        </a-form-item>
        
        <a-form-item name="password" label="密码">
          <a-input-password v-model:value="formState.password" placeholder="请输入密码">
            <template #prefix><lock-outlined /></template>
          </a-input-password>
        </a-form-item>
        
        <div class="login-options">
          <a-checkbox v-model:checked="formState.remember">记住我</a-checkbox>
          <a href="#" class="forgot-password" @click.prevent="handleForgotPassword">忘记密码？</a>
        </div>
        
        <a-form-item>
          <a-button type="primary" html-type="submit" :loading="loading" block>登录</a-button>
        </a-form-item>
        
        <div class="login-divider">
          <span>或</span>
        </div>
        
        <a-form-item>
          <a-button @click="handleRegister" block>注册新账号</a-button>
        </a-form-item>
      </a-form>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { UserOutlined, LockOutlined } from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'

const router = useRouter()
const route = useRoute()
const loading = ref(false)

// 表单数据
const formState = reactive({
  username: '',
  password: '',
  remember: false
})

// 表单验证规则
const rules = {
  username: [
    { required: true, message: '请输入用户名或邮箱', trigger: 'blur' },
    { min: 3, message: '用户名长度至少为3个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少为6个字符', trigger: 'blur' }
  ]
}

// 登录表单提交
const onFinish = (values) => {
  loading.value = true
  
  // 模拟登录请求
  setTimeout(() => {
    // 模拟登录成功
    localStorage.setItem('token', 'mock-token-xxx')
    localStorage.setItem('userName', values.username)
    
    // 模拟用户角色，实际项目中应该从后端获取
    const isTeacher = Math.random() > 0.5 // 随机决定是否为教师用户
    localStorage.setItem('isTeacher', isTeacher.toString())
    
    loading.value = false
    message.success('登录成功')
    
    // 如果有重定向，则跳转到重定向地址
    if (route.query.redirect) {
      router.push(route.query.redirect)
    } else {
      router.push({ name: 'TeacherList' })
    }
  }, 1500)
}

// 注册按钮点击
const handleRegister = () => {
  router.push({ name: 'Register' })
}

// 忘记密码点击
const handleForgotPassword = () => {
  message.info('忘记密码功能正在开发中...')
}
</script>

<style lang="less" scoped>
@import '../assets/styles/variables.less';

.login-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background-image: linear-gradient(-45deg, #ee7752, #e73c7e, #23a6d5, #23d5ab);
  background-size: 400% 400%;
  animation: gradientBG 10s ease infinite;
  padding: @padding-lg;
  position: relative;
  overflow: hidden;
  
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
  
  .floating-shapes {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    z-index: 0;
    overflow: hidden;
  }
  
  .shape {
    position: absolute;
    background-color: rgba(255, 255, 255, 0.15);
    animation-duration: 12s;
    animation-timing-function: linear;
    animation-iteration-count: infinite;
    box-shadow: 0 0 20px rgba(255, 255, 255, 0.3);
    border: 1px solid rgba(255, 255, 255, 0.2);
    backdrop-filter: blur(2px);
    opacity: 0.8;
    
    &:nth-child(1) {
      width: 80px;
      height: 80px;
      left: 10%;
      top: 20%;
      border-radius: 50%;
      background-color: rgba(238, 119, 82, 0.2);
      animation-name: float-up-1;
      animation-delay: 0s;
      transform: translateY(50vh);
    }
    
    &:nth-child(2) {
      width: 120px;
      height: 120px;
      right: 15%;
      top: 15%;
      border-radius: 30% 70% 70% 30% / 30% 30% 70% 70%;
      background-color: rgba(231, 60, 126, 0.2);
      animation-name: float-up-2;
      animation-duration: 18s;
      animation-delay: 2s;
      transform: translateY(20vh);
    }
    
    &:nth-child(3) {
      width: 150px;
      height: 150px;
      left: 20%;
      bottom: 15%;
      border-radius: 39% 61% 61% 39% / 38% 37% 63% 62%;
      background-color: rgba(35, 166, 213, 0.2);
      animation-name: float-up-3;
      animation-duration: 15s;
      animation-delay: 4s;
      transform: translateY(30vh);
    }
    
    &:nth-child(4) {
      width: 100px;
      height: 100px;
      right: 10%;
      bottom: 25%;
      border-radius: 42% 58% 43% 57% / 59% 38% 62% 41%;
      background-color: rgba(35, 213, 171, 0.2);
      animation-name: float-up-4;
      animation-duration: 20s;
      animation-delay: 6s;
      transform: translateY(0vh);
    }
    
    &:nth-child(5) {
      width: 70px;
      height: 70px;
      left: 40%;
      top: 30%;
      border-radius: 50%;
      background-color: rgba(238, 119, 82, 0.15);
      animation-name: float-up-5;
      animation-duration: 13s;
      animation-delay: 3s;
      transform: translateY(20vh);
    }
    
    &:nth-child(6) {
      width: 90px;
      height: 90px;
      right: 30%;
      bottom: 40%;
      border-radius: 63% 37% 37% 63% / 35% 38% 62% 65%;
      background-color: rgba(231, 60, 126, 0.15);
      animation-name: float-up-6;
      animation-duration: 16s;
      animation-delay: 1s;
      transform: translateY(40vh);
    }
    
    &:nth-child(7) {
      width: 65px;
      height: 65px;
      left: 50%;
      top: 40%;
      border-radius: 52% 48% 23% 77% / 46% 68% 32% 54%;
      background-color: rgba(35, 213, 171, 0.2);
      animation-name: float-up-1;
      animation-duration: 14s;
      animation-delay: 1.5s;
      transform: translateY(5vh);
    }
    
    &:nth-child(8) {
      width: 110px;
      height: 110px;
      right: 25%;
      top: 50%;
      border-radius: 22% 78% 33% 67% / 76% 31% 69% 24%;
      background-color: rgba(238, 119, 82, 0.2);
      animation-name: float-up-2;
      animation-duration: 17s;
      animation-delay: 3.5s;
      transform: translateY(15vh);
    }
    
    &:nth-child(9) {
      width: 85px;
      height: 85px;
      left: 30%;
      bottom: 30%;
      border-radius: 51% 49% 58% 42% / 34% 71% 29% 66%;
      background-color: rgba(231, 60, 126, 0.15);
      animation-name: float-up-3;
      animation-duration: 19s;
      animation-delay: 5s;
      transform: translateY(25vh);
    }
    
    &:nth-child(10) {
      width: 75px;
      height: 75px;
      right: 40%;
      bottom: 20%;
      border-radius: 50%;
      background-color: rgba(35, 166, 213, 0.15);
      animation-name: float-up-4;
      animation-duration: 16s;
      animation-delay: 0.5s;
      transform: translateY(15vh);
    }
  }
  
  @keyframes float-up-1 {
    0% { transform: translateY(50vh) rotate(0deg); }
    100% { transform: translateY(-150vh) rotate(360deg); }
  }
  
  @keyframes float-up-2 {
    0% { transform: translateY(20vh) rotate(0deg); }
    100% { transform: translateY(-180vh) rotate(-360deg); }
  }
  
  @keyframes float-up-3 {
    0% { transform: translateY(30vh) rotate(0deg); }
    100% { transform: translateY(-170vh) rotate(360deg); }
  }
  
  @keyframes float-up-4 {
    0% { transform: translateY(0vh) rotate(0deg); }
    100% { transform: translateY(-200vh) rotate(-360deg); }
  }
  
  @keyframes float-up-5 {
    0% { transform: translateY(20vh) rotate(0deg); }
    100% { transform: translateY(-180vh) rotate(360deg); }
  }
  
  @keyframes float-up-6 {
    0% { transform: translateY(40vh) rotate(0deg); }
    100% { transform: translateY(-160vh) rotate(-360deg); }
  }
  
  &:before, &:after {
    content: '';
    position: absolute;
    width: 200vw;
    height: 200vh;
    top: -50vh;
    left: -50vw;
    z-index: 0;
    background: radial-gradient(circle, rgba(255, 255, 255, 0.1) 0%, rgba(255, 255, 255, 0) 70%);
    border-radius: 40%;
    transform-origin: center center;
  }
  
  &:before {
    animation: rotate 15s linear infinite;
  }
  
  &:after {
    animation: rotate 25s linear infinite reverse;
    opacity: 0.7;
  }
  
  @keyframes rotate {
    0% {
      transform: rotate(0deg);
    }
    100% {
      transform: rotate(360deg);
    }
  }
  
  .login-box {
    width: 100%;
    max-width: 420px;
    background: rgba(255, 255, 255, 0.9);
    border-radius: @border-radius-base;
    box-shadow: 0 10px 50px rgba(0, 0, 0, 0.3);
    padding: @padding-lg;
    position: relative;
    z-index: 1;
    backdrop-filter: blur(10px);
    border: 1px solid rgba(255, 255, 255, 0.3);
    transform: translateZ(0);
    animation: pulse 2s infinite;
    
    @keyframes pulse {
      0% {
        box-shadow: 0 10px 50px rgba(0, 0, 0, 0.3);
      }
      70% {
        box-shadow: 0 10px 50px rgba(0, 0, 0, 0.5);
      }
      100% {
        box-shadow: 0 10px 50px rgba(0, 0, 0, 0.3);
      }
    }
    
    .login-header {
      text-align: center;
      margin-bottom: @padding-lg;
      
      h2 {
        font-size: 28px;
        background: linear-gradient(90deg, #e73c7e, #23a6d5);
        -webkit-background-clip: text;
        -webkit-text-fill-color: transparent;
        margin-bottom: @padding-xs;
      }
      
      p {
        color: @text-color-secondary;
      }
    }
    
    .login-options {
      display: flex;
      justify-content: space-between;
      margin-bottom: @padding-lg;
      
      .forgot-password {
        color: @primary-color;
      }
    }
    
    .login-divider {
      position: relative;
      text-align: center;
      margin: @padding-md 0;
      
      &:before {
        content: '';
        position: absolute;
        top: 50%;
        left: 0;
        right: 0;
        height: 1px;
        background: @border-color-split;
      }
      
      span {
        position: relative;
        padding: 0 @padding-xs;
        color: @text-color-secondary;
      }
    }
  }
}
</style> 