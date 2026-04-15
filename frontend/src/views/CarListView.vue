<script setup lang="ts">
import { onMounted, reactive, ref } from "vue";
import { useRouter } from "vue-router";
import { fetchCars } from "@/api/modules/cars";
import type { CarInfo } from "@/api/types";
import { useAuthStore } from "@/stores/auth";

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
      <h2>Available Cars</h2>
      <span class="muted">Online booking and instant order creation</span>
    </div>
    <div class="filters">
      <el-input v-model="query.brand" placeholder="Brand" clearable />
      <el-input-number v-model="query.minPrice" :min="0" :controls="false" placeholder="Min/day" />
      <el-input-number v-model="query.maxPrice" :min="0" :controls="false" placeholder="Max/day" />
      <el-button type="primary" @click="loadCars">Search</el-button>
      <el-button @click="resetFilter">Reset</el-button>
    </div>
    <el-skeleton :loading="loading" :rows="6" animated>
      <div class="car-grid">
        <article v-for="car in cars" :key="car.id" class="car-card">
          <img :src="car.coverUrl || 'https://picsum.photos/600/400?car'" alt="car cover" />
          <div class="card-content">
            <h3>{{ car.brand }} {{ car.model }}</h3>
            <p class="muted">{{ car.series || "-" }} | {{ car.gearbox || "-" }} | {{ car.fuelType || "-" }}</p>
            <div class="price-row">
              <strong>¥{{ car.dayRent }}/day</strong>
              <span>Deposit: ¥{{ car.deposit }}</span>
            </div>
            <el-button type="primary" @click="goBook(car.id)">Book Now</el-button>
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
  margin-bottom: 16px;
}

.car-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 14px;
}

.car-card {
  border: 1px solid var(--line);
  border-radius: 14px;
  overflow: hidden;
  background: #fff;
}

.car-card img {
  width: 100%;
  height: 180px;
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

