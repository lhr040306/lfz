<script setup lang="ts">
import { onMounted, reactive, ref } from "vue";
import { ElMessageBox } from "element-plus";
import { cancelOrder, fetchMyOrders, payOrder } from "@/api/modules/orders";
import type { RentalOrder } from "@/api/types";

// 我的订单：支持支付与取消待支付订单
const loading = ref(false);
const orders = ref<RentalOrder[]>([]);
const total = ref(0);
const query = reactive({
  page: 1,
  size: 10
});

const statusMap: Record<number, { text: string; type: "" | "warning" | "success" | "info" | "danger" }> = {
  10: { text: "待支付", type: "warning" },
  20: { text: "待取车", type: "info" },
  30: { text: "租赁中", type: "" },
  50: { text: "已完成", type: "success" },
  90: { text: "已取消", type: "danger" }
};

async function loadOrders() {
  loading.value = true;
  try {
    const data = await fetchMyOrders(query);
    orders.value = data.records;
    total.value = data.total;
  } finally {
    loading.value = false;
  }
}

async function onPay(id: number) {
  await payOrder(id);
  await loadOrders();
}

async function onCancel(id: number) {
  await ElMessageBox.confirm("确定要取消该订单吗？", "提示", { type: "warning" });
  await cancelOrder(id);
  await loadOrders();
}

onMounted(loadOrders);
</script>

<template>
  <section class="panel">
    <div class="title-row">
      <h2>我的订单</h2>
      <span class="muted">查看订单状态并完成支付</span>
    </div>
    <el-table :data="orders" v-loading="loading" border>
      <el-table-column prop="orderNo" label="订单号" min-width="170" />
      <el-table-column prop="carId" label="车辆ID" width="90" />
      <el-table-column label="租赁时间" min-width="290">
        <template #default="{ row }">
          {{ row.rentStartTime }} ~ {{ row.rentEndTime }}
        </template>
      </el-table-column>
      <el-table-column prop="rentDays" label="天数" width="80" />
      <el-table-column prop="totalAmount" label="订单金额" width="120">
        <template #default="{ row }">{{ row.totalAmount }} 元</template>
      </el-table-column>
      <el-table-column label="状态" width="140">
        <template #default="{ row }">
          <el-tag :type="statusMap[row.orderStatus]?.type || 'info'">
            {{ statusMap[row.orderStatus]?.text || row.orderStatus }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.orderStatus === 10" type="primary" link @click="onPay(row.id)">支付</el-button>
          <el-button v-if="row.orderStatus === 10" type="danger" link @click="onCancel(row.id)">取消</el-button>
          <span v-else class="muted">-</span>
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
.pager {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>

