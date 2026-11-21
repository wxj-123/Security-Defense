import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'
import fs from 'fs'
import path from 'path'

// 获取证书文件路径
const certPath = path.resolve(fileURLToPath(new URL('.', import.meta.url)), 'frontend.p12')

export default defineConfig({
  plugins: [
    vue(),
    AutoImport({
      resolvers: [ElementPlusResolver()],
      imports: ['vue', 'vue-router', 'pinia'],
      dts: 'src/auto-imports.d.ts',
    }),
    Components({
      resolvers: [ElementPlusResolver()],
      dts: 'src/components.d.ts',
    }),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },
  server: {
    port: 3000,
    https: {
      pfx: fs.readFileSync(certPath),
      passphrase: process.env.KEYSTORE_PASSWORD || '',
    },
    proxy: {
      '/api': {
        target: 'https://localhost:8443',
        changeOrigin: true,
        secure: false, // 允许自签名证书
        ws: false,  // 禁用WebSocket代理
      },
      '/uploads': {
        target: 'https://localhost:8443',
        changeOrigin: true,
        secure: false, // 允许自签名证书
      },
    },
  },
})
