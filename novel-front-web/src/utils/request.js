import axios from 'axios'
import router from '@/router'
import { ElMessage } from 'element-plus'
import {getToken, removeToken, removeNickName, removeUid} from '@/utils/auth'

// 白名单，不需要token的接口
const whiteList = ['/user/login', '/user/register']

// 创建axios实例
const service = axios.create({
  baseURL: process.env.VUE_APP_BASE_API_URL || 'http://localhost:8888/api',
  timeout: 10000,
  withCredentials: true
})

// 请求拦截器
service.interceptors.request.use(
  config => {
    // 检查是否在白名单中
    const isWhiteList = whiteList.some(url => config.url?.includes(url))
    
    // 添加token（白名单接口除外）
    if (!isWhiteList) {
      const token = getToken()
      if (token) {
        // 检查token是否已经包含Bearer前缀，避免重复添加
        if (token.startsWith('Bearer ')) {
          config.headers['Authorization'] = token
        } else {
          config.headers['Authorization'] = `Bearer ${token}`
        }
      }
    }
    
    // 设置请求头（但不覆盖FormData的Content-Type）
    if (!(config.data instanceof FormData)) {
      config.headers['Content-Type'] = 'application/json'
    }
    config.headers['X-Requested-With'] = 'XMLHttpRequest'
    
    console.log('发送请求:', {
      method: config.method?.toUpperCase(),
      url: config.baseURL + config.url,
      headers: config.headers,
      params: config.params,
      data: config.data,
      isWhiteList: isWhiteList,
      isFormData: config.data instanceof FormData
    })
    
    return config
  },
  error => {
    console.error('请求拦截器错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  response => {
    console.log('收到响应:', {
      status: response.status,
      data: response.data,
      headers: response.headers
    })
    
    const res = response.data
    
    // 检查响应数据
    if (!res || typeof res !== 'object') {
      const error = new Error('服务端响应格式异常')
      ElMessage.error('服务端响应格式异常')
      return Promise.reject(error)
    }
    
    // 检查业务状态码
    if (res.code !== "00000") {
      console.warn('业务错误:', res)
      
      // 显示错误消息
      if (res.message) {
        ElMessage.error(res.message)
      }
      
      // 处理登录过期
      if (res.code === 'A0230') {
        removeToken()
        removeNickName()
        removeUid()
        router.push({ path: '/login' })
      }
      
      return Promise.reject(new Error(res.message || '业务处理失败'))
    }
    
    return res
  },
  error => {
    console.error('响应错误详情:', {
      message: error.message,
      code: error.code,
      config: error.config,
      request: error.request,
      response: error.response
    })
    
    let errorMessage = '网络异常'
    
    if (error.response) {
      // 服务器响应了错误状态码
      const { status, data } = error.response
      console.error('HTTP错误:', { status, data })
      
      switch (status) {
        case 400:
          errorMessage = '请求参数错误'
          break
        case 401:
          errorMessage = '请先登录'
          removeToken()
          removeNickName()
          removeUid()
          setTimeout(() => {
            router.push({ path: '/login' })
          }, 1000)
          break
        case 403:
          errorMessage = '权限不足'
          break
        case 404:
          errorMessage = '请求的接口不存在'
          break
        case 500:
          errorMessage = '服务器内部错误'
          break
        default:
          errorMessage = data?.message || `请求失败 (${status})`
      }
    } else if (error.request) {
      // 请求发出但没有收到响应
      console.error('网络请求超时或连接失败')
      errorMessage = '网络连接失败，请检查网络连接'
    } else {
      // 请求配置错误
      console.error('请求配置错误:', error.message)
      errorMessage = '请求配置错误'
    }
    
    ElMessage.error(errorMessage)
    return Promise.reject(new Error(errorMessage))
  }
)

export default service
