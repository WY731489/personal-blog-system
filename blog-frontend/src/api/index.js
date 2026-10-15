import axios from 'axios'
import { ElMessage } from 'element-plus'

// 创建axios实例
const service = axios.create({
    baseURL: 'http://localhost:8080/api',
    timeout: 10000
})

// 请求拦截器
service.interceptors.request.use(
    config => {
        const token = localStorage.getItem('token')
        if (token) {
            config.headers['Authorization'] = 'Bearer ' + token
        }
        return config
    },
    error => {
        console.log(error)
        return Promise.reject(error)
    }
)

// 响应拦截器
service.interceptors.response.use(
    response => {
        const res = response.data
        if (response.status === 200) {
            return res
        } else {
            ElMessage.error(res.message || 'Error')
            return Promise.reject(new Error(res.message || 'Error'))
        }
    },
    error => {
        console.log('err' + error)

        if (error.response) {
            switch (error.response.status) {
                case 401:
                    ElMessage.error('未授权，请重新登录')
                    localStorage.removeItem('token')
                    localStorage.removeItem('user')
                    break
                case 403:
                    ElMessage.error('拒绝访问')
                    break
                case 404:
                    ElMessage.error('请求地址不存在')
                    break
                case 500:
                    ElMessage.error('服务器内部错误')
                    break
                default:
                    ElMessage.error(error.response.data?.message || '服务器错误')
            }
        } else {
            ElMessage.error('网络错误，请检查网络连接')
        }

        return Promise.reject(error)
    }
)

export default service