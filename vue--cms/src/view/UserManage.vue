<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUserPage, createUser, updateUser, deleteUser } from '@/http/user.ts'

// 分页
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

// 加载状态
const tableLoading = ref(false)

// 查询表单
const queryForm = reactive({
  username: '',
  status: '',
  phone: '',
})

// 列表数据
const userList = ref<any[]>([])

// 弹窗 & 编辑
const dialogVisible = ref(false)
const editingId = ref<number | null>(null)

// 表单
const formRef = ref()
const userForm = ref({
  username: '',
  password: '',
  realName: '',
  phone: '',
  email: '',
  status: 1,
})

// 校验规则
const rules = reactive({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 4, max: 20, message: '用户名长度4-20位', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度6-20位', trigger: 'blur' },
  ],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
})

// 状态映射
const statusTagMap: Record<number, string> = {
  0: 'danger',
  1: 'success',
}
const statusTextMap: Record<number, string> = {
  0: '禁用',
  1: '启用',
}

// 获取用户列表
const getUsers = async () => {
  tableLoading.value = true
  try {
    const result = await getUserPage({
      currentPage: currentPage.value,
      pageSize: pageSize.value,
      username: queryForm.username || undefined,
      status: queryForm.status !== '' ? queryForm.status : undefined,
      phone: queryForm.phone || undefined,
    })
    userList.value = result.data?.records || []
    total.value = Number(result.data?.total || 0)
  } finally {
    tableLoading.value = false
  }
}

// 打开弹窗（新增/编辑）
const openUserDialog = (row?: any) => {
  editingId.value = row?.id || null
  dialogVisible.value = true

  if (row) {
    // 编辑时回显数据（不包含密码）
    userForm.value = {
      username: row.username,
      password: '',
      realName: row.realName,
      phone: row.phone || '',
      email: row.email || '',
      status: row.status,
    }
  } else {
    // 新增时重置表单
    userForm.value = {
      username: '',
      password: '',
      realName: '',
      phone: '',
      email: '',
      status: 1,
    }
  }
}

// 提交表单
const submitUser = async () => {
  await formRef.value.validate()

  const payload: any = {
    username: userForm.value.username,
    realName: userForm.value.realName,
    phone: userForm.value.phone,
    email: userForm.value.email,
    status: userForm.value.status,
  }

  // 新增时才传密码
  if (!editingId.value) {
    payload.password = userForm.value.password
  }

  if (editingId.value) {
    payload.id = editingId.value
    await updateUser(payload)
  } else {
    await createUser(payload)
  }

  ElMessage.success(editingId.value ? '用户已更新' : '用户已新增')
  dialogVisible.value = false
  await getUsers()
}

// 删除用户
const handleDelete = async (row: any) => {
  await ElMessageBox.confirm(`确认删除用户“${row.username}”吗？`, '删除确认', {
    confirmButtonText: '删除',
    cancelButtonText: '取消',
    type: 'warning',
  })
  await deleteUser(row.id)
  ElMessage.success('用户已删除')
  await getUsers()
}

// 页面加载
onMounted(() => {
  getUsers()
})
</script>

<template>
  <!-- 搜索栏 + 新增按钮 -->
  <div style="padding: 16px; background: #fff; margin-bottom: 16px; border-radius: 4px;">
    <div style="display: flex; justify-content: space-between; margin-bottom: 12px;">
      <h3 style="margin: 0;">用户管理</h3>
      <el-button type="primary" @click="openUserDialog()">+ 新增用户</el-button>
    </div>

    <el-form :model="queryForm" inline>
      <el-form-item label="用户名">
        <el-input v-model="queryForm.username" placeholder="用户名" clearable />
      </el-form-item>
      <el-form-item label="手机号">
        <el-input v-model="queryForm.phone" placeholder="手机号" clearable />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="queryForm.status" placeholder="请选择" clearable style="width: 100px">
          <el-option label="启用" :value="1" />
          <el-option label="禁用" :value="0" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="getUsers">搜索</el-button>
        <el-button @click="queryForm = { username: '', status: '', phone: '' }">重置</el-button>
      </el-form-item>
    </el-form>
  </div>

  <!-- 表格 -->
  <el-table v-loading="tableLoading" :data="userList" border>
    <el-table-column prop="id" label="用户ID" width="90" />
    <el-table-column prop="username" label="用户名" min-width="120" show-overflow-tooltip />
    <el-table-column prop="realName" label="真实姓名" min-width="100" />
    <el-table-column prop="phone" label="手机号" min-width="130" />
    <el-table-column prop="email" label="邮箱" min-width="160" show-overflow-tooltip />
    <el-table-column label="状态" width="80">
      <template #default="{ row }">
        <el-tag :type="statusTagMap[row.status] || 'info'" size="small">
          {{ statusTextMap[row.status] || row.status }}
        </el-tag>
      </template>
    </el-table-column>
    <el-table-column prop="createTime" label="创建时间" min-width="160" />
    <el-table-column label="操作" width="160" fixed="right">
      <template #default="{ row }">
        <el-button type="primary" size="small" @click="openUserDialog(row)">编辑</el-button>
        <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
      </template>
    </el-table-column>
  </el-table>

  <!-- 分页器 -->
  <el-pagination
      v-model:current-page="currentPage"
      v-model:page-size="pageSize"
      :total="total"
      background
      layout="total, sizes, prev, pager, next, jumper"
      @size-change="getUsers"
      @current-change="getUsers"
      style="margin-top: 16px; text-align: right;"
  />

  <!-- 弹窗（新增/编辑） -->
  <el-dialog
      v-model="dialogVisible"
      :title="editingId ? '编辑用户' : '新增用户'"
      width="520px"
      destroy-on-close
      align-center
  >
    <el-form ref="formRef" :model="userForm" :rules="rules" label-width="90px">
      <el-form-item label="用户名" prop="username">
        <el-input v-model="userForm.username" maxlength="20" :disabled="!!editingId" placeholder="请输入用户名" />
      </el-form-item>
      <el-form-item label="密码" prop="password" v-if="!editingId">
        <el-input v-model="userForm.password" type="password" maxlength="20" placeholder="请输入密码" show-password />
      </el-form-item>
      <el-form-item label="真实姓名" prop="realName">
        <el-input v-model="userForm.realName" maxlength="50" placeholder="请输入真实姓名" />
      </el-form-item>
      <el-form-item label="手机号">
        <el-input v-model="userForm.phone" maxlength="20" placeholder="请输入手机号" />
      </el-form-item>
      <el-form-item label="邮箱">
        <el-input v-model="userForm.email" maxlength="100" placeholder="请输入邮箱" />
      </el-form-item>
      <el-form-item label="状态">
        <el-radio-group v-model="userForm.status">
          <el-radio :value="1">启用</el-radio>
          <el-radio :value="0">禁用</el-radio>
        </el-radio-group>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" @click="submitUser">保存</el-button>
    </template>
  </el-dialog>
</template>

<style scoped>
</style>