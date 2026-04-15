<script setup lang="ts">
import { onMounted, reactive, ref } from "vue";
import {
  adminCreateCar,
  adminFetchCars,
  adminUpdateCar,
  adminUpdateCarStatus,
  type CarPayload
} from "@/api/modules/cars";
import type { CarInfo } from "@/api/types";

const loading = ref(false);
const dialogVisible = ref(false);
const isEdit = ref(false);
const editingId = ref<number | null>(null);
const cars = ref<CarInfo[]>([]);
const total = ref(0);
const query = reactive({
  page: 1,
  size: 10,
  brand: "",
  status: undefined as number | undefined
});

const form = reactive<CarPayload>({
  brand: "",
  series: "",
  model: "",
  plateNo: "",
  seatCount: 5,
  gearbox: "Auto",
  fuelType: "Gasoline",
  dayRent: 300,
  deposit: 2000,
  status: 1,
  mileage: 0,
  coverUrl: ""
});

const statusOptions = [
  { label: "Available", value: 1 },
  { label: "Maintaining", value: 2 },
  { label: "Offline", value: 3 }
];

async function loadCars() {
  loading.value = true;
  try {
    const data = await adminFetchCars(query);
    cars.value = data.records;
    total.value = data.total;
  } finally {
    loading.value = false;
  }
}

function openCreate() {
  isEdit.value = false;
  editingId.value = null;
  Object.assign(form, {
    brand: "",
    series: "",
    model: "",
    plateNo: "",
    seatCount: 5,
    gearbox: "Auto",
    fuelType: "Gasoline",
    dayRent: 300,
    deposit: 2000,
    status: 1,
    mileage: 0,
    coverUrl: ""
  });
  dialogVisible.value = true;
}

function openEdit(row: CarInfo) {
  isEdit.value = true;
  editingId.value = row.id;
  Object.assign(form, {
    brand: row.brand,
    series: row.series || "",
    model: row.model,
    plateNo: row.plateNo,
    seatCount: row.seatCount,
    gearbox: row.gearbox || "",
    fuelType: row.fuelType || "",
    dayRent: Number(row.dayRent),
    deposit: Number(row.deposit),
    status: row.status,
    mileage: row.mileage,
    coverUrl: row.coverUrl || ""
  });
  dialogVisible.value = true;
}

async function submitForm() {
  if (isEdit.value && editingId.value) {
    await adminUpdateCar(editingId.value, form);
  } else {
    await adminCreateCar(form);
  }
  dialogVisible.value = false;
  await loadCars();
}

async function changeStatus(id: number, status: number) {
  await adminUpdateCarStatus(id, status);
  await loadCars();
}

onMounted(loadCars);
</script>

<template>
  <section class="panel">
    <div class="title-row">
      <h2>Admin - Cars</h2>
      <el-button type="primary" @click="openCreate">New Car</el-button>
    </div>
    <div class="filters">
      <el-input v-model="query.brand" placeholder="Brand filter" clearable />
      <el-select v-model="query.status" placeholder="Status" clearable>
        <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
      </el-select>
      <el-button type="primary" @click="loadCars">Search</el-button>
    </div>
    <el-table :data="cars" v-loading="loading" border>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="brand" label="Brand" width="120" />
      <el-table-column prop="model" label="Model" min-width="160" />
      <el-table-column prop="plateNo" label="Plate" width="130" />
      <el-table-column prop="dayRent" label="Day Rent" width="110" />
      <el-table-column prop="deposit" label="Deposit" width="110" />
      <el-table-column label="Status" width="130">
        <template #default="{ row }">
          <el-select :model-value="row.status" @change="(value: string | number) => changeStatus(row.id, Number(value))">
            <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </template>
      </el-table-column>
      <el-table-column label="Actions" width="120" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link @click="openEdit(row)">Edit</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div class="pager">
      <el-pagination
        v-model:current-page="query.page"
        v-model:page-size="query.size"
        layout="total, prev, pager, next"
        :total="total"
        @current-change="loadCars"
      />
    </div>
  </section>

  <el-dialog v-model="dialogVisible" :title="isEdit ? 'Edit Car' : 'Create Car'" width="680px">
    <el-form :model="form" label-width="120px">
      <el-form-item label="Brand"><el-input v-model="form.brand" /></el-form-item>
      <el-form-item label="Series"><el-input v-model="form.series" /></el-form-item>
      <el-form-item label="Model"><el-input v-model="form.model" /></el-form-item>
      <el-form-item label="Plate"><el-input v-model="form.plateNo" /></el-form-item>
      <el-form-item label="Seat Count"><el-input-number v-model="form.seatCount" :min="2" /></el-form-item>
      <el-form-item label="Gearbox"><el-input v-model="form.gearbox" /></el-form-item>
      <el-form-item label="Fuel Type"><el-input v-model="form.fuelType" /></el-form-item>
      <el-form-item label="Day Rent"><el-input-number v-model="form.dayRent" :min="1" /></el-form-item>
      <el-form-item label="Deposit"><el-input-number v-model="form.deposit" :min="0" /></el-form-item>
      <el-form-item label="Status">
        <el-select v-model="form.status">
          <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="Mileage"><el-input-number v-model="form.mileage" :min="0" /></el-form-item>
      <el-form-item label="Cover URL"><el-input v-model="form.coverUrl" /></el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">Cancel</el-button>
      <el-button type="primary" @click="submitForm">Submit</el-button>
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
