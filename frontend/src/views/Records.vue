<template>
  <div class="space-y-6">
    <!-- 页面标题 -->
    <div class="card">
      <h1 class="text-2xl font-bold text-gray-800 mb-2">导入记录</h1>
      <p class="text-gray-500">查看历史导入记录，管理数据上报</p>
    </div>

    <!-- 记录列表 -->
    <div class="card">
      <el-table
        v-loading="loading"
        :data="records"
        stripe
        style="width: 100%"
      >
        <el-table-column prop="batchNo" label="批次号" width="220">
          <template #default="{ row }">
            <span class="font-mono text-sm">{{ row.batchNo }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="fileName" label="文件名" min-width="180" />
        <el-table-column prop="totalCount" label="总记录数" width="100" align="center">
          <template #default="{ row }">
            <span class="font-medium">{{ row.totalCount || 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="successCount" label="成功数" width="100" align="center">
          <template #default="{ row }">
            <span class="text-green-600 font-medium">{{ row.successCount || 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="failCount" label="失败数" width="100" align="center">
          <template #default="{ row }">
            <span class="text-red-600 font-medium">{{ row.failCount || 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="operatorName" label="操作人" width="100" />
        <el-table-column prop="createTime" label="导入时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <div class="flex space-x-2">
              <el-button type="primary" link size="small" @click="viewDetail(row)">
                详情
              </el-button>
              <el-button
                v-if="row.status === 1"
                type="success"
                link
                size="small"
                @click="reportData(row)"
              >
                上报
              </el-button>
            </div>
          </template>
        </el-table-column>
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
        <!-- 结果统计 -->
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

        <!-- 错误列表 -->
        <div v-if="reportResult.errorList && reportResult.errorList.length > 0">
          <div class="flex items-center justify-between mb-3">
            <h4 class="font-medium text-gray-700">上报失败数据</h4>
            <el-button type="primary" link size="small" @click="exportErrors">
              导出失败数据
            </el-button>
          </div>
          <el-table :data="reportResult.errorList" stripe max-height="250" size="small">
            <el-table-column prop="dataCode" label="数据编号" width="120" />
            <el-table-column prop="name" label="姓名" width="100" />
            <el-table-column prop="errorMsg" label="错误原因" />
          </el-table>
        </div>

        <div v-if="reportResult.failCount > 0" class="mt-4 p-3 bg-yellow-50 rounded-lg">
          <p class="text-sm text-yellow-700">
            <svg class="w-4 h-4 inline-block mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
            </svg>
            部分数据上报失败，您可以修正数据后重新上报
          </p>
        </div>
      </div>

      <template #footer>
        <div class="flex justify-end space-x-3">
          <el-button @click="reportDialogVisible = false">关闭</el-button>
          <el-button
            v-if="reportResult?.failCount > 0"
            type="primary"
            @click="retryReport"
            :loading="retrying"
          >
            重新上报失败数据
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { excelApi } from '@/api'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const records = ref([])
const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const reportDialogVisible = ref(false)
const reportResult = ref(null)
const currentBatchNo = ref('')
const retrying = ref(false)

const getStatusType = (status) => {
  const types = { 0: 'info', 1: 'success', 2: 'warning' }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = { 0: '处理中', 1: '完成', 2: '部分失败' }
  return texts[status] || '未知'
}

const formatTime = (time) => {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN')
}

const fetchRecords = async () => {
  loading.value = true
  try {
    const res = await excelApi.getRecords({
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize
    })
    records.value = res.data.records || []
    pagination.total = res.data.total || 0
  } catch (error) {
    // 错误已在拦截器中处理
  } finally {
    loading.value = false
  }
}

const handleSizeChange = (size) => {
  pagination.pageSize = size
  fetchRecords()
}

const handleCurrentChange = (page) => {
  pagination.pageNum = page
  fetchRecords()
}

const viewDetail = (row) => {
  router.push(`/data/${row.batchNo}`)
}

const reportData = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要将批次 ${row.batchNo} 的数据上报到国家平台吗？`,
      '确认上报',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    currentBatchNo.value = row.batchNo
    ElMessage.info('正在上报数据，请稍候...')

    const res = await excelApi.reportData(row.batchNo)
    reportResult.value = res.data
    reportDialogVisible.value = true

    if (res.data.failCount === 0) {
      ElMessage.success('数据上报成功')
    } else {
      ElMessage.warning(`上报完成，${res.data.failCount}条数据上报失败`)
    }

    fetchRecords()
  } catch (error) {
    if (error !== 'cancel') {
      // 错误已在拦截器中处理
    }
  }
}

const retryReport = async () => {
  retrying.value = true
  try {
    const res = await excelApi.retryReport(currentBatchNo.value)
    reportResult.value = res.data

    if (res.data.failCount === 0) {
      ElMessage.success('重新上报成功')
    } else {
      ElMessage.warning(`重新上报完成，仍有${res.data.failCount}条数据失败`)
    }

    fetchRecords()
  } catch (error) {
    // 错误已在拦截器中处理
  } finally {
    retrying.value = false
  }
}

const exportErrors = () => {
  const token = userStore.token
  const url = excelApi.exportErrors(currentBatchNo.value)
  window.open(`${url}?token=${token}`, '_blank')
}

onMounted(() => {
  fetchRecords()
})
</script>
