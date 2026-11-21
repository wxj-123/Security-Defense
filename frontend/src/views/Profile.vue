<template>
  <div class="profile-container">
    <el-card>
      <template #header>
        <span>个人中心</span>
      </template>
      
      <el-row :gutter="20">
        <!-- 左侧：头像上传 -->
        <el-col :span="8">
          <div class="avatar-section">
            <div class="avatar-wrapper">
              <el-avatar :size="150" :src="userStore.user?.avatar">
                {{ userStore.user?.username?.charAt(0).toUpperCase() }}
              </el-avatar>
              <div class="avatar-overlay" @click="showUploadDialog = true">
                <el-icon :size="30"><Camera /></el-icon>
                <span>更换头像</span>
              </div>
            </div>
            <div class="avatar-tips">
              <p>支持 JPG、PNG 格式</p>
              <p>文件大小不超过 2MB</p>
              <p>建议尺寸 200x200 像素</p>
            </div>
          </div>
        </el-col>
        
        <!-- 右侧：基本信息 -->
        <el-col :span="16">
          <el-form :model="userForm" label-width="100px">
            <el-form-item label="用户名">
              <el-input v-model="userForm.username" disabled />
            </el-form-item>
            
            <el-form-item label="邮箱">
              <el-input v-model="userForm.email" />
            </el-form-item>
            
            <el-form-item label="角色">
              <el-tag v-for="role in userStore.user?.roles" :key="role" style="margin-right: 10px;">
                {{ role }}
              </el-tag>
            </el-form-item>
            
            <el-form-item label="权限">
              <el-tag 
                v-for="perm in userStore.user?.permissions" 
                :key="perm" 
                type="success"
                style="margin-right: 10px;"
              >
                {{ perm }}
              </el-tag>
            </el-form-item>
            
            <el-divider />
            
            <el-form-item label="修改密码">
              <el-button @click="showPasswordDialog = true">修改密码</el-button>
            </el-form-item>
            
            <el-form-item>
              <el-button type="primary" @click="saveProfile">保存信息</el-button>
            </el-form-item>
          </el-form>
        </el-col>
      </el-row>
    </el-card>
    
    <!-- 头像上传对话框 -->
    <el-dialog v-model="showUploadDialog" title="上传头像" width="500px">
      <div class="upload-section">
        <el-upload
          class="avatar-uploader"
          :auto-upload="false"
          :show-file-list="false"
          :on-change="handleFileChange"
          drag
        >
          <el-icon v-if="!imageUrl" class="avatar-uploader-icon"><Plus /></el-icon>
          <img v-else :src="imageUrl" class="avatar-preview" />
        </el-upload>
        <div class="upload-tips">
          <p>点击或拖拽文件到此处上传</p>
          <p class="tip-text">支持 JPG、PNG 格式，文件大小不超过 2MB</p>
        </div>
      </div>
      <template #footer>
        <el-button @click="showUploadDialog = false">取消</el-button>
        <el-button type="primary" @click="confirmUpload" :disabled="!imageUrl" :loading="uploading">确定</el-button>
      </template>
    </el-dialog>
    
    <!-- 修改密码对话框 -->
    <el-dialog v-model="showPasswordDialog" title="修改密码" width="500px">
      <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="100px">
        <el-form-item label="原密码" prop="oldPassword">
          <el-input v-model="passwordForm.oldPassword" type="password" show-password />
        </el-form-item>
        
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="passwordForm.newPassword" type="password" show-password />
        </el-form-item>
        
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="passwordForm.confirmPassword" type="password" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showPasswordDialog = false">取消</el-button>
        <el-button type="primary" @click="changePassword">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { ElMessage, type FormInstance } from 'element-plus'
import { Camera, Plus } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { uploadAvatar, updateUserInfo, changePassword as changePasswordApi } from '@/api/user'

const userStore = useUserStore()

const showUploadDialog = ref(false)
const showPasswordDialog = ref(false)
const imageUrl = ref('')
const currentFile = ref<File | null>(null)
const uploading = ref(false)
const passwordFormRef = ref<FormInstance>()

// 用户表单
const userForm = reactive({
  username: userStore.user?.username || '',
  email: userStore.user?.email || ''
})

// 密码表单
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 处理文件选择
const handleFileChange = (file: any) => {
  const rawFile = file.raw
  
  // 验证文件类型
  const isImage = rawFile.type === 'image/jpeg' || rawFile.type === 'image/png'
  if (!isImage) {
    ElMessage.error('头像只能是 JPG 或 PNG 格式!')
    return
  }
  
  // 验证文件大小
  const isLt2M = rawFile.size / 1024 / 1024 < 2
  if (!isLt2M) {
    ElMessage.error('头像大小不能超过 2MB!')
    return
  }
  
  // 保存文件引用
  currentFile.value = rawFile
  
  // 预览图片
  const reader = new FileReader()
  reader.onload = (e) => {
    imageUrl.value = e.target?.result as string
  }
  reader.readAsDataURL(rawFile)
}

// 密码验证规则
const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入原密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' },
    { 
      pattern: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)[^]{6,}$/,
      message: '密码必须包含大小写字母和数字',
      trigger: 'blur'
    }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (rule: any, value: any, callback: any) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 确认上传
const confirmUpload = async () => {
  if (!currentFile.value) {
    ElMessage.warning('请先选择图片')
    return
  }

  uploading.value = true

  try {
    const formData = new FormData()
    formData.append('file', currentFile.value)

    const response = await uploadAvatar(formData)
    const res = response.data

    console.log('头像上传响应:', res)

    // 兼容两种响应格式
    let avatarUrl = ''
    if (res.code === 200 && res.data) {
      // 标准 ApiResponse 格式
      avatarUrl = res.data.url
    } else if (res.url) {
      // 直接返回数据格式 (兼容旧版本)
      avatarUrl = res.url
    }

    if (avatarUrl) {
      ElMessage.success('头像更新成功')
      console.log('新头像URL:', avatarUrl)
      userStore.updateAvatar(avatarUrl)
      showUploadDialog.value = false
      imageUrl.value = ''
      currentFile.value = null
    } else {
      ElMessage.error(res.message || '上传失败')
    }
  } catch (error: any) {
    console.error('上传头像失败:', error)
    ElMessage.error(error.response?.data?.message || '上传失败，请稍后重试')
  } finally {
    uploading.value = false
  }
}

// 保存个人信息
const saveProfile = async () => {
  try {
    const response = await updateUserInfo({
      email: userForm.email
    })
    const res = response.data
    
    if (res.code === 200) {
      userStore.updateUserInfo({ email: userForm.email })
      ElMessage.success('信息更新成功')
    }
  } catch (error) {
    ElMessage.error('更新失败，请稍后重试')
  }
}

// 修改密码
const changePassword = async () => {
  if (!passwordFormRef.value) return
  
  await passwordFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const response = await changePasswordApi({
          oldPassword: passwordForm.oldPassword,
          newPassword: passwordForm.newPassword
        })
        const res = response.data
        
        if (res.code === 200) {
          ElMessage.success('密码修改成功，请重新登录')
          
          // 清空表单
          passwordForm.oldPassword = ''
          passwordForm.newPassword = ''
          passwordForm.confirmPassword = ''
          showPasswordDialog.value = false
          
          // 延迟后退出登录
          setTimeout(() => {
            userStore.logout()
            window.location.href = '/login'
          }, 1500)
        }
      } catch (error: any) {
        ElMessage.error(error.message || '密码修改失败')
      }
    }
  })
}
</script>

<style scoped>
.profile-container {
  padding: 20px;
}

.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px;
}

.avatar-wrapper {
  position: relative;
  margin-bottom: 20px;
  cursor: pointer;
}

.avatar-wrapper:hover .avatar-overlay {
  opacity: 1;
}

.avatar-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  border-radius: 50%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: white;
  opacity: 0;
  transition: opacity 0.3s;
}

.avatar-overlay span {
  margin-top: 8px;
  font-size: 14px;
}

.avatar-tips {
  text-align: center;
  color: #909399;
  font-size: 12px;
  line-height: 1.8;
}

.upload-section {
  text-align: center;
}

.avatar-uploader {
  margin: 0 auto;
}

.avatar-uploader :deep(.el-upload) {
  border: 1px dashed var(--el-border-color);
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: var(--el-transition-duration-fast);
}

.avatar-uploader :deep(.el-upload:hover) {
  border-color: var(--el-color-primary);
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 200px;
  height: 200px;
  text-align: center;
  line-height: 200px;
}

.avatar-preview {
  width: 200px;
  height: 200px;
  display: block;
  object-fit: cover;
}

.upload-tips {
  margin-top: 20px;
  color: #606266;
}

.tip-text {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}
</style>
