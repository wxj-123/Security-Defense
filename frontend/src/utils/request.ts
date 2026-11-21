import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse, InternalAxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import CryptoJS from 'crypto-js'
import type { ApiResponse } from '@/types'

// 创建axios实例
const service: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json;charset=utf-8',
    'Accept': 'application/json;charset=utf-8'
  }
})

// AES加密密钥
const AES_KEY = import.meta.env.VITE_AES_KEY || 'security-defense-2024'

// 数据加密函数
const encryptData = (data: any): string => {
  const jsonStr = JSON.stringify(data)
  return CryptoJS.AES.encrypt(jsonStr, AES_KEY).toString()
}

// 数据解密函数
const decryptData = (encrypted: string): any => {
  const bytes = CryptoJS.AES.decrypt(encrypted, AES_KEY)
  const decrypted = bytes.toString(CryptoJS.enc.Utf8)
  return JSON.parse(decrypted)
}

// 请求拦截器
service.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const userStore = useUserStore()

    // 添加JWT Token
    if (userStore.token) {
      config.headers.Authorization = `Bearer ${userStore.token}`
    }

    // 添加CSRF Token (后端已禁用CSRF保护，暂时注释掉)
    // const csrfToken = sessionStorage.getItem('csrf-token')
    // if (csrfToken) {
    //   config.headers['X-CSRF-TOKEN'] = csrfToken
    // }

    // 添加请求时间戳，防止缓存
    config.headers['X-Request-Time'] = Date.now().toString()

    // FormData 不进行加密
    const isFormData = config.data instanceof FormData

    // 敏感数据加密（POST、PUT请求）
    if (config.method === 'post' || config.method === 'put') {
      if (config.data && !isFormData && import.meta.env.VITE_ENABLE_ENCRYPTION === 'true') {
        config.data = {
          encrypted: encryptData(config.data)
        }
      }
    }

    return config
  },
  (error) => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  (response: AxiosResponse<ApiResponse>) => {
    const res = response.data
    
    // 如果返回的数据是加密的，进行解密
    if (res.data && typeof res.data === 'string' && res.data.startsWith('encrypted:')) {
      res.data = decryptData(res.data.substring(10))
    }
    
    // 根据业务状态码处理
    if (res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      
      // Token过期，跳转登录
      if (res.code === 401) {
        const userStore = useUserStore()
        userStore.logout()
        window.location.href = '/login'
      }
      
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    
    // 返回完整的 response 对象，保持 Axios 拦截器的类型一致性
    return response
  },
  (error) => {
    console.error('响应错误:', error)
    
    let message = '系统错误'
    
    if (error.response) {
      switch (error.response.status) {
        case 400:
          message = '请求参数错误'
          break
        case 401:
          message = '未授权，请重新登录'
          const userStore = useUserStore()
          userStore.logout()
          window.location.href = '/login'
          break
        case 403:
          message = '拒绝访问'
          break
        case 404:
          message = '请求资源不存在'
          break
        case 500:
          message = '服务器内部错误'
          break
        case 503:
          message = '服务不可用'
          break
        default:
          message = error.response.data?.message || '请求失败'
      }
    } else if (error.request) {
      message = '网络连接失败，请检查网络'
    }
    
    ElMessage.error(message)
    return Promise.reject(error)
  }
)

// 导出请求方法
export default service

// 封装常用请求方法
export const http = {
  get<T = any>(url: string, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
    return service.get(url, config).then(res => res.data)
  },
  
  post<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
    return service.post(url, data, config).then(res => res.data)
  },
  
  put<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
    return service.put(url, data, config).then(res => res.data)
  },
  
  delete<T = any>(url: string, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
    return service.delete(url, config).then(res => res.data)
  }
}

// 为了向后兼容，也导出直接使用 service 的方法
export const request = {
  get: service.get,
  post: service.post,
  put: service.put,
  delete: service.delete
}
