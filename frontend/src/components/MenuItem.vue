<!-- eslint-disable vue/no-unused-vars -->
<template>
  <template v-if="!route.meta?.hidden">
    <el-sub-menu
      v-if="route.children && route.children.length > 0"
      :index="getMenuPath(route)"
    >
      <template #title>
        <el-icon v-if="route.meta?.icon">
          <component :is="route.meta.icon" />
        </el-icon>
        <span>{{ route.meta?.title }}</span>
      </template>
      
      <menu-item
        v-for="child in route.children"
        :key="child.path"
        :route="child"
      />
    </el-sub-menu>

    <el-menu-item v-else :index="getMenuPath(route)">
      <el-icon v-if="route.meta?.icon">
        <component :is="route.meta.icon" />
      </el-icon>
      <span>{{ route.meta?.title }}</span>
    </el-menu-item>
  </template>
</template>

<script setup lang="ts">
import type { RouteRecordRaw } from 'vue-router'

interface Props {
  route: RouteRecordRaw
}

const { route } = defineProps<Props>()

const getMenuPath = (route: RouteRecordRaw) => {
  // 如果 path以/开头，直接返回
  if (route.path.startsWith('/')) {
    return route.path
  }
  // 否则返回相对路径
  return route.path
}
</script>