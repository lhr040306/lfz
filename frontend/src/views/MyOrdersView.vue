<script setup lang="ts">
import { onMounted, reactive, ref } from "vue";
import { ElMessageBox } from "element-plus";
import { cancelOrder, fetchMyOrders, payOrder } from "@/api/modules/orders";
import type { RentalOrder } from "@/api/types";

const loading = ref(false);
const orders = ref<RentalOrder[]>([]);
const total = ref(0);
const query = reactive({
  page: 1,
  size: 10
});

const statusMap: Record<number, { text: string; type: "" | "warning" | "success" | "info" | "danger" }> = {
  10: { text: "Pending Pay", type: "warning" },
  20: { text: "Pending Pickup", type: "info" },
  30: { text: "Renting", type: "" },
  50: { text: "Completed", type: "success" },
  90: { text: "Canceled", type: "danger" }
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
  await ElMessageBox.confirm("Cancel this order?", "Confirm", { type: "warning" });
  await cancelOrder(id);
  await loadOrders();
}

onMounted(loadOrders);
</script>

<template>
  <section class="panel">
    <div class="title-row">
      <h2>My Orders</h2>
      <span class="muted">Manage payment and check rental progress</span>
    </div>
    <el-table :data="orders" v-loading="loading" border>
      <el-table-column prop="orderNo" label="Order No" min-width="170" />
      <el-table-column prop="carId" label="Car ID" width="90" />
      <el-table-column label="Rent Time" min-width="290">
        <template #default="{ row }">
          {{ row.rentStartTime }} ~ {{ row.rentEndTime }}
        </template>
      </el-table-column>
      <el-table-column prop="rentDays" label="Days" width="80" />
      <el-table-column prop="totalAmount" label="Amount" width="120">
        <template #default="{ row }">¥{{ row.totalAmount }}</template>
      </el-table-column>
      <el-table-column label="Status" width="140">
        <template #default="{ row }">
          <el-tag :type="statusMap[row.orderStatus]?.type || 'info'">
            {{ statusMap[row.orderStatus]?.text || row.orderStatus }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="Actions" width="220" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.orderStatus === 10" type="primary" link @click="onPay(row.id)">Pay</el-button>
          <el-button v-if="row.orderStatus === 10" type="danger" link @click="onCancel(row.id)">Cancel</el-button>
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

