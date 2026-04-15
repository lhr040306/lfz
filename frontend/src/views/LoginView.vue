<script setup lang="ts">
import { reactive, ref } from "vue";
import { ElMessage } from "element-plus";
import { useRouter } from "vue-router";
import { login, register } from "@/api/modules/auth";
import { useAuthStore } from "@/stores/auth";

const router = useRouter();
const authStore = useAuthStore();
const activeTab = ref<"login" | "register">("login");
const loading = ref(false);

const loginForm = reactive({
  username: "",
  password: ""
});

const registerForm = reactive({
  username: "",
  password: "",
  realName: "",
  phone: ""
});

async function onLogin() {
  loading.value = true;
  try {
    const data = await login(loginForm);
    authStore.setAuth({
      token: data.token,
      username: data.username,
      roleCode: data.roleCode
    });
    ElMessage.success("Login successful");
    router.push("/cars");
  } finally {
    loading.value = false;
  }
}

async function onRegister() {
  loading.value = true;
  try {
    await register(registerForm);
    ElMessage.success("Register successful, please login");
    activeTab.value = "login";
    loginForm.username = registerForm.username;
    loginForm.password = "";
  } finally {
    loading.value = false;
  }
}
</script>

<template>
  <div class="auth-wrap">
    <div class="panel auth-panel">
      <h2>Account Access</h2>
      <p class="muted">User default: register by yourself. Admin default: admin / admin123</p>
      <el-tabs v-model="activeTab" class="auth-tabs">
        <el-tab-pane label="Login" name="login">
          <el-form :model="loginForm" label-width="90px">
            <el-form-item label="Username">
              <el-input v-model="loginForm.username" placeholder="Enter username" />
            </el-form-item>
            <el-form-item label="Password">
              <el-input v-model="loginForm.password" show-password placeholder="Enter password" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="loading" @click="onLogin">Login</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
        <el-tab-pane label="Register" name="register">
          <el-form :model="registerForm" label-width="90px">
            <el-form-item label="Username">
              <el-input v-model="registerForm.username" placeholder="4-20 chars" />
            </el-form-item>
            <el-form-item label="Password">
              <el-input v-model="registerForm.password" show-password placeholder="6-20 chars" />
            </el-form-item>
            <el-form-item label="Real Name">
              <el-input v-model="registerForm.realName" placeholder="Enter your name" />
            </el-form-item>
            <el-form-item label="Phone">
              <el-input v-model="registerForm.phone" placeholder="Optional phone number" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="loading" @click="onRegister">Register</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<style scoped>
.auth-wrap {
  display: flex;
  justify-content: center;
  padding-top: 40px;
}

.auth-panel {
  width: 100%;
  max-width: 520px;
}

h2 {
  margin: 0 0 4px;
}
</style>

