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
    console.log(token)
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
      console.log(error.response)
      switch (error.response.status) {
        case 400:
          message.error(error.response.data.error || '请求参数错误')
          break
        case 401:
          // token过期或未登录
          localStorage.removeItem('token')
          router.push('/login')
          message.error('请重新登录')
          break
        case 403:
          message.error('请登录后访问')
          router.push('/login')
          break
        case 404:
          message.error('请求的资源不存在')
          break
        case 500:
          message.error('服务器错误')
          break
        default:
          message.error(error.response.data.error || '请求失败')
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
  register: (data) => instance.post('/users/register', data),
  
  // 用户资料相关接口 - 注意这里直接使用了/user/profile，因为baseURL已经包含了/api
  getUserProfile: () => instance.get('/user/profile'),
  updateUserProfile: (data) => instance.put('/user/profile', data),
  changePassword: (data) => instance.post('/user/profile/change-password', data),
  
  // 资质管理相关接口
  getQualifications: () => instance.get('/user/qualifications'),
  getQualificationsByStatus: (status) => instance.get(`/user/qualifications/status/${status}`),
  addQualification: (data) => instance.post('/user/qualifications', data),
  deleteQualification: (id) => instance.delete(`/user/qualifications/${id}`),
  
  // 管理员审核资质接口
  reviewQualification: (id, data) => instance.put(`/admin/qualifications/${id}/review`, data),
  
  // 教师管理相关接口
  getAllTeachers: (params) => instance.get('/teachers', { params }),
  getTeacherById: (id) => instance.get(`/teachers/${id}`),
  addTeacher: (data) => instance.post('/teachers', data),
  updateTeacher: (id, data) => instance.put(`/teachers/${id}`, data),
  deleteTeacher: (id) => instance.delete(`/teachers/${id}`),
  getTeacherQualifications: (id) => instance.get(`/teachers/${id}/qualifications`),
  
  // 课程相关接口
  getAllCourses: (params) => instance.get('/courses', { params }),
  getCourseById: (id) => instance.get(`/courses/${id}`),
  getTeacherCourses: (params) => instance.get('/courses/teacher', { params }),
  createCourse: (data) => instance.post('/courses', data),
  updateCourseStatus: (id, status) => instance.put(`/courses/${id}/status?status=${status}`),
  deleteCourse: (id) => instance.delete(`/courses/${id}`),
  updateCourse: (id, data) => instance.put(`/courses/${id}`, data),
  getCourseDetail: (id) => instance.get(`/courses/${id}/detail`),
  
  // 可以继续添加其他API请求方法
}

export default instance
