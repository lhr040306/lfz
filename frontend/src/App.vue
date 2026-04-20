<script setup lang="ts">
import { computed } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useAuthStore } from "@/stores/auth";

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();

// 顶部导航高亮状态
const activePath = computed(() => route.path);
const roleName = computed(() => (authStore.roleCode === "ADMIN" ? "管理员" : "用户"));

function go(path: string) {
  router.push(path);
}

function logout() {
  authStore.logout();
  router.push("/login");
}
</script>

<template>
  <div class="app-root">
    <header class="app-header">
      <div class="app-brand" @click="go('/cars')">汽车租赁管理系统</div>
      <nav class="app-nav">
        <button :class="{ active: activePath.startsWith('/cars') }" @click="go('/cars')">车辆大厅</button>
        <button v-if="authStore.isLoggedIn" :class="{ active: activePath === '/orders' }" @click="go('/orders')">
          我的订单
        </button>
        <button v-if="authStore.isAdmin" :class="{ active: activePath === '/admin/cars' }" @click="go('/admin/cars')">
          车辆管理
        </button>
        <button
          v-if="authStore.isAdmin"
          :class="{ active: activePath === '/admin/customers' }"
          @click="go('/admin/customers')"
        >
          客户管理
        </button>
        <button
          v-if="authStore.isAdmin"
          :class="{ active: activePath === '/admin/orders' }"
          @click="go('/admin/orders')"
        >
          订单管理
        </button>
        <button
          v-if="authStore.isAdmin"
          :class="{ active: activePath === '/admin/stats' }"
          @click="go('/admin/stats')"
        >
          统计报表
        </button>
      </nav>
      <div class="app-user">
        <span v-if="authStore.isLoggedIn">{{ authStore.username }}（{{ roleName }}）</span>
        <button v-if="!authStore.isLoggedIn" @click="go('/login')">登录</button>
        <button v-else @click="logout">退出登录</button>
      </div>
    </header>
    <main class="app-main">
      <router-view />
    </main>
  </div>
</template>
