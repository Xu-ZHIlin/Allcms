import axios from './axios'

// 后端返回的新闻结构（与您 HomeView 的 NewsData 对应，类型稍微调整）
export interface NewsData {
    id: string         // 注意：后端返回的是字符串（大整数转字符串）
    title: string
    category: string   // 后端返回中文，如 "学院新闻"
    supplier?: string
    reviewer?: string
    content: string
    status: string
    publishTime?: string
    createTime?: string
    updateTime?: string
}

// 获取首页新闻列表（POST /api/news/public/page）
export const getNews = (currentPage = 1, pageSize = 10) => {
    return axios({
        url: '/api/news/public/page',
        method: 'POST',
        data: {
            currentPage,
            pageSize,
            params: [] // 默认不加筛选条件
        }
    })
}

// 获取单条新闻详情（可选，如果列表里已经有 content，详情页可以不调用这个，但留着备用）
export const getNewsDetail = (id: string) => {
    return axios({
        url: `/api/news/public/${id}`,
        method: 'GET'
    })
}