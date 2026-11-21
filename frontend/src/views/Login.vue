<template>
  <div class="login-container">
    <!-- 矩阵数字雨背景 -->
    <canvas ref="matrixCanvas" class="matrix-rain"></canvas>
    
    <!-- 3D六边形网格背景 -->
    <div class="hex-grid">
      <div class="hex-wrapper" v-for="i in 30" :key="i">
        <div class="hexagon" :style="{ animationDelay: `${i * 0.1}s` }"></div>
      </div>
    </div>
    
    <!-- 脉冲波纹 -->
    <div class="pulse-waves">
      <div class="pulse" v-for="i in 4" :key="'pulse-' + i" :style="{ animationDelay: `${i * 0.8}s` }"></div>
    </div>
    
    <!-- 数据流光效 -->
    <div class="data-streams">
      <div class="stream" v-for="i in 8" :key="'stream-' + i" :style="{ 
        left: `${i * 12}%`,
        animationDelay: `${i * 0.3}s`,
        animationDuration: `${3 + Math.random() * 2}s`
      }">
        <div class="stream-particle" v-for="j in 5" :key="j"></div>
      </div>
    </div>
    
    <!-- 扫描线效果 -->
    <div class="scan-lines">
      <div class="scan-line scan-line-1"></div>
      <div class="scan-line scan-line-2"></div>
      <div class="scan-line scan-line-3"></div>
      <div class="scan-line scan-line-4"></div>
      <div class="scan-line scan-line-5"></div>
    </div>
    
    <!-- 水平扫描线 -->
    <div class="horizontal-scan-lines">
      <div class="h-scan-line h-scan-1"></div>
      <div class="h-scan-line h-scan-2"></div>
      <div class="h-scan-line h-scan-3"></div>
      <div class="h-scan-line h-scan-4"></div>
      <div class="h-scan-line h-scan-5"></div>
    </div>
    
    <!-- 全息扫描层 -->
    <div class="holographic-scan"></div>
    
    <!-- 动态光晕 -->
    <div class="dynamic-glow">
      <div class="glow-orb orb-1"></div>
      <div class="glow-orb orb-2"></div>
      <div class="glow-orb orb-3"></div>
    </div>
    
    <div class="login-box">
      <div class="login-header">
        <h1>网络安全防御系统</h1>
        <p>Security Defense System</p>
      </div>
      
      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        class="login-form"
        @keyup.enter="handleLogin"
      >
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入用户名"
            prefix-icon="User"
            size="large"
          />
        </el-form-item>
        
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            prefix-icon="Lock"
            size="large"
            show-password
          />
        </el-form-item>
        
        <el-form-item prop="code">
          <div class="code-input">
            <el-input
              v-model="loginForm.code"
              placeholder="请输入验证码"
              prefix-icon="Picture"
              size="large"
              style="flex: 1"
            />
            <img
              :src="captchaUrl"
              class="captcha-img"
              @click="refreshCaptcha"
              alt="验证码"
            />
          </div>
        </el-form-item>
        
        <el-form-item>
          <el-checkbox v-model="loginForm.remember">记住我</el-checkbox>
        </el-form-item>
        
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            style="width: 100%"
            @click="handleLogin"
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>
      
      <!-- 双因素认证对话框 -->
      <el-dialog
        v-model="showTwoFactor"
        title="双因素认证"
        width="400px"
        :close-on-click-modal="false"
      >
        <el-form :model="twoFactorForm">
          <el-form-item label="验证码">
            <el-input
              v-model="twoFactorForm.code"
              placeholder="请输入6位验证码"
              maxlength="6"
            />
          </el-form-item>
        </el-form>
        
        <template #footer>
          <el-button @click="showTwoFactor = false">取消</el-button>
          <el-button type="primary" @click="handleTwoFactorVerify">确认</el-button>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { verifyTwoFactor } from '@/api/auth'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const loginFormRef = ref<FormInstance>()
const loading = ref(false)
const showTwoFactor = ref(false)
const captchaUrl = ref('/api/auth/captcha?t=' + Date.now())
const matrixCanvas = ref<HTMLCanvasElement>()

let animationFrameId: number

// 矩阵数字雨效果
const initMatrixRain = () => {
  const canvas = matrixCanvas.value
  if (!canvas) return
  
  const ctx = canvas.getContext('2d')
  if (!ctx) return
  
  canvas.width = window.innerWidth
  canvas.height = window.innerHeight
  
  const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789@#$%^&*()セキュリティ防御系统网络安全'
  const charArray = chars.split('')
  const fontSize = 14
  const columns = canvas.width / fontSize
  const drops: number[] = []
  
  for (let i = 0; i < columns; i++) {
    drops[i] = Math.random() * canvas.height / fontSize
  }
  
  const draw = () => {
    ctx.fillStyle = 'rgba(0, 0, 0, 0.05)'
    ctx.fillRect(0, 0, canvas.width, canvas.height)
    
    ctx.fillStyle = '#00ff41'
    ctx.font = `${fontSize}px monospace`
    
    for (let i = 0; i < drops.length; i++) {
      const char = charArray[Math.floor(Math.random() * charArray.length)]
      const x = i * fontSize
      const y = drops[i] * fontSize
      
      // 渐变效果
      const gradient = ctx.createLinearGradient(x, y - fontSize * 10, x, y)
      gradient.addColorStop(0, 'rgba(0, 255, 65, 0)')
      gradient.addColorStop(0.5, 'rgba(0, 255, 65, 0.3)')
      gradient.addColorStop(1, 'rgba(0, 255, 65, 0.8)')
      
      ctx.fillStyle = gradient
      ctx.fillText(char, x, y)
      
      if (y > canvas.height && Math.random() > 0.975) {
        drops[i] = 0
      }
      drops[i]++
    }
    
    animationFrameId = requestAnimationFrame(draw)
  }
  
  draw()
  
  // 窗口大小改变时重新初始化
  const handleResize = () => {
    canvas.width = window.innerWidth
    canvas.height = window.innerHeight
  }
  
  window.addEventListener('resize', handleResize)
}

onMounted(() => {
  initMatrixRain()
})

onUnmounted(() => {
  if (animationFrameId) {
    cancelAnimationFrame(animationFrameId)
  }
})

const loginForm = reactive({
  username: '',
  password: '',
  code: '',
  remember: false
})

const twoFactorForm = reactive({
  code: '',
  token: ''
})

const loginRules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度为3-20个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为6-20个字符', trigger: 'blur' }
  ]
  // 暂时禁用验证码验证
  // code: [
  //   { required: true, message: '请输入验证码', trigger: 'blur' },
  //   { len: 4, message: '验证码长度为4位', trigger: 'blur' }
  // ]
}

// 刷新验证码
const refreshCaptcha = () => {
  captchaUrl.value = '/api/auth/captcha?t=' + Date.now()
}

// 处理登录
const handleLogin = async () => {
  if (!loginFormRef.value) return
  
  await loginFormRef.value.validate(async (valid: boolean) => {
    if (!valid) return
    
    loading.value = true
    try {
      console.log('开始登录...')
      const res = await userStore.login(loginForm)
      console.log('登录响应:', res)
      
      // 检查是否需要双因素认证
      if (res.data.requireTwoFactor) {
        twoFactorForm.token = res.data.tempToken || ''
        showTwoFactor.value = true
        loading.value = false
        return
      }
      
      ElMessage.success('登录成功')
      
      // 跳转到redirect参数指定的页面或首页
      const redirect = (route.query.redirect as string) || '/'
      console.log('准备跳转到:', redirect)
      
      // 使用 nextTick 确保状态更新后再跳转
      await router.push(redirect)
      console.log('跳转完成')
    } catch (error: any) {
      console.error('登录失败:', error)
      ElMessage.error(error.message || '登录失败')
      refreshCaptcha()
    } finally {
      loading.value = false
    }
  })
}

// 处理双因素认证
const handleTwoFactorVerify = async () => {
  if (!twoFactorForm.code) {
    ElMessage.warning('请输入验证码')
    return
  }
  
  try {
    const res = await verifyTwoFactor(twoFactorForm)
    userStore.token = res.data.token
    localStorage.setItem('token', res.data.token)
    
    ElMessage.success('验证成功')
    showTwoFactor.value = false
    
    const redirect = (route.query.redirect as string) || '/'
    router.push(redirect)
  } catch (error: any) {
    ElMessage.error(error.message || '验证失败')
  }
}
</script>

<style scoped>
/* 主容器 */
.login-container {
  position: relative;
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100vh;
  background: #000000;
  overflow: hidden;
}

/* 矩阵数字雨 */
.matrix-rain {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 1;
  opacity: 0.3;
}

/* 3D六边形网格 */
.hex-grid {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 2;
  display: grid;
  grid-template-columns: repeat(auto-fill, 100px);
  gap: 20px;
  padding: 20px;
  opacity: 0.15;
  transform: perspective(1000px) rotateX(60deg) translateY(-20%);
  transform-style: preserve-3d;
}

.hex-wrapper {
  position: relative;
  animation: floatHex 8s ease-in-out infinite;
}

.hexagon {
  width: 60px;
  height: 60px;
  background: linear-gradient(135deg, rgba(0, 255, 255, 0.1), rgba(0, 100, 255, 0.05));
  clip-path: polygon(50% 0%, 100% 25%, 100% 75%, 50% 100%, 0% 75%, 0% 25%);
  border: 1px solid rgba(0, 255, 255, 0.3);
  animation: pulseHex 3s ease-in-out infinite;
  box-shadow: 0 0 20px rgba(0, 255, 255, 0.2);
}

/* 脉冲波纹 */
.pulse-waves {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  z-index: 3;
  pointer-events: none;
}

.pulse {
  position: absolute;
  top: 50%;
  left: 50%;
  width: 500px;
  height: 500px;
  margin-left: -250px;
  margin-top: -250px;
  border: 2px solid rgba(0, 255, 255, 0.5);
  border-radius: 50%;
  animation: pulseExpand 4s ease-out infinite;
  box-shadow: 0 0 30px rgba(0, 255, 255, 0.3), inset 0 0 30px rgba(0, 255, 255, 0.1);
}

/* 数据流光效 */
.data-streams {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 4;
  pointer-events: none;
}

.stream {
  position: absolute;
  top: 0;
  width: 2px;
  height: 100%;
  overflow: hidden;
}

.stream-particle {
  position: absolute;
  top: -10%;
  width: 100%;
  height: 50px;
  background: linear-gradient(to bottom, transparent, #00ffff, transparent);
  animation: streamFlow 3s linear infinite;
  box-shadow: 0 0 10px #00ffff;
}

.stream-particle:nth-child(2) {
  animation-delay: 0.6s;
  top: -20%;
}

.stream-particle:nth-child(3) {
  animation-delay: 1.2s;
  top: -30%;
}

.stream-particle:nth-child(4) {
  animation-delay: 1.8s;
  top: -40%;
}

.stream-particle:nth-child(5) {
  animation-delay: 2.4s;
  top: -50%;
}

/* 扫描线效果 */
.scan-lines {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 5;
  pointer-events: none;
  overflow: hidden;
}

.scan-line {
  position: absolute;
  width: 100%;
  height: 2px;
  background: linear-gradient(
    to right,
    transparent 0%,
    rgba(0, 255, 255, 0.1) 10%,
    rgba(0, 255, 255, 0.3) 50%,
    rgba(0, 255, 255, 0.1) 90%,
    transparent 100%
  );
  box-shadow:
    0 0 10px rgba(0, 255, 255, 0.3),
    0 0 20px rgba(0, 255, 255, 0.15),
    0 0 30px rgba(0, 255, 255, 0.08);
  opacity: 0;
}

.scan-line-1 {
  animation: scanDown 4s linear infinite;
}

.scan-line-2 {
  background: linear-gradient(
    to right,
    transparent 0%,
    rgba(0, 180, 255, 0.1) 10%,
    rgba(0, 180, 255, 0.3) 50%,
    rgba(0, 180, 255, 0.1) 90%,
    transparent 100%
  );
  box-shadow:
    0 0 10px rgba(0, 180, 255, 0.3),
    0 0 20px rgba(0, 180, 255, 0.15),
    0 0 30px rgba(0, 180, 255, 0.08);
  animation: scanDown 5s linear infinite;
  animation-delay: 0.8s;
}

.scan-line-3 {
  height: 3px;
  background: linear-gradient(
    to right,
    transparent 0%,
    rgba(0, 255, 136, 0.15) 10%,
    rgba(0, 255, 136, 0.35) 50%,
    rgba(0, 255, 136, 0.15) 90%,
    transparent 100%
  );
  box-shadow:
    0 0 15px rgba(0, 255, 136, 0.35),
    0 0 30px rgba(0, 255, 136, 0.2),
    0 0 45px rgba(0, 255, 136, 0.1);
  animation: scanDown 6s linear infinite;
  animation-delay: 1.6s;
}

.scan-line-4 {
  height: 1px;
  background: linear-gradient(
    to right,
    transparent 0%,
    rgba(255, 100, 100, 0.15) 10%,
    rgba(255, 100, 100, 0.35) 50%,
    rgba(255, 100, 100, 0.15) 90%,
    transparent 100%
  );
  box-shadow:
    0 0 8px rgba(255, 100, 100, 0.35),
    0 0 16px rgba(0, 255, 255, 0.2);
  animation: scanDown 7s linear infinite;
  animation-delay: 2.4s;
}

.scan-line-5 {
  height: 2px;
  background: linear-gradient(
    to right,
    transparent 0%,
    rgba(100, 200, 255, 0.1) 10%,
    rgba(100, 200, 255, 0.3) 50%,
    rgba(100, 200, 255, 0.1) 90%,
    transparent 100%
  );
  box-shadow:
    0 0 10px rgba(100, 200, 255, 0.3),
    0 0 20px rgba(100, 200, 255, 0.15);
  animation: scanDown 5.5s linear infinite;
  animation-delay: 3.2s;
}

/* 水平扫描线 */
.horizontal-scan-lines {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 5;
  pointer-events: none;
  overflow: hidden;
}

.h-scan-line {
  position: absolute;
  width: 2px;
  height: 100%;
  background: linear-gradient(
    to bottom,
    transparent 0%,
    rgba(0, 255, 255, 0.1) 10%,
    rgba(0, 255, 255, 0.3) 50%,
    rgba(0, 255, 255, 0.1) 90%,
    transparent 100%
  );
  box-shadow:
    0 0 10px rgba(0, 255, 255, 0.3),
    0 0 20px rgba(0, 255, 255, 0.15),
    0 0 30px rgba(0, 255, 255, 0.08);
  opacity: 0;
}

.h-scan-1 {
  animation: scanRight 4.5s linear infinite;
}

.h-scan-2 {
  background: linear-gradient(
    to bottom,
    transparent 0%,
    rgba(255, 100, 200, 0.1) 10%,
    rgba(255, 100, 200, 0.3) 50%,
    rgba(255, 100, 200, 0.1) 90%,
    transparent 100%
  );
  box-shadow:
    0 0 10px rgba(255, 100, 200, 0.3),
    0 0 20px rgba(255, 100, 200, 0.15),
    0 0 30px rgba(255, 100, 200, 0.08);
  animation: scanRight 5.5s linear infinite;
  animation-delay: 1s;
}

.h-scan-3 {
  width: 3px;
  background: linear-gradient(
    to bottom,
    transparent 0%,
    rgba(255, 200, 0, 0.15) 10%,
    rgba(255, 200, 0, 0.35) 50%,
    rgba(255, 200, 0, 0.15) 90%,
    transparent 100%
  );
  box-shadow:
    0 0 15px rgba(255, 200, 0, 0.35),
    0 0 30px rgba(255, 200, 0, 0.2),
    0 0 45px rgba(255, 200, 0, 0.1);
  animation: scanRight 6.5s linear infinite;
  animation-delay: 2s;
}

.h-scan-4 {
  width: 1px;
  background: linear-gradient(
    to bottom,
    transparent 0%,
    rgba(100, 255, 100, 0.15) 10%,
    rgba(100, 255, 100, 0.35) 50%,
    rgba(100, 255, 100, 0.15) 90%,
    transparent 100%
  );
  box-shadow:
    0 0 8px rgba(100, 255, 100, 0.35),
    0 0 16px rgba(100, 255, 100, 0.2);
  animation: scanRight 7.5s linear infinite;
  animation-delay: 3s;
}

.h-scan-5 {
  width: 2px;
  background: linear-gradient(
    to bottom,
    transparent 0%,
    rgba(150, 100, 255, 0.1) 10%,
    rgba(150, 100, 255, 0.3) 50%,
    rgba(150, 100, 255, 0.1) 90%,
    transparent 100%
  );
  box-shadow:
    0 0 10px rgba(150, 100, 255, 0.3),
    0 0 20px rgba(150, 100, 255, 0.15);
  animation: scanRight 5s linear infinite;
  animation-delay: 0.5s;
}

/* 全息扫描层 */
.holographic-scan {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 6;
  background: linear-gradient(
    180deg,
    transparent 0%,
    rgba(0, 255, 255, 0.03) 50%,
    transparent 100%
  );
  animation: holographicMove 6s linear infinite;
  pointer-events: none;
}

/* 故障效果遮罩 */
.glitch-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 6;
  pointer-events: none;
  opacity: 0.8;
}

.glitch-line {
  position: absolute;
  width: 100%;
  height: 2px;
  background: rgba(0, 255, 255, 0.8);
  opacity: 0;
  animation: glitchLine 8s linear infinite;
  box-shadow: 0 0 10px #00ffff;
}

.glitch-line:nth-child(1) {
  top: 10%;
  animation-delay: 0s;
}

.glitch-line:nth-child(2) {
  top: 30%;
  animation-delay: 2s;
}

.glitch-line:nth-child(3) {
  top: 50%;
  animation-delay: 4s;
}

.glitch-line:nth-child(4) {
  top: 70%;
  animation-delay: 6s;
}

.glitch-line:nth-child(5) {
  top: 90%;
  animation-delay: 1s;
}

/* 动态光晕 */
.dynamic-glow {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 7;
  pointer-events: none;
  overflow: hidden;
}

.glow-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.4;
}

.orb-1 {
  width: 400px;
  height: 400px;
  background: radial-gradient(circle, rgba(0, 255, 255, 0.6) 0%, transparent 70%);
  top: -10%;
  left: -10%;
  animation: orbFloat1 15s ease-in-out infinite;
}

.orb-2 {
  width: 500px;
  height: 500px;
  background: radial-gradient(circle, rgba(0, 100, 255, 0.5) 0%, transparent 70%);
  bottom: -15%;
  right: -10%;
  animation: orbFloat2 18s ease-in-out infinite;
}

.orb-3 {
  width: 300px;
  height: 300px;
  background: radial-gradient(circle, rgba(0, 255, 136, 0.5) 0%, transparent 70%);
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  animation: orbFloat3 12s ease-in-out infinite;
}

/* 登录框 */
.login-box {
  position: relative;
  z-index: 100;
  width: 450px;
  padding: 50px 45px;
  background: rgba(0, 20, 40, 0.3);
  border-radius: 20px;
  border: 1px solid rgba(0, 255, 255, 0.3);
  box-shadow: 
    0 8px 32px rgba(0, 0, 0, 0.3),
    0 0 40px rgba(0, 255, 255, 0.1);
  backdrop-filter: blur(20px);
  overflow: hidden;
}

.login-header {
  text-align: center;
  margin-bottom: 35px;
  position: relative;
}

.login-header h1 {
  font-size: 32px;
  color: #00ffff;
  margin: 0 0 12px;
  text-shadow: 
    0 0 20px rgba(0, 255, 255, 0.8),
    0 0 40px rgba(0, 255, 255, 0.4),
    0 0 60px rgba(0, 255, 255, 0.2);
  font-weight: 700;
  letter-spacing: 3px;
}

.login-header p {
  font-size: 14px;
  color: #88ccff;
  margin: 0;
  letter-spacing: 2px;
  text-transform: uppercase;
  font-weight: 300;
  text-shadow: 0 0 10px rgba(136, 204, 255, 0.5);
}

.login-form {
  margin-top: 25px;
  position: relative;
  z-index: 1;
}

/* Element Plus 样式覆盖 */
.login-form :deep(.el-form-item) {
  margin-bottom: 24px;
}

.login-form :deep(.el-input__wrapper) {
  background: rgba(0, 30, 60, 0.4) !important;
  border: 1px solid rgba(0, 255, 255, 0.25) !important;
  border-radius: 12px !important;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.2) !important;
  padding: 12px 15px !important;
}

.login-form :deep(.el-input__wrapper:hover) {
  background: rgba(0, 30, 60, 0.4) !important;
  border: 1px solid rgba(0, 255, 255, 0.25) !important;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.2) !important;
}

.login-form :deep(.el-input__wrapper.is-focus) {
  background: rgba(0, 30, 60, 0.4) !important;
  border: 1px solid rgba(0, 255, 255, 0.25) !important;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.2) !important;
}

.login-form :deep(.el-input__inner) {
  color: #e6f7ff;
  font-size: 15px;
  letter-spacing: 0.5px;
}

.login-form :deep(.el-input__inner::placeholder) {
  color: #6b8fad;
}

.login-form :deep(.el-input__prefix) {
  color: #00ffff;
  font-size: 18px;
}

.login-form :deep(.el-checkbox__label) {
  color: #88ccff;
  font-size: 14px;
}

.login-form :deep(.el-checkbox__inner) {
  background: rgba(0, 30, 60, 0.5);
  border-color: rgba(0, 255, 255, 0.35);
  width: 18px;
  height: 18px;
}

.login-form :deep(.el-checkbox__input.is-checked .el-checkbox__inner) {
  background: linear-gradient(135deg, #00ffff, #0080ff);
  border-color: #00ffff;
  box-shadow: 0 0 12px rgba(0, 255, 255, 0.4);
}

.login-form :deep(.el-button--primary) {
  background: linear-gradient(135deg, rgba(0, 180, 216, 0.6) 0%, rgba(0, 102, 204, 0.6) 100%) !important;
  border: 1px solid rgba(0, 255, 255, 0.3) !important;
  border-radius: 12px !important;
  font-size: 16px !important;
  font-weight: 600 !important;
  letter-spacing: 2px !important;
  height: 48px !important;
  box-shadow: 0 4px 16px rgba(0, 180, 216, 0.3) !important;
}

.login-form :deep(.el-button--primary:hover),
.login-form :deep(.el-button--primary:focus),
.login-form :deep(.el-button--primary:active) {
  background: linear-gradient(135deg, rgba(0, 180, 216, 0.6) 0%, rgba(0, 102, 204, 0.6) 100%) !important;
  border: 1px solid rgba(0, 255, 255, 0.3) !important;
  box-shadow: 0 4px 16px rgba(0, 180, 216, 0.3) !important;
}

.login-form :deep(.el-button--primary:active) {
  transform: translateY(-1px);
}

.code-input {
  display: flex;
  gap: 12px;
}

.captcha-img {
  width: 130px;
  height: 46px;
  cursor: pointer;
  border-radius: 10px;
  border: 1px solid rgba(0, 255, 255, 0.25);
  background: rgba(0, 30, 60, 0.4);
}

.captcha-img:hover,
.captcha-img:focus {
  border: 1px solid rgba(0, 255, 255, 0.25);
  background: rgba(0, 30, 60, 0.4);
}

/* 对话框样式 */
:deep(.el-dialog) {
  background: linear-gradient(135deg, rgba(0, 20, 40, 0.95), rgba(0, 40, 80, 0.9));
  border: 2px solid rgba(0, 255, 255, 0.4);
  border-radius: 20px;
  box-shadow: 
    0 20px 60px rgba(0, 0, 0, 0.8),
    0 0 80px rgba(0, 255, 255, 0.3);
  backdrop-filter: blur(20px);
}

:deep(.el-dialog__header) {
  border-bottom: 1px solid rgba(0, 255, 255, 0.2);
}

:deep(.el-dialog__title) {
  color: #00ffff;
  font-size: 18px;
  font-weight: 600;
  text-shadow: 0 0 10px rgba(0, 255, 255, 0.5);
}

:deep(.el-dialog__body) {
  color: #e6f7ff;
}

:deep(.el-form-item__label) {
  color: #88ccff;
}

/* 动画关键帧 */
@keyframes floatHex {
  0%, 100% {
    transform: translateY(0) rotate(0deg);
  }
  50% {
    transform: translateY(-10px) rotate(5deg);
  }
}

@keyframes pulseHex {
  0%, 100% {
    opacity: 0.3;
    transform: scale(1);
  }
  50% {
    opacity: 0.6;
    transform: scale(1.05);
  }
}

@keyframes pulseExpand {
  0% {
    transform: translate(-50%, -50%) scale(0.5);
    opacity: 0.8;
  }
  100% {
    transform: translate(-50%, -50%) scale(2);
    opacity: 0;
  }
}

@keyframes streamFlow {
  0% {
    transform: translateY(0);
  }
  100% {
    transform: translateY(120vh);
  }
}

@keyframes scanDown {
  0% {
    top: -5%;
    opacity: 0;
  }
  5% {
    opacity: 0.4;
  }
  95% {
    opacity: 0.4;
  }
  100% {
    top: 105%;
    opacity: 0;
  }
}

@keyframes scanRight {
  0% {
    left: -5%;
    opacity: 0;
  }
  5% {
    opacity: 0.4;
  }
  95% {
    opacity: 0.4;
  }
  100% {
    left: 105%;
    opacity: 0;
  }
}

@keyframes holographicMove {
  0%, 100% {
    transform: translateY(-100%);
  }
  50% {
    transform: translateY(100%);
  }
}

@keyframes orbFloat1 {
  0%, 100% {
    transform: translate(0, 0);
  }
  25% {
    transform: translate(100px, 50px);
  }
  50% {
    transform: translate(50px, 100px);
  }
  75% {
    transform: translate(-50px, 50px);
  }
}

@keyframes orbFloat2 {
  0%, 100% {
    transform: translate(0, 0);
  }
  25% {
    transform: translate(-80px, -40px);
  }
  50% {
    transform: translate(-40px, -80px);
  }
  75% {
    transform: translate(40px, -40px);
  }
}

@keyframes orbFloat3 {
  0%, 100% {
    transform: translate(-50%, -50%) scale(1);
    opacity: 0.4;
  }
  50% {
    transform: translate(-50%, -50%) scale(1.2);
    opacity: 0.6;
  }
}
</style>
