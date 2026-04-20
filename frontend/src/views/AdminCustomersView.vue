<script setup lang="ts">
import { onMounted, reactive, ref } from "vue";
import { ElMessageBox } from "element-plus";
import {
  adminCreateCustomer,
  adminDeleteCustomer,
  adminFetchCustomers,
  adminUpdateCustomer,
  adminUpdateCustomerStatus,
  type CustomerPayload
} from "@/api/modules/customers";
import type { CustomerInfo } from "@/api/types";

const loading = ref(false);
const customers = ref<CustomerInfo[]>([]);
const total = ref(0);
const dialogVisible = ref(false);
const isEdit = ref(false);
const editingId = ref<number | null>(null);

const query = reactive({
  page: 1,
  size: 10,
  keyword: "",
  status: undefined as number | undefined
});

const form = reactive<CustomerPayload>({
  username: "",
  password: "",
  realName: "",
  phone: "",
  status: 1
});

const statusOptions = [
  { label: "启用", value: 1 },
  { label: "禁用", value: 0 }
];

async function loadCustomers() {
  loading.value = true;
  try {
    const data = await adminFetchCustomers(query);
    customers.value = data.records;
    total.value = data.total;
  } finally {
    loading.value = false;
  }
}

function openCreate() {
  isEdit.value = false;
  editingId.value = null;
  Object.assign(form, {
    username: "",
    password: "",
    realName: "",
    phone: "",
    status: 1
  });
  dialogVisible.value = true;
}

function openEdit(row: CustomerInfo) {
  isEdit.value = true;
  editingId.value = row.id;
  Object.assign(form, {
    username: row.username,
    password: "",
    realName: row.realName,
    phone: row.phone || "",
    status: row.status
  });
  dialogVisible.value = true;
}

async function submitForm() {
  const payload: CustomerPayload = {
    username: form.username,
    realName: form.realName,
    phone: form.phone,
    status: form.status
  };
  if (form.password && form.password.trim()) {
    payload.password = form.password.trim();
  } else if (!isEdit.value) {
    payload.password = "";
  }

  if (isEdit.value && editingId.value) {
    await adminUpdateCustomer(editingId.value, payload);
  } else {
    await adminCreateCustomer(payload);
  }
  dialogVisible.value = false;
  await loadCustomers();
}

async function changeStatus(id: number, status: number) {
  await adminUpdateCustomerStatus(id, status);
  await loadCustomers();
}

async function removeCustomer(id: number) {
  await ElMessageBox.confirm("确定删除该客户吗？", "提示", { type: "warning" });
  await adminDeleteCustomer(id);
  await loadCustomers();
}

onMounted(loadCustomers);
</script>

<template>
  <section class="panel">
    <div class="title-row">
      <h2>客户管理</h2>
      <el-button type="primary" @click="openCreate">新增客户</el-button>
    </div>
    <div class="filters">
      <el-input v-model="query.keyword" placeholder="用户名/姓名/手机号" clearable />
      <el-select v-model="query.status" placeholder="状态筛选" clearable>
        <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
      </el-select>
      <el-button type="primary" @click="loadCustomers">查询</el-button>
    </div>
    <el-table :data="customers" v-loading="loading" border>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="username" label="用户名" width="130" />
      <el-table-column prop="realName" label="姓名" width="120" />
      <el-table-column prop="phone" label="手机号" width="150" />
      <el-table-column prop="createdAt" label="创建时间" min-width="170" />
      <el-table-column label="状态" width="130">
        <template #default="{ row }">
          <el-select :model-value="row.status" @change="(value: string | number) => changeStatus(row.id, Number(value))">
            <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link @click="openEdit(row)">编辑</el-button>
          <el-button type="danger" link @click="removeCustomer(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div class="pager">
      <el-pagination
        v-model:current-page="query.page"
        v-model:page-size="query.size"
        layout="total, prev, pager, next"
        :total="total"
        @current-change="loadCustomers"
      />
    </div>
  </section>

  <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑客户' : '新增客户'" width="620px">
    <el-form :model="form" label-width="110px">
      <el-form-item label="用户名"><el-input v-model="form.username" /></el-form-item>
      <el-form-item :label="isEdit ? '新密码(可选)' : '密码'">
        <el-input v-model="form.password" show-password />
      </el-form-item>
      <el-form-item label="姓名"><el-input v-model="form.realName" /></el-form-item>
      <el-form-item label="手机号"><el-input v-model="form.phone" /></el-form-item>
      <el-form-item label="状态">
        <el-select v-model="form.status">
          <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" @click="submitForm">提交</el-button>
    </template>
  </el-dialog>
</template>

<style scoped>
.filters {
  display: grid;
  grid-template-columns: 1fr 180px auto;
  gap: 10px;
  margin-bottom: 14px;
}

.pager {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

@media (max-width: 860px) {
  .filters {
    grid-template-columns: 1fr;
  }
}
</style>

