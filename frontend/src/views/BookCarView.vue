<script setup lang="ts">
import dayjs from "dayjs";
import { computed, onMounted, reactive, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { fetchCarDetail } from "@/api/modules/cars";
import { createOrder } from "@/api/modules/orders";
import type { CarInfo } from "@/api/types";

const route = useRoute();
const router = useRouter();
const loading = ref(false);
const submitLoading = ref(false);
const car = ref<CarInfo | null>(null);

const form = reactive({
  rentStartTime: "",
  rentEndTime: "",
  remark: ""
});

const rentDays = computed(() => {
  if (!form.rentStartTime || !form.rentEndTime) {
    return 0;
  }
  const start = dayjs(form.rentStartTime);
  const end = dayjs(form.rentEndTime);
  const minutes = end.diff(start, "minute");
  if (minutes <= 0) {
    return 0;
  }
  return Math.ceil(minutes / (24 * 60));
});

const estimateTotal = computed(() => {
  if (!car.value || rentDays.value <= 0) {
    return 0;
  }
  return Number(car.value.dayRent) * rentDays.value + Number(car.value.deposit);
});

async function loadCar() {
  const id = Number(route.params.id);
  if (!id) {
    router.push("/cars");
    return;
  }
  loading.value = true;
  try {
    car.value = await fetchCarDetail(id);
  } finally {
    loading.value = false;
  }
}

async function submitOrder() {
  if (!car.value) {
    return;
  }
  if (rentDays.value <= 0) {
    ElMessage.warning("Please select a valid rent time range");
    return;
  }

  submitLoading.value = true;
  try {
    await createOrder({
      carId: car.value.id,
      rentStartTime: form.rentStartTime,
      rentEndTime: form.rentEndTime,
      remark: form.remark
    });
    ElMessage.success("Order created, please continue payment in My Orders");
    router.push("/orders");
  } finally {
    submitLoading.value = false;
  }
}

onMounted(loadCar);
</script>

<template>
  <section class="panel">
    <div class="title-row">
      <h2>Book Car</h2>
      <el-button @click="$router.push('/cars')">Back</el-button>
    </div>
    <el-skeleton :loading="loading" :rows="5" animated>
      <div v-if="car" class="booking-wrap">
        <div class="car-block">
          <img :src="car.coverUrl || 'https://picsum.photos/640/420?car'" alt="car cover" />
          <h3>{{ car.brand }} {{ car.model }}</h3>
          <p class="muted">{{ car.series || "-" }} | {{ car.gearbox || "-" }} | {{ car.fuelType || "-" }}</p>
          <p><strong>¥{{ car.dayRent }}/day</strong> | Deposit: ¥{{ car.deposit }}</p>
        </div>
        <div class="form-block">
          <el-form label-width="130px">
            <el-form-item label="Rent Start">
              <el-date-picker
                v-model="form.rentStartTime"
                type="datetime"
                placeholder="Select start"
                value-format="YYYY-MM-DD HH:mm:ss"
                format="YYYY-MM-DD HH:mm:ss"
              />
            </el-form-item>
            <el-form-item label="Rent End">
              <el-date-picker
                v-model="form.rentEndTime"
                type="datetime"
                placeholder="Select end"
                value-format="YYYY-MM-DD HH:mm:ss"
                format="YYYY-MM-DD HH:mm:ss"
              />
            </el-form-item>
            <el-form-item label="Remark">
              <el-input v-model="form.remark" type="textarea" :rows="3" />
            </el-form-item>
          </el-form>
          <div class="summary">
            <p>Rent days: {{ rentDays }}</p>
            <p>Estimated amount: <strong>¥{{ estimateTotal.toFixed(2) }}</strong></p>
            <el-button type="primary" :loading="submitLoading" @click="submitOrder">Create Order</el-button>
          </div>
        </div>
      </div>
    </el-skeleton>
  </section>
</template>

<style scoped>
.booking-wrap {
  display: grid;
  grid-template-columns: 1fr 1.2fr;
  gap: 18px;
}

.car-block img {
  width: 100%;
  border-radius: 12px;
  height: 220px;
  object-fit: cover;
}

h3 {
  margin: 12px 0 6px;
}

.form-block {
  border: 1px solid var(--line);
  border-radius: 12px;
  padding: 14px;
}

.summary {
  border-top: 1px dashed var(--line);
  padding-top: 12px;
}

@media (max-width: 900px) {
  .booking-wrap {
    grid-template-columns: 1fr;
  }
}
</style>

