import axios from "@/http/axios.ts";

export interface NewsPayload {
    title: string
    category: string
    supplier?: string
    reviewer?: string
    content: string
}

export const createNews = (data: NewsPayload) => {
    return axios({
        url: '/api/news',
        method: 'POST',
        data
    })
}