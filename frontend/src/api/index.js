import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import router from '@/router'

const baseURL = import.meta.env.VITE_API_BASE_URL || '/api'

const request = axios.create({
  baseURL,
  timeout: 60000,
  headers: {
    'Content-Type': 'application/json'
  }
})

request.interceptors.request.use(
  (config) => {
    const userStore = useUserStore()
    if (userStore.token) {
      config.headers.Authorization = `Bearer ${userStore.token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

request.interceptors.response.use(
  (response) => {
    // 文件下载（blob）直接放行，由调用方处理
    if (response.config.responseType === 'blob') {
      return response
    }
    const res = response.data
    if (res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res
  },
  (error) => {
    if (error.response) {
      const { status, data } = error.response
      if (status === 401) {
        ElMessage.error('登录已过期，请重新登录')
        const userStore = useUserStore()
        userStore.logout()
        router.push('/login')
      } else if (data instanceof Blob && data.type?.includes('application/json')) {
        // 文件下载接口返回的JSON错误信息（blob形式）
        data.text().then((text) => {
          try {
            ElMessage.error(JSON.parse(text).message || '下载失败')
          } catch {
            ElMessage.error('下载失败')
          }
        })
      } else {
        ElMessage.error(data?.message || '请求失败')
      }
    } else {
      ElMessage.error('网络错误，请检查网络连接')
    }
    return Promise.reject(error)
  }
)

/**
 * 下载文件（自动携带token，从响应头解析文件名并保存）
 * @param {string} path 接口路径，如 /excel/export/import-errors/xxx
 */
export const downloadFile = async (path) => {
  const res = await request.get(path, { responseType: 'blob' })
  const blob = res.data

  // 后端未返回文件（如没有异常数据）时，给出提示
  if (blob.type && blob.type.includes('application/json')) {
    const text = JSON.parse(await blob.text())
    const message = text.message || '下载失败'
    ElMessage.error(message)
    throw new Error(message)
  }

  // 从Content-Disposition解析文件名（格式：attachment;filename*=utf-8''xxx.xlsx）
  const disposition = res.headers['content-disposition'] || ''
  let filename = 'download.xlsx'
  const utf8Match = disposition.match(/filename\*=utf-8''([^;]+)/i)
  if (utf8Match) {
    filename = decodeURIComponent(utf8Match[1])
  } else {
    const asciiMatch = disposition.match(/filename="?([^";]+)"?/i)
    if (asciiMatch) {
      filename = asciiMatch[1]
    }
  }

  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = filename
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(url)
}

export const authApi = {
  login: (data) => request.post('/auth/login', data)
}

export const excelApi = {
  import: (file, onProgress) => {
    const formData = new FormData()
    formData.append('file', file)
    return request.post('/excel/import', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      },
      timeout: 300000,
      onUploadProgress: onProgress
    })
  },

  getRecords: (params) => request.get('/excel/records', { params }),

  getDataByBatch: (batchNo, params) => request.get(`/excel/data/${batchNo}`, { params }),

  reportData: (batchNo) => request.post(`/excel/report/${batchNo}`),

  getFailedData: (batchNo) => request.get(`/excel/report/failed/${batchNo}`),

  retryReport: (batchNo) => request.post(`/excel/report/retry/${batchNo}`),

  downloadTemplate: () => {
    return `${baseURL}/excel/template`
  },

  exportErrors: (batchNo) => {
    return `${baseURL}/excel/export/errors/${batchNo}`
  },

  exportImportErrors: (batchNo) => {
    return `/excel/export/import-errors/${batchNo}`
  }
}

export default request
