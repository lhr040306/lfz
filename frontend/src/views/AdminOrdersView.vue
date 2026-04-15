<script setup lang="ts">
import { onMounted, reactive, ref } from "vue";
import { ElMessageBox } from "element-plus";
import { adminFetchOrders, adminPickupOrder, adminReturnOrder } from "@/api/modules/orders";
import type { RentalOrder } from "@/api/types";

// 管理员订单管理：处理出车与还车结算
const loading = ref(false);
const orders = ref<RentalOrder[]>([]);
const total = ref(0);
const query = reactive({
  page: 1,
  size: 10,
  status: undefined as number | undefined
});

const statusMap: Record<number, string> = {
  10: "待支付",
  20: "待取车",
  30: "租赁中",
  50: "已完成",
  90: "已取消"
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
  const result = await ElMessageBox.prompt("请输入额外费用（如超时、损坏）", "还车结算", {
    inputValue: "0",
    inputPattern: /^(\d+)(\.\d{1,2})?$/,
    inputErrorMessage: "请输入有效金额"
  });
  await adminReturnOrder(id, Number(result.value));
  await loadOrders();
}

onMounted(loadOrders);
</script>

<template>
  <section class="panel">
    <div class="title-row">
      <h2>订单管理</h2>
      <span class="muted">处理出车与还车结算流程</span>
    </div>
    <div class="filters">
      <el-select v-model="query.status" placeholder="状态筛选" clearable>
        <el-option v-for="(text, key) in statusMap" :key="key" :label="text" :value="Number(key)" />
      </el-select>
      <el-button type="primary" @click="loadOrders">查询</el-button>
    </div>
    <el-table :data="orders" v-loading="loading" border>
      <el-table-column prop="orderNo" label="订单号" min-width="170" />
      <el-table-column prop="userId" label="用户ID" width="90" />
      <el-table-column prop="carId" label="车辆ID" width="90" />
      <el-table-column prop="totalAmount" label="总金额(元)" width="120" />
      <el-table-column label="状态" width="140">
        <template #default="{ row }">
          <el-tag>{{ statusMap[row.orderStatus] || row.orderStatus }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="210" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.orderStatus === 20" type="primary" link @click="pickup(row.id)">确认出车</el-button>
          <el-button v-if="row.orderStatus === 30" type="success" link @click="returnCar(row.id)">还车结算</el-button>
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

