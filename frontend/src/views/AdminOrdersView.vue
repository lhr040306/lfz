<script setup lang="ts">
import { onMounted, reactive, ref } from "vue";
import { ElMessageBox } from "element-plus";
import { adminFetchOrders, adminPickupOrder, adminReturnOrder } from "@/api/modules/orders";
import type { RentalOrder } from "@/api/types";

const loading = ref(false);
const orders = ref<RentalOrder[]>([]);
const total = ref(0);
const query = reactive({
  page: 1,
  size: 10,
  status: undefined as number | undefined
});

const statusMap: Record<number, string> = {
  10: "Pending Pay",
  20: "Pending Pickup",
  30: "Renting",
  50: "Completed",
  90: "Canceled"
};

async function loadOrders() {
  loading.value = true;
  try {
    const data = await adminFetchOrders(query);
    orders.value = data.records;
    total.value = data.total;
  } finally {
    loading.value = false;
  }
}

async function pickup(id: number) {
  await adminPickupOrder(id);
  await loadOrders();
}

async function returnCar(id: number) {
  const result = await ElMessageBox.prompt("Input extra amount", "Return Settle", {
    inputValue: "0",
    inputPattern: /^(\d+)(\.\d{1,2})?$/,
    inputErrorMessage: "Invalid amount"
  });
  await adminReturnOrder(id, Number(result.value));
  await loadOrders();
}

onMounted(loadOrders);
</script>

<template>
  <section class="panel">
    <div class="title-row">
      <h2>Admin - Orders</h2>
      <span class="muted">Pickup and return settlement management</span>
    </div>
    <div class="filters">
      <el-select v-model="query.status" placeholder="Status filter" clearable>
        <el-option v-for="(text, key) in statusMap" :key="key" :label="text" :value="Number(key)" />
      </el-select>
      <el-button type="primary" @click="loadOrders">Search</el-button>
    </div>
    <el-table :data="orders" v-loading="loading" border>
      <el-table-column prop="orderNo" label="Order No" min-width="170" />
      <el-table-column prop="userId" label="User ID" width="90" />
      <el-table-column prop="carId" label="Car ID" width="90" />
      <el-table-column prop="totalAmount" label="Total" width="120">
        <template #default="{ row }">¥{{ row.totalAmount }}</template>
      </el-table-column>
      <el-table-column label="Status" width="140">
        <template #default="{ row }">
          <el-tag>{{ statusMap[row.orderStatus] || row.orderStatus }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="Actions" width="210" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.orderStatus === 20" type="primary" link @click="pickup(row.id)">Confirm Pickup</el-button>
          <el-button v-if="row.orderStatus === 30" type="success" link @click="returnCar(row.id)">Return Settle</el-button>
          <span v-if="![20, 30].includes(row.orderStatus)" class="muted">-</span>
        </template>
      </el-table-column>
    </el-table>
    <div class="pager">
      <el-pagination
        v-model:current-page="query.page"
        v-model:page-size="query.size"
        layout="total, prev, pager, next"
        :total="total"
        @current-change="loadOrders"
      />
    </div>
  </section>
</template>

<style scoped>
.filters {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 14px;
}

.pager {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>

