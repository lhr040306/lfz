<script setup lang="ts">
import { computed } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useAuthStore } from "@/stores/auth";

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();

const activePath = computed(() => route.path);

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
      <div class="app-brand" @click="go('/cars')">Car Rental System</div>
      <nav class="app-nav">
        <button :class="{ active: activePath.startsWith('/cars') }" @click="go('/cars')">Cars</button>
        <button v-if="authStore.isLoggedIn" :class="{ active: activePath === '/orders' }" @click="go('/orders')">
          My Orders
        </button>
        <button v-if="authStore.isAdmin" :class="{ active: activePath === '/admin/cars' }" @click="go('/admin/cars')">
          Admin Cars
        </button>
        <button
          v-if="authStore.isAdmin"
          :class="{ active: activePath === '/admin/orders' }"
          @click="go('/admin/orders')"
        >
          Admin Orders
        </button>
      </nav>
      <div class="app-user">
        <span v-if="authStore.isLoggedIn">{{ authStore.username }} ({{ authStore.roleCode }})</span>
        <button v-if="!authStore.isLoggedIn" @click="go('/login')">Login</button>
        <button v-else @click="logout">Logout</button>
      </div>
    </header>
    <main class="app-main">
      <router-view />
    </main>
  </div>
</template>

