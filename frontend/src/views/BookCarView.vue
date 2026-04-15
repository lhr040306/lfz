<script setup lang="ts">
import dayjs from "dayjs";
import { computed, onMounted, reactive, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { fetchCarDetail } from "@/api/modules/cars";
import { createOrder } from "@/api/modules/orders";
import type { CarInfo } from "@/api/types";

// 租车下单页：根据时间区间计算租赁天数与预估金额
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
    ElMessage.warning("请选择有效的租赁时间");
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
    ElMessage.success("下单成功，请在“我的订单”完成支付");
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
      <h2>确认租车</h2>
      <el-button @click="$router.push('/cars')">返回车辆列表</el-button>
    </div>
    <el-skeleton :loading="loading" :rows="5" animated>
      <div v-if="car" class="booking-wrap">
        <div class="car-block">
          <img :src="car.coverUrl || 'https://picsum.photos/640/420?car'" alt="车辆封面" />
          <h3>{{ car.brand }} {{ car.model }}</h3>
          <p class="muted">{{ car.series || "-" }} | {{ car.gearbox || "-" }} | {{ car.fuelType || "-" }}</p>
          <p><strong>{{ car.dayRent }} 元/天</strong> | 押金：{{ car.deposit }} 元</p>
        </div>
        <div class="form-block">
          <el-form label-width="130px">
            <el-form-item label="租赁开始时间">
              <el-date-picker
                v-model="form.rentStartTime"
                type="datetime"
                placeholder="请选择开始时间"
                value-format="YYYY-MM-DD HH:mm:ss"
                format="YYYY-MM-DD HH:mm:ss"
              />
            </el-form-item>
            <el-form-item label="租赁结束时间">
              <el-date-picker
                v-model="form.rentEndTime"
                type="datetime"
                placeholder="请选择结束时间"
                value-format="YYYY-MM-DD HH:mm:ss"
                format="YYYY-MM-DD HH:mm:ss"
              />
            </el-form-item>
            <el-form-item label="备注">
              <el-input v-model="form.remark" type="textarea" :rows="3" />
            </el-form-item>
          </el-form>
          <div class="summary">
            <p>租赁天数：{{ rentDays }} 天</p>
            <p>预估总金额：<strong>{{ estimateTotal.toFixed(2) }} 元</strong></p>
            <el-button type="primary" :loading="submitLoading" @click="submitOrder">提交订单</el-button>
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

