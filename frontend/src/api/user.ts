import { http } from '@/utils/request'

// 上传头像
export const uploadAvatar = (formData: FormData) => {
  return http.post('/user/avatar', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 更新用户信息
export const updateUserInfo = (data: any) => {
  return http.put('/user/info', data)
}

// 修改密码
export const changePassword = (data: { oldPassword: string; newPassword: string }) => {
  return http.put('/user/password', data)
}

// 获取用户详情
export const getUserDetail = () => {
  return http.get('/user/detail')
}