import { createRouter, createWebHistory, type RouteRecordRaw } from "vue-router";
import { useAuthStore } from "@/stores/auth";

const routes: RouteRecordRaw[] = [
  {
    path: "/",
    redirect: "/login"
  },
  {
    path: "/login",
    name: "login",
    component: () => import("@/views/LoginView.vue")
  },
  {
    path: "/cars",
    name: "cars",
    component: () => import("@/views/CarListView.vue")
  },
  {
    path: "/cars/:id/book",
    name: "book-car",
    component: () => import("@/views/BookCarView.vue"),
    meta: { requiresAuth: true }
  },
  {
    path: "/orders",
    name: "orders",
    component: () => import("@/views/MyOrdersView.vue"),
    meta: { requiresAuth: true }
  },
  {
    path: "/admin/cars",
    name: "admin-cars",
    component: () => import("@/views/AdminCarsView.vue"),
    meta: { requiresAuth: true, requiresAdmin: true }
  },
  {
    path: "/admin/customers",
    name: "admin-customers",
    component: () => import("@/views/AdminCustomersView.vue"),
    meta: { requiresAuth: true, requiresAdmin: true }
  },
  {
    path: "/admin/orders",
    name: "admin-orders",
    component: () => import("@/views/AdminOrdersView.vue"),
    meta: { requiresAuth: true, requiresAdmin: true }
  },
  {
    path: "/admin/stats",
    name: "admin-stats",
    component: () => import("@/views/AdminStatsView.vue"),
    meta: { requiresAuth: true, requiresAdmin: true }
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

// 路由守卫：控制登录与管理员页面访问权限
router.beforeEach((to) => {
  const authStore = useAuthStore();
  if (to.path === "/login" && authStore.isLoggedIn) {
    return "/cars";
  }

  if (to.meta.requiresAuth && !authStore.isLoggedIn) {
    return "/login";
  }

  if (to.meta.requiresAdmin && !authStore.isAdmin) {
    return "/cars";
  }

  return true;
});

export default router;
