import request from '@/http/request'

// 分页查询用户
export const getUserPage = (params: any) => {
    return request.get('/api/users/page', { params })
}

// 新增用户
export const createUser = (data: any) => {
    return request.post('/api/users', data)
}

// 更新用户
export const updateUser = (data: any) => {
    return request.put(`/api/users/${data.id}`, data)
}

// 删除用户
export const deleteUser = (id: number) => {
    return request.delete(`/api/users/${id}`)
}