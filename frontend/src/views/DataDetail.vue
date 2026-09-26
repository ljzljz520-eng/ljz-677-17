<template>
  <div class="space-y-6">
    <!-- 页面标题 -->
    <div class="card">
      <div class="flex items-center justify-between">
        <div>
          <div class="flex items-center mb-2">
            <el-button link @click="goBack" class="mr-2">
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" />
              </svg>
            </el-button>
            <h1 class="text-2xl font-bold text-gray-800">数据详情</h1>
          </div>
          <p class="text-gray-500">批次号：{{ batchNo }}</p>
        </div>
        <el-button type="primary" @click="reportData" :loading="reporting">
          <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
              d="M7 16a4 4 0 01-.88-7.903A5 5 0 1115.9 6L16 6a5 5 0 011 9.9M15 13l-3-3m0 0l-3 3m3-3v12" />
          </svg>
          上报数据
        </el-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="grid grid-cols-4 gap-4">
      <div class="card flex items-center">
        <div class="w-12 h-12 bg-blue-100 rounded-xl flex items-center justify-center mr-4">
          <svg class="w-6 h-6 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
              d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
          </svg>
        </div>
        <div>
          <p class="text-2xl font-bold text-gray-800">{{ stats.total }}</p>
          <p class="text-gray-500 text-sm">总记录数</p>
        </div>
      </div>
      <div class="card flex items-center">
        <div class="w-12 h-12 bg-yellow-100 rounded-xl flex items-center justify-center mr-4">
          <svg class="w-6 h-6 text-yellow-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
              d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
        </div>
        <div>
          <p class="text-2xl font-bold text-gray-800">{{ stats.pending }}</p>
          <p class="text-gray-500 text-sm">待上报</p>
        </div>
      </div>
      <div class="card flex items-center">
        <div class="w-12 h-12 bg-green-100 rounded-xl flex items-center justify-center mr-4">
          <svg class="w-6 h-6 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
          </svg>
        </div>
        <div>
          <p class="text-2xl font-bold text-gray-800">{{ stats.success }}</p>
          <p class="text-gray-500 text-sm">已上报</p>
        </div>
      </div>
      <div class="card flex items-center">
        <div class="w-12 h-12 bg-red-100 rounded-xl flex items-center justify-center mr-4">
          <svg class="w-6 h-6 text-red-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
          </svg>
        </div>
        <div>
          <p class="text-2xl font-bold text-gray-800">{{ stats.failed }}</p>
          <p class="text-gray-500 text-sm">上报失败</p>
        </div>
      </div>
    </div>

    <!-- 数据表格 -->
    <div class="card">
      <div class="flex items-center justify-between mb-4">
        <h2 class="text-lg font-semibold text-gray-700">数据列表</h2>
        <el-select v-model="statusFilter" placeholder="状态筛选" clearable style="width: 140px" @change="fetchData">
          <el-option label="全部" value="" />
          <el-option label="待上报" :value="0" />
          <el-option label="已上报" :value="1" />
          <el-option label="上报失败" :value="2" />
        </el-select>
      </div>

      <el-table v-loading="loading" :data="dataList" stripe style="width: 100%">
        <el-table-column prop="dataCode" label="数据编号" width="140" />
        <el-table-column prop="name" label="姓名" width="100" />
        <el-table-column prop="idCard" label="身份证号" width="180">
          <template #default="{ row }">
            {{ maskIdCard(row.idCard) }}
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" width="130">
          <template #default="{ row }">
            {{ maskPhone(row.phone) }}
          </template>
        </el-table-column>
        <el-table-column prop="amount" label="金额" width="120" align="right">
          <template #default="{ row }">
            <span class="font-medium">{{ formatAmount(row.amount) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="address" label="地址" min-width="200" show-overflow-tooltip />
        <el-table-column prop="reportStatus" label="上报状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getReportStatusType(row.reportStatus)" size="small">
              {{ getReportStatusText(row.reportStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="reportMessage" label="上报信息" min-width="180" show-overflow-tooltip />
      </el-table>

      <!-- 分页 -->
      <div class="mt-4 flex justify-end">
        <el-pagination
          v-model:current-page="pagination.pageNum"
          v-model:page-size="pagination.pageSize"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <!-- 上报结果弹窗 -->
    <el-dialog
      v-model="reportDialogVisible"
      title="数据上报结果"
      width="600px"
      :close-on-click-modal="false"
    >
      <div v-if="reportResult">
        <div class="grid grid-cols-3 gap-4 mb-6">
          <div class="bg-blue-50 rounded-lg p-4 text-center">
            <p class="text-2xl font-bold text-blue-600">{{ reportResult.totalCount }}</p>
            <p class="text-gray-500 text-sm">上报总数</p>
          </div>
          <div class="bg-green-50 rounded-lg p-4 text-center">
            <p class="text-2xl font-bold text-green-600">{{ reportResult.successCount }}</p>
            <p class="text-gray-500 text-sm">成功</p>
          </div>
          <div class="bg-red-50 rounded-lg p-4 text-center">
            <p class="text-2xl font-bold text-red-600">{{ reportResult.failCount }}</p>
            <p class="text-gray-500 text-sm">失败</p>
          </div>
        </div>

        <div v-if="reportResult.errorList && reportResult.errorList.length > 0">
          <h4 class="font-medium text-gray-700 mb-3">上报失败数据</h4>
          <el-table :data="reportResult.errorList" stripe max-height="250" size="small">
            <el-table-column prop="dataCode" label="数据编号" width="120" />
            <el-table-column prop="name" label="姓名" width="100" />
            <el-table-column prop="errorMsg" label="错误原因" />
          </el-table>
        </div>
      </div>

      <template #footer>
        <el-button @click="reportDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { excelApi } from '@/api'

const route = useRoute()
const router = useRouter()

const batchNo = computed(() => route.params.batchNo)
const loading = ref(false)
const dataList = ref([])
const statusFilter = ref('')
const reporting = ref(false)
const reportDialogVisible = ref(false)
const reportResult = ref(null)

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const stats = reactive({
  total: 0,
  pending: 0,
  success: 0,
  failed: 0
})

const getReportStatusType = (status) => {
  const types = { 0: 'info', 1: 'success', 2: 'danger' }
  return types[status] || 'info'
}

const getReportStatusText = (status) => {
  const texts = { 0: '待上报', 1: '已上报', 2: '上报失败' }
  return texts[status] || '未知'
}

const maskIdCard = (idCard) => {
  if (!idCard) return '-'
  return idCard.replace(/^(.{6})(.*)(.{4})$/, '$1********$3')
}

const maskPhone = (phone) => {
  if (!phone) return '-'
  return phone.replace(/^(.{3})(.*)(.{4})$/, '$1****$3')
}

const formatAmount = (amount) => {
  if (amount === null || amount === undefined) return '-'
  return '¥' + Number(amount).toLocaleString('zh-CN', { minimumFractionDigits: 2 })
}

const fetchData = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize
    }
    if (statusFilter.value !== '') {
      params.reportStatus = statusFilter.value
    }

    const res = await excelApi.getDataByBatch(batchNo.value, params)
    dataList.value = res.data.records || []
    pagination.total = res.data.total || 0

    // 计算统计数据
    updateStats()
  } catch (error) {
    // 错误已在拦截器中处理
  } finally {
    loading.value = false
  }
}

const updateStats = () => {
  stats.total = pagination.total
  stats.pending = dataList.value.filter(d => d.reportStatus === 0).length
  stats.success = dataList.value.filter(d => d.reportStatus === 1).length
  stats.failed = dataList.value.filter(d => d.reportStatus === 2).length
}

const handleSizeChange = (size) => {
  pagination.pageSize = size
  fetchData()
}

const handleCurrentChange = (page) => {
  pagination.pageNum = page
  fetchData()
}

const goBack = () => {
  router.push('/records')
}

const reportData = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要将待上报的数据上报到国家平台吗？',
      '确认上报',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    reporting.value = true
    ElMessage.info('正在上报数据，请稍候...')

    const res = await excelApi.reportData(batchNo.value)
    reportResult.value = res.data
    reportDialogVisible.value = true

    if (res.data.failCount === 0) {
      ElMessage.success('数据上报成功')
    } else {
      ElMessage.warning(`上报完成，${res.data.failCount}条数据上报失败`)
    }

    fetchData()
  } catch (error) {
    if (error !== 'cancel') {
      // 错误已在拦截器中处理
    }
  } finally {
    reporting.value = false
  }
}

onMounted(() => {
  fetchData()
})
</script>
