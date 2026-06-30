<script setup lang="ts">
import {ref, reactive, onMounted} from 'vue'
import {
  getPermissionPage, createPermission, updatePermission, deletePermission, getPermissionTree,
} from "@/http/permission.ts";

const currentPage = ref(1); const pageSize = ref(20); const total = ref(0)
const tableLoading = ref(false)
const queryForm = reactive({ name: '', code: '', menuType: '' })
const permissionList = ref<any[]>([])
const parentOptions = ref<any[]>([])
const parentNameMap = new Map()
const dialogVisible = ref(false); const editingId = ref<number | null>(null)
const permissionForm = ref({ name: '', menuType: '', parentId: 0, code: '', path: '', sort: 0 })

const getPermissionTreeData = async () => {
  const res = await getPermissionTree()
  parentOptions.value = res.data || []
  parentOptions.value.forEach((item:any) => parentNameMap.set(item.id, item.label))
}

const openPermissionDialog = (row?:any) => {
  editingId.value = row?.id || null
  dialogVisible.value = true
  if (row) permissionForm.value = { ...row }
  else permissionForm.value = { name: '', menuType: '', parentId: 0, code: '', path: '', sort: 0 }
}

const getPermissions = async () => {
  tableLoading.value = true
  const result = await getPermissionPage({
    currentPage: currentPage.value, pageSize: pageSize.value,
    params: [{name: 'name', value: queryForm.name},{name: 'code', value: queryForm.code},{name: 'menuType', value: queryForm.menuType}],
  })
  permissionList.value = result.data?.records || []
  total.value = Number(result.data?.total || 0)
  tableLoading.value = false
}

const submitPermission = async () => {
  if (!permissionForm.value.name || !permissionForm.value.menuType || !permissionForm.value.code) {
    alert('请填写名称、类型和标识'); return
  }
  const payload = { name: permissionForm.value.name, menuType: permissionForm.value.menuType, code: permissionForm.value.code, path: permissionForm.value.path, parentId: permissionForm.value.parentId, sort: permissionForm.value.sort }
  if (editingId.value) await updatePermission(editingId.value, payload)
  else await createPermission(payload)
  alert(editingId.value ? '权限已更新' : '权限已新增')
  dialogVisible.value = false
  await Promise.all([getPermissions(), getPermissionTreeData()])
}

const handleDelete = async (row:any) => {
  if (!confirm(`确认删除权限“${row.name}”吗？`)) return
  await deletePermission(row.id)
  alert('权限已删除')
  await Promise.all([getPermissions(), getPermissionTreeData()])
}

onMounted(() => { getPermissions(); getPermissionTreeData() })
</script>

<template>
  <div class="layout-container">
    <div class="header-btns"><h3>权限管理</h3><button class="btn-primary" @click="openPermissionDialog()">+ 新增权限</button></div>
    <div class="search-panel">
      <input type="text" v-model="queryForm.name" placeholder="权限名称" />
      <input type="text" v-model="queryForm.code" placeholder="权限标识" />
      <select v-model="queryForm.menuType"><option value="">全部</option><option value="DIRECTORY">目录</option><option value="MENU">菜单</option><option value="BUTTON">按钮</option></select>
      <button class="btn-primary" @click="getPermissions">搜索</button>
      <button @click="queryForm = { name: '', code: '', menuType: '' }">重置</button>
    </div>
    <table border="1" class="raw-table">
      <thead><tr><th>ID</th><th>名称</th><th>类型</th><th>标识</th><th>路径</th><th>父级</th><th>排序</th><th>操作</th></tr></thead>
      <tbody>
      <tr v-if="tableLoading"><td colspan="8" style="text-align:center;">加载中...</td></tr>
      <tr v-else-if="permissionList.length === 0"><td colspan="8" style="text-align:center;">暂无数据</td></tr>
      <tr v-for="row in permissionList" :key="row.id">
        <td>{{ row.id }}</td>
        <td>{{ row.name }}</td>
        <td>{{ row.menuType }}</td>
        <td>{{ row.code }}</td>
        <td>{{ row.path || '-' }}</td>
        <td>{{ parentNameMap.get(row.parentId) || '顶级' }}</td>
        <td>{{ row.sort }}</td>
        <td><button class="btn-xs btn-primary" @click="openPermissionDialog(row)">编辑</button> <button class="btn-xs btn-danger" @click="handleDelete(row)">删除</button></td>
      </tr>
      </tbody>
    </table>
    <div class="pagination">
      <span>共 {{ total }} 条</span>
      <select v-model="pageSize"><option :value="20">20</option><option :value="50">50</option></select>
      <button @click="currentPage > 1 && (currentPage--, getPermissions())">上一页</button>
      <span>第 <input type="number" v-model="currentPage" style="width:40px;text-align:center;" @change="getPermissions"/> 页</span>
      <button @click="currentPage < Math.ceil(total/pageSize) && (currentPage++, getPermissions())">下一页</button>
    </div>

    <!-- 原生弹窗 -->
    <div class="modal-overlay" v-if="dialogVisible">
      <div class="modal-box" style="width:520px;">
        <div class="modal-header"><h3>{{ editingId ? '编辑权限' : '新增权限' }}</h3><span class="close" @click="dialogVisible=false">×</span></div>
        <div class="modal-body">
          <div class="form-group"><label>名称</label><input type="text" v-model="permissionForm.name" /></div>
          <div class="form-group"><label>类型</label><select v-model="permissionForm.menuType"><option value="DIRECTORY">目录</option><option value="MENU">菜单</option><option value="BUTTON">按钮</option></select></div>
          <div class="form-group"><label>父级</label><select v-model="permissionForm.parentId"><option :value="0">顶级权限</option><option v-for="opt in parentOptions" :key="opt.id" :label="opt.label" :value="opt.id">{{ opt.label }}</option></select></div>
          <div class="form-group"><label>标识</label><input type="text" v-model="permissionForm.code" /></div>
          <div class="form-group"><label>路径</label><input type="text" v-model="permissionForm.path" /></div>
          <div class="form-group"><label>排序</label><input type="number" v-model="permissionForm.sort" /></div>
        </div>
        <div class="modal-footer"><button @click="dialogVisible=false">取消</button><button class="btn-primary" @click="submitPermission">保存</button></div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.layout-container { padding: 15px; background: #f5f7fa; font-family: sans-serif; }
.header-btns { display: flex; justify-content: space-between; align-items: center; background: #fff; border:1px solid #ccc; padding: 10px; margin-bottom: 12px; }
.search-panel { padding: 10px; background: #fff; border: 1px solid #ccc; margin-bottom: 12px; display: flex; gap: 8px; flex-wrap: wrap; }
.search-panel input, .search-panel select { border: 1px solid #ccc; padding: 4px 8px; }
.raw-table { width: 100%; border-collapse: collapse; background: #fff; font-size: 13px; }
.raw-table th, .raw-table td { border: 1px solid #ccc; padding: 6px 10px; text-align: left; }
.raw-table th { background: #eee; font-weight: bold; }
.btn-primary { background: #0056b3; color: #fff; border: 1px solid #0056b3; padding: 4px 10px; cursor: pointer; }
.btn-danger { background: #d32f2f; color: #fff; border: 1px solid #d32f2f; }
.btn-xs { padding: 2px 6px; font-size: 12px; border: 1px solid transparent; cursor: pointer; }
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