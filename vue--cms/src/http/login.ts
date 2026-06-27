import axios from '@/http/axios.ts'

export const login = (data: any) => {
   return  axios({
        url:'/auth/login',
        method: 'POST',
        data:data,
    })
}