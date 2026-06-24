import axios from "axios";
import { ElMessage } from "element-plus";

const instance = axios.create({
    baseURL: 'https://pixelbox.bond/',
    //baseURL: 'http://localhost:8080',
    timeout: 10000,
});

// 请求拦截器
instance.interceptors.request.use((config: any) => {
    if (config.method?.toLowerCase() !== 'get') {
        const isFormData = config.data instanceof FormData;
        const hasContentType = config.headers && (config.headers['Content-Type'] || config.headers['content-type']);
        if (!isFormData && !hasContentType) {
            config.headers['Content-Type'] = 'application/json;charset=UTF-8';
        }
    }
    return config;
}, (error: any) => {
    return Promise.reject(error);
});

// 响应拦截器
instance.interceptors.response.use(
    (response: any) => {
        const res = response.data;

        // 标准 Result 包装对象
        if (res && typeof res.code !== 'undefined') {
            if (res.code !== 200) {
                ElMessage.warning(res.message || '请求失败');
                return Promise.reject(res);
            }
            return res;  // 返回 {code, message, data}
        }

        // 非标准包装：统一包装成标准格式，保证调用方一致性
        return {
            code: 200,
            data: res,
            message: 'success'
        };
    },
    (error: any) => {
        if (error.response) {
            switch (error.response.status) {
                case 500:
                    ElMessage.error("服务器开小差了，请稍后再试");
                    break;
                default:
                    ElMessage.error(error.response.data?.message || "网络错误");
            }
        } else {
            ElMessage.error("连接服务器失败");
        }
        return Promise.reject(error);
    }
);

export default <T = any>(options: any): Promise<T> => instance(options);
