<script setup lang="ts">
import { reactive, ref } from "vue";
import { ElMessage } from "element-plus";
import { useRouter } from "vue-router";
import { login, register } from "@/api/modules/auth";
import { useAuthStore } from "@/stores/auth";

const router = useRouter();
const authStore = useAuthStore();
// 登录/注册双标签页
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
    ElMessage.success({
      message: "登录成功",
      duration: 1000
    });
    router.push("/cars");
  } finally {
    loading.value = false;
  }
}

async function onRegister() {
  loading.value = true;
  try {
    await register(registerForm);
    ElMessage.success("注册成功，请登录");
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
      <h2>账号登录</h2>
      <p class="muted">普通用户请先注册，管理员默认账号：admin / admin123</p>
      <el-tabs v-model="activeTab" class="auth-tabs">
        <el-tab-pane label="登录" name="login">
          <el-form :model="loginForm" label-width="90px">
            <el-form-item label="用户名">
              <el-input v-model="loginForm.username" placeholder="请输入用户名" />
            </el-form-item>
            <el-form-item label="密码">
              <el-input v-model="loginForm.password" show-password placeholder="请输入密码" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="loading" @click="onLogin">登录</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
        <el-tab-pane label="注册" name="register">
          <el-form :model="registerForm" label-width="90px">
            <el-form-item label="用户名">
              <el-input v-model="registerForm.username" placeholder="4-20 位字符" />
            </el-form-item>
            <el-form-item label="密码">
              <el-input v-model="registerForm.password" show-password placeholder="6-20 位字符" />
            </el-form-item>
            <el-form-item label="真实姓名">
              <el-input v-model="registerForm.realName" placeholder="请输入真实姓名" />
            </el-form-item>
            <el-form-item label="手机号">
              <el-input v-model="registerForm.phone" placeholder="可选，11位手机号" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="loading" @click="onRegister">注册</el-button>
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
