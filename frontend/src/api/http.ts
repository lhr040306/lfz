import axios from "axios";
import { ElMessage } from "element-plus";

interface ApiResponse<T> {
  code: number;
  message: string;
  data: T;
}

const http = axios.create({
  baseURL: "/api/v1",
  timeout: 10000
});

http.interceptors.request.use((config) => {
  const token = localStorage.getItem("token");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

http.interceptors.response.use(
  (response) => {
    const body = response.data as ApiResponse<unknown>;
    if (body && typeof body.code === "number") {
      if (body.code === 0) {
        return body.data;
      }
      ElMessage.error(body.message || "Request failed");
      return Promise.reject(new Error(body.message || "Request failed"));
    }
    return response.data;
  },
  (error) => {
    const status = error?.response?.status;
    if (status === 401) {
      localStorage.removeItem("token");
      localStorage.removeItem("roleCode");
      localStorage.removeItem("username");
      if (window.location.pathname !== "/login") {
        window.location.href = "/login";
      }
      ElMessage.error("Login expired. Please login again.");
      return Promise.reject(error);
    }

    const message = error?.response?.data?.message || error?.message || "Network error";
    ElMessage.error(message);
    return Promise.reject(error);
  }
);

export default http;

