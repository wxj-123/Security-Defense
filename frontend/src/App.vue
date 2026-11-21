<template>
  <div id="app">
    <router-view />
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { getCsrfToken } from '@/api/auth'

onMounted(async () => {
  // 获取CSRF Token (后端已禁用CSRF保护，暂时注释掉)
  // try {
  //   const res = await getCsrfToken()
  //   sessionStorage.setItem('csrf-token', res.data.token)
  // } catch (error) {
  //   console.error('获取CSRF Token失败:', error)
  // }
})
</script>

<style>
#app {
  width: 100%;
  height: 100%;
}
</style>
