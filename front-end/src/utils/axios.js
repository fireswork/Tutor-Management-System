import axios from 'axios'
import { message } from 'ant-design-vue'
import router from '../router'  // 请确保路径正确

// 创建axios实例
const instance = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 5000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
instance.interceptors.request.use(
  config => {
    // 从localStorage获取token
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
instance.interceptors.response.use(
  response => {
    return response.data
  },
  error => {
    if (error.response) {
      switch (error.response.status) {
        case 401:
          // token过期或未登录
          localStorage.removeItem('token')
          router.push('/login')
          message.error('请重新登录')
          break
        case 403:
          message.error('没有权限访问')
          break
        case 404:
          message.error('请求的资源不存在')
          break
        case 500:
          message.error('服务器错误')
          break
        default:
          message.error(error.response.data.message || '请求失败')
      }
    } else {
      message.error('网络错误，请检查您的网络连接')
    }
    return Promise.reject(error)
  }
)

// API请求方法
export const api = {
  login: (data) => instance.post('/users/login', data),
  // 可以继续添加其他API请求方法
}

export default instance
