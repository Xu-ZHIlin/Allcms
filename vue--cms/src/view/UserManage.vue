<script setup lang="ts">
import {onMounted, reactive, ref} from "vue";
import {createUser, deleteUser, getUserPage, updateUser} from "@/http/user.ts";
import {getRoleInfoPage} from "@/http/role.ts";

interface RoleOption { id: number; name: string }
interface UserItem { id: string; username: string; createTime?: string; updateTime?: string; roleIds: number[]; roleNames: string[] }
interface UserForm { username: string; password: string; roleIds: number[] }

const emptyForm = (): UserForm => ({ username: '', password: '', roleIds: [], })
const userForm = ref<UserForm>(emptyForm())
const editingId = ref<string | null>(null)
const dialogVisible = ref(false)
const submitting = ref(false)
const tableLoading = ref(false)
const userList = ref<UserItem[]>([])
const roleOptions = ref<RoleOption[]>([])
const deletingIds = ref<string[]>([])
const currentPage = ref(1); const pageSize = ref(10); const total = ref(0)
const queryForm = reactive({ username: '', })

const formatDateTime = (value?: string) => { if (!value) return '-'; return value.replace('T', ' ').slice(0, 16) }

const getUsers = async () => {
  tableLoading.value = true
  const result: any = await getUserPage({
    currentPage: currentPage.value, pageSize: pageSize.value,
    params: [{name: 'username', value: queryForm.username}],
  })
  userList.value = (result.data?.records || []).map((item: UserItem) => ({
    ...item, roleIds: Array.isArray(item.roleIds) ? item.roleIds : [], roleNames: Array.isArray(item.roleNames) ? item.roleNames : [],
  }))
  total.value = Number(result.data?.total || 0)
  tableLoading.value = false
}

const getRoles = async () => {
  const result: any = await getRoleInfoPage({ currentPage: 1, pageSize: 100, params: [{name: 'roleName', value: ''}] })
  roleOptions.value = result.data?.records || []
}

const handleSearch = () => { currentPage.value = 1; getUsers() }
const resetSearch = () => { queryForm.username = ''; handleSearch() }

const openUserDialog = (row?: UserItem) => {
  editingId.value = row?.id || null
  userForm.value = row ? { username: row.username, password: '', roleIds: [...row.roleIds] } : emptyForm()
  dialogVisible.value = true
}

const submitUser = async () => {
  if (!userForm.value.username) { alert('请输入用户名'); return }
  if (!editingId.value && !userForm.value.password) { alert('请输入密码'); return }
  submitting.value = true
  const payload = { username: userForm.value.username, password: userForm.value.password, roleIds: userForm.value.roleIds }
  if (editingId.value) await updateUser(editingId.value, payload)
  else await createUser(payload)
  alert(editingId.value ? '用户已更新' : '用户已新增')
  dialogVisible.value = false
  await getUsers()
  submitting.value = false
}

const handleDeleteUser = async (row: UserItem) => {
  if (!confirm(`确认删除用户“${row.username}”吗？`)) return
  deletingIds.value.push(row.id)
  await deleteUser(row.id)
  alert('用户已删除')
  if (userList.value.length === 1 && currentPage.value > 1) currentPage.value -= 1
  await getUsers()
  deletingIds.value = deletingIds.value.filter(id => id !== row.id)
}

onMounted(() => { getRoles(); getUsers() })
</script>

<template>
  <div class="layout-container">
    <div class="toolbar"><div><h2>用户管理</h2><p>维护登录账号，并给用户分配角色。</p></div><button class="btn-primary" @click="openUserDialog()">新增用户</button></div>
    <div class="search-panel">
      <input type="text" v-model="queryForm.username" placeholder="请输入用户名" />
      <button class="btn-primary" @click="handleSearch">查询</button>
      <button @click="resetSearch">重置</button>
    </div>
    <table border="1" class="raw-table">
      <thead><tr><th>ID</th><th>用户名</th><th>角色</th><th>创建时间</th><th>更新时间</th><th>操作</th></tr></thead>
      <tbody>
      <tr v-if="tableLoading"><td colspan="6" style="text-align:center;">加载中...</td></tr>
      <tr v-else-if="userList.length === 0"><td colspan="6" style="text-align:center;">暂无数据</td></tr>
      <tr v-for="row in userList" :key="row.id">
        <td>{{ row.id }}</td>
        <td>{{ row.username }}</td>
        <td><span v-if="row.roleNames.length"><span class="role-tag" v-for="r in row.roleNames" :key="r">{{ r }}</span></span><span v-else>-</span></td>
        <td>{{ formatDateTime(row.createTime) }}</td>
        <td>{{ formatDateTime(row.updateTime) }}</td>
        <td><button class="btn-xs btn-primary" @click="openUserDialog(row)">编辑</button> <button class="btn-xs btn-danger" @click="handleDeleteUser(row)">删除</button></td>
      </tr>
      </tbody>
    </table>
    <div class="pagination">
      <span>共 {{ total }} 条</span>
      <select v-model="pageSize"><option :value="10">10</option><option :value="20">20</option><option :value="50">50</option></select>
      <button @click="currentPage > 1 && (currentPage--, getUsers())">上一页</button>
      <span>第 <input type="number" v-model="currentPage" style="width:40px;text-align:center;" @change="getUsers"/> 页</span>
      <button @click="currentPage < Math.ceil(total/pageSize) && (currentPage++, getUsers())">下一页</button>
    </div>

    <!-- 原生弹窗 -->
    <div class="modal-overlay" v-if="dialogVisible">
      <div class="modal-box" style="width:480px;">
        <div class="modal-header"><h3>{{ editingId ? '编辑用户' : '新增用户' }}</h3><span class="close" @click="dialogVisible=false">×</span></div>
        <div class="modal-body">
          <div class="form-group"><label>用户名</label><input type="text" v-model="userForm.username" /></div>
          <div class="form-group"><label>{{ editingId ? '新密码' : '密码' }}</label><input type="password" v-model="userForm.password" placeholder="留空不修改" /></div>
          <div class="form-group"><label>角色（按住 Ctrl 多选）</label><select multiple v-model="userForm.roleIds" style="height:100px;width:100%;"><option v-for="r in roleOptions" :key="r.id" :value="r.id">{{ r.name }}</option></select></div>
        </div>
        <div class="modal-footer"><button @click="dialogVisible=false">取消</button><button class="btn-primary" @click="submitUser">{{ editingId ? '保存修改' : '新增用户' }}</button></div>
      </div>
    </div>
  </div>


</template>

<style scoped>
.layout-container { padding: 15px; background: #f5f7fa; font-family: sans-serif; }
.toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.search-panel { padding: 10px; background: #fff; border: 1px solid #ccc; margin-bottom: 12px; display: flex; gap: 8px; }
.search-panel input { border: 1px solid #ccc; padding: 4px 8px; }
.raw-table { width: 100%; border-collapse: collapse; background: #fff; font-size: 13px; }
.raw-table th, .raw-table td { border: 1px solid #ccc; padding: 6px 10px; text-align: left; }
.raw-table th { background: #eee; font-weight: bold; }
.btn-primary { background: #0056b3; color: #fff; border: 1px solid #0056b3; padding: 4px 10px; cursor: pointer; }
.btn-danger { background: #d32f2f; color: #fff; border: 1px solid #d32f2f; }
.btn-xs { padding: 2px 6px; font-size: 12px; border: 1px solid transparent; cursor: pointer; }
.role-tag { background: #eee; border: 1px solid #ccc; padding: 0 4px; font-size:12px; margin-right:4px; }
.pagination { display: flex; align-items: center; gap: 10px; margin-top: 12px; font-size: 13px; }
.modal-overlay { position: fixed; top:0; left:0; width:100%; height:100%; background: rgba(0,0,0,0.4); z-index:2000; display:flex; justify-content:center; align-items:center; }
.modal-box { background: #fff; border: 2px solid #000; display:flex; flex-direction:column; }
.modal-header { border-bottom: 1px solid #ccc; padding: 12px 20px; display:flex; justify-content:space-between; align-items:center; }
.modal-body { padding: 16px 20px; overflow-y: auto; flex:1; }
.modal-footer { border-top: 1px solid #ccc; padding: 12px 20px; text-align:right; }
.form-group { margin-bottom: 12px; }
.form-group label { display: block; font-size: 13px; font-weight:bold; margin-bottom:4px; }
.form-group input, .form-group select { width: 100%; border: 1px solid #ccc; padding: 4px 6px; box-sizing:border-box; }
.close { cursor: pointer; font-size: 24px; line-height:1; }
</style>