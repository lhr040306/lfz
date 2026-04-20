<script setup lang="ts">
import { onMounted, reactive, ref } from "vue";
import { useRouter } from "vue-router";
import { fetchCars } from "@/api/modules/cars";
import type { CarInfo } from "@/api/types";
import { useAuthStore } from "@/stores/auth";

// 车辆大厅：支持分页与价格筛选
const router = useRouter();
const authStore = useAuthStore();
const loading = ref(false);
const cars = ref<CarInfo[]>([]);
const total = ref(0);

const query = reactive({
  page: 1,
  size: 8,
  brand: "",
  minPrice: undefined as number | undefined,
  maxPrice: undefined as number | undefined
});

async function loadCars() {
  loading.value = true;
  try {
    const data = await fetchCars(query);
    cars.value = data.records;
    total.value = data.total;
  } finally {
    loading.value = false;
  }
}

function resetFilter() {
  query.brand = "";
  query.minPrice = undefined;
  query.maxPrice = undefined;
  query.page = 1;
  loadCars();
}

function goBook(carId: number) {
  if (!authStore.isLoggedIn) {
    router.push("/login");
    return;
  }
  router.push(`/cars/${carId}/book`);
}

onMounted(loadCars);
</script>

<template>
  <section class="panel">
    <div class="title-row">
      <div>
        <h2>可租车辆</h2>
        <span class="muted">在线选车，快速下单</span>
      </div>
      <el-tag type="success" effect="dark">实时库存 {{ total }} 辆</el-tag>
    </div>
    <div class="filters">
      <el-input v-model="query.brand" placeholder="品牌筛选" clearable />
      <el-input-number v-model="query.minPrice" :min="0" :controls="false" placeholder="最低日租" />
      <el-input-number v-model="query.maxPrice" :min="0" :controls="false" placeholder="最高日租" />
      <el-button type="primary" @click="loadCars">查询</el-button>
      <el-button @click="resetFilter">重置</el-button>
    </div>
    <el-skeleton :loading="loading" :rows="6" animated>
      <div class="car-grid">
        <article v-for="car in cars" :key="car.id" class="car-card">
          <img :src="car.coverUrl || 'https://picsum.photos/600/400?car'" alt="车辆封面" />
          <div class="card-content">
            <h3>{{ car.brand }} {{ car.model }}</h3>
            <p class="muted">{{ car.series || "-" }} | {{ car.gearbox || "-" }} | {{ car.fuelType || "-" }}</p>
            <div class="price-row">
              <strong>{{ car.dayRent }} 元/天</strong>
              <span>押金：{{ car.deposit }} 元</span>
            </div>
            <el-button type="primary" round @click="goBook(car.id)">立即租车</el-button>
          </div>
        </article>
      </div>
    </el-skeleton>
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
</template>

<style scoped>
.filters {
  display: grid;
  grid-template-columns: 1.2fr repeat(2, 1fr) auto auto;
  gap: 10px;
  margin-bottom: 18px;
  padding: 12px;
  border: 1px dashed #d8e4f6;
  border-radius: 14px;
  background: #f9fbff;
}

.car-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 14px;
}

.car-card {
  border: 1px solid var(--line);
  border-radius: 16px;
  overflow: hidden;
  background: #fff;
  transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease;
}

.car-card:hover {
  transform: translateY(-4px);
  border-color: #b8cae6;
  box-shadow: 0 16px 28px rgba(20, 44, 83, 0.12);
}

.car-card img {
  width: 100%;
  height: 186px;
  object-fit: cover;
}

.card-content {
  padding: 12px;
}

h3 {
  margin: 0;
  font-size: 18px;
}

.price-row {
  margin: 12px 0;
  display: flex;
  align-items: baseline;
  justify-content: space-between;
}

.price-row strong {
  color: var(--brand-deep);
}

.pager {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

@media (max-width: 860px) {
  .filters {
    grid-template-columns: 1fr 1fr;
  }
}
</style>
