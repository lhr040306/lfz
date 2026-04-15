import { computed, ref } from "vue";
import { defineStore } from "pinia";

export const useAuthStore = defineStore("auth", () => {
  const token = ref<string>(localStorage.getItem("token") || "");
  const username = ref<string>(localStorage.getItem("username") || "");
  const roleCode = ref<string>(localStorage.getItem("roleCode") || "");

  const isLoggedIn = computed(() => Boolean(token.value));
  const isAdmin = computed(() => roleCode.value === "ADMIN");

  function setAuth(payload: { token: string; username: string; roleCode: string }) {
    token.value = payload.token;
    username.value = payload.username;
    roleCode.value = payload.roleCode;
    localStorage.setItem("token", payload.token);
    localStorage.setItem("username", payload.username);
    localStorage.setItem("roleCode", payload.roleCode);
  }

  function logout() {
    token.value = "";
    username.value = "";
    roleCode.value = "";
    localStorage.removeItem("token");
    localStorage.removeItem("username");
    localStorage.removeItem("roleCode");
  }

  return {
    token,
    username,
    roleCode,
    isLoggedIn,
    isAdmin,
    setAuth,
    logout
  };
});

