<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { adminFetchCarRank, adminFetchRevenueTrend, adminFetchStatsOverview } from "@/api/modules/stats";
import type { CarRevenueRank, RevenueTrendPoint, StatsOverview } from "@/api/types";

const loading = ref(false);
const overview = ref<StatsOverview>({
  totalRevenue: 0,
  totalExtraAmount: 0,
  totalOrders: 0,
  finishedOrders: 0,
  canceledOrders: 0,
  finishRate: 0
});
const trend = ref<RevenueTrendPoint[]>([]);
const rank = ref<CarRevenueRank[]>([]);

const query = reactive({
  dateRange: [] as string[]
});

const apiParams = computed(() => ({
  startDate: query.dateRange?.[0],
  endDate: query.dateRange?.[1]
}));

const maxTrendRevenue = computed(() => {
  if (!trend.value.length) {
    return 0;
  }
  return Math.max(...trend.value.map((item) => Number(item.revenue)));
});

function barHeight(value: number) {
  if (maxTrendRevenue.value <= 0) {
    return 8;
  }
  return Math.max((value / maxTrendRevenue.value) * 120, 8);
}

async function loadStats() {
  loading.value = true;
  try {
    const [overviewData, trendData, rankData] = await Promise.all([
      adminFetchStatsOverview(apiParams.value),
      adminFetchRevenueTrend(apiParams.value),
      adminFetchCarRank({ ...apiParams.value, limit: 10 })
    ]);
    overview.value = overviewData;
    trend.value = trendData;
    rank.value = rankData;
  } finally {
    loading.value = false;
  }
}

onMounted(loadStats);
</script>

<template>
  <section class="panel">
    <div class="title-row">
      <h2>统计报表</h2>
      <div class="filters">
        <el-date-picker
          v-model="query.dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
        />
        <el-button type="primary" :loading="loading" @click="loadStats">查询</el-button>
      </div>
    </div>

    <div class="cards">
      <article class="card">
        <p>总收入</p>
        <h3>{{ Number(overview.totalRevenue || 0).toFixed(2) }} 元</h3>
      </article>
      <article class="card">
        <p>订单总数</p>
        <h3>{{ overview.totalOrders || 0 }}</h3>
      </article>
      <article class="card">
        <p>完成率</p>
        <h3>{{ Number(overview.finishRate || 0).toFixed(2) }}%</h3>
      </article>
      <article class="card">
        <p>额外费用</p>
        <h3>{{ Number(overview.totalExtraAmount || 0).toFixed(2) }} 元</h3>
      </article>
    </div>

    <div class="block">
      <h3>收入趋势（按天）</h3>
      <div class="trend-wrap">
        <div v-for="item in trend" :key="item.date" class="trend-item">
          <div class="bar" :style="{ height: `${barHeight(Number(item.revenue))}px` }"></div>
          <span class="amount">{{ Number(item.revenue).toFixed(0) }}</span>
          <span class="date">{{ item.date.slice(5) }}</span>
        </div>
      </div>
    </div>

    <div class="block">
      <h3>车辆收入排行（Top 10）</h3>
      <el-table :data="rank" v-loading="loading" border>
        <el-table-column label="排名" width="80">
          <template #default="{ $index }">{{ $index + 1 }}</template>
        </el-table-column>
        <el-table-column prop="brand" label="品牌" width="120" />
        <el-table-column prop="model" label="车型" min-width="160" />
        <el-table-column prop="plateNo" label="车牌号" width="130" />
        <el-table-column prop="orderCount" label="完成订单数" width="120" />
        <el-table-column prop="revenue" label="收入(元)" width="120" />
      </el-table>
    </div>
  </section>
</template>

<style scoped>
.filters {
  display: flex;
  align-items: center;
  gap: 10px;
}

.cards {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}

.card {
  border: 1px solid var(--line);
  border-radius: 12px;
  padding: 14px;
  background: #fff;
}

.card p {
  margin: 0;
  color: var(--muted);
  font-size: 13px;
}

.card h3 {
  margin: 8px 0 0;
  font-size: 22px;
}

.block {
  margin-top: 18px;
}

.block h3 {
  margin: 0 0 10px;
}

.trend-wrap {
  border: 1px solid var(--line);
  border-radius: 12px;
  padding: 12px;
  display: flex;
  align-items: flex-end;
  gap: 8px;
  overflow-x: auto;
  min-height: 190px;
  background: #fff;
}

.trend-item {
  width: 44px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.bar {
  width: 20px;
  border-radius: 8px 8px 0 0;
  background: linear-gradient(180deg, #22c55e 0%, #0f766e 100%);
}

.amount {
  font-size: 12px;
  color: var(--muted);
}

.date {
  font-size: 12px;
}

@media (max-width: 960px) {
  .cards {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .title-row {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>

