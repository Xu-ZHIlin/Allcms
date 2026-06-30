<script lang="ts" setup>
import { nextTick, onMounted, reactive, ref } from 'vue'
import {
  authorizeRole, createRole, deleteRole, getPermissionTree, getRoleInfoPage, getRolePermissionIds, updateRole
} from '@/http/role.ts'
import { useUserStore } from '@/stores/user'

interface RoleItem { id: number; name: string }
interface RoleForm { name: string }
const userStore = useUserStore()

const roleFormRef = ref()
const roleForm = ref<RoleForm>({ name: '' })
const editingId = ref<number | null>(null)
const roleDialogVisible = ref(false)
const authorizeDialogVisible = ref(false)
const submitting = ref(false); const authorizing = ref(false)
const tableLoading = ref(false)
const roleList = ref<RoleItem[]>([])
const currentPage = ref(1); const pageSize = ref(10); const total = ref(0)
const currentRole = ref<RoleItem | null>(null)
const permissionTree = ref<any[]>([])
const queryForm = reactive({ roleName: '' })

// 树结构共享状态
const checkedPermissionIds = ref<number[]>([])
const expandedNodes = ref<Set<number>>(new Set())

const hasPermission = (permCode: string) => {
  if (!userStore.userInfo || !userStore.userInfo.permissionList) return false
  return userStore.userInfo.permissionList.some((p: any) => p.code === permCode)
}

const getRoleData = async () => {
  tableLoading.value = true
  const result = await getRoleInfoPage({ currentPage: currentPage.value, pageSize: pageSize.value, roleName: queryForm.roleName })
  roleList.value = result.data?.records || []
  total.value = Number(result.data?.total || 0)
  tableLoading.value = false
}

const handleSearch = () => { currentPage.value = 1; getRoleData() }
const resetSearch = () => { queryForm.roleName = ''; handleSearch() }

const openRoleDialog = (row?: RoleItem) => {
  editingId.value = row?.id || null
  roleForm.value = row ? { name: row.name } : { name: '' }
  roleDialogVisible.value = true
}

const submitRole = async () => {
  if (!roleForm.value.name) { alert('请输入角色名称'); return }
  submitting.value = true
  if (editingId.value) await updateRole(editingId.value, roleForm.value)
  else await createRole(roleForm.value)
  alert(editingId.value ? '角色已更新' : '角色已新增')
  roleDialogVisible.value = false
  await getRoleData()
  submitting.value = false
}

const handleDelete = async (row: RoleItem) => {
  if (!confirm(`确认删除角色“${row.name}”吗？`)) return
  await deleteRole(row.id)
  alert('角色已删除')
  await getRoleData()
}

// 1. 切换折叠状态
const toggleExpand = (id: number) => {
  if (expandedNodes.value.has(id)) expandedNodes.value.delete(id)
  else expandedNodes.value.add(id)
}

// 2. 切换复选框状态
const toggleCheckNode = (id: number) => {
  const idx = checkedPermissionIds.value.indexOf(id)
  if (idx > -1) checkedPermissionIds.value.splice(idx, 1)
  else checkedPermissionIds.value.push(id)
}

const handleAuthorize = async (role: RoleItem) => {
  currentRole.value = role
  authorizeDialogVisible.value = true
  const treeResult = await getPermissionTree()
  permissionTree.value = treeResult.data || []
  const permRes = await getRolePermissionIds(role.id)
  checkedPermissionIds.value = permRes.data || []
}

const handleSaveAuthorize = async () => {
  if (checkedPermissionIds.value.length === 0) { alert('至少需要分配一项权限'); return }
  authorizing.value = true
  await authorizeRole({ roleId: currentRole.value!.id, permissionIds: checkedPermissionIds.value })
  alert('授权保存成功')
  authorizeDialogVisible.value = false
  authorizing.value = false
}

onMounted(() => { getRoleData() })
</script>

<!-- 3. 用于递归渲染的原生 HTML 树组件（使用了独立的 script 标签） -->
<script lang="ts">
import { defineComponent, PropType } from 'vue'
export default defineComponent({
  name: 'TreeItem',
  props: {
    node: { type: Object as PropType<any>, required: true },
    checkedIds: { type: Array as PropType<number[]>, required: true },
    expandedIds: { type: Object as PropType<Set<number>>, required: true },
    toggleExpand: { type: Function as PropType<(id: number) => void>, required: true },
    toggleCheck: { type: Function as PropType<(id: number) => void>, required: true }
  },
  template: `
    <li>
      <div class="tree-node">
        <span class="tree-toggle" @click="toggleExpand(node.id)">
          {{ node.children?.length ? (expandedIds.has(node.id) ? '[-]' : '[+]') : '' }}
        </span>
        <input type="checkbox" :checked="checkedIds.includes(node.id)" @change="toggleCheck(node.id)" />
        <span class="tree-label">{{ node.name }}</span>
        <span class="tree-type">[{{ node.menuType }}]</span>
      </div>
      <ul class="tree-children" v-if="node.children?.length && expandedIds.has(node.id)">
        <TreeItem
            v-for="child in node.children"
            :key="child.id"
            :node="child"
            :checkedIds="checkedIds"
            :expandedIds="expandedIds"
            :toggleExpand="toggleExpand"
            :toggleCheck="toggleCheck"
        />
      </ul>
    </li>
  `
})
</script>

<template>
  <div class="layout-container">
    <div class="toolbar"><div><h2>角色管理</h2><p>维护角色并分配菜单和按钮权限。</p></div><button class="btn-primary" @click="openRoleDialog()">新增角色</button></div>
    <div class="search-panel">
      <input type="text" v-model="queryForm.roleName" placeholder="请输入角色名称" />
      <button class="btn-primary" @click="handleSearch">查询</button>
      <button @click="resetSearch">重置</button>
    </div>
    <table border="1" class="raw-table">
      <thead><tr><th>ID</th><th>角色名称</th><th>操作</th></tr></thead>
      <tbody>
      <tr v-if="tableLoading"><td colspan="3" style="text-align:center;">加载中...</td></tr>
      <tr v-else-if="roleList.length === 0"><td colspan="3" style="text-align:center;">暂无数据</td></tr>
      <tr v-for="row in roleList" :key="row.id">
        <td>{{ row.id }}</td>
        <td>{{ row.name }}</td>
        <td>
          <button class="btn-xs btn-primary" :disabled="!hasPermission('role:edit')" @click="openRoleDialog(row)">编辑</button>
          <button class="btn-xs btn-success" :disabled="!hasPermission('role:assign-permission')" @click="handleAuthorize(row)">授权</button>
          <button class="btn-xs btn-danger" :disabled="!hasPermission('role:delete')" @click="handleDelete(row)">删除</button>
        </td>
      </tr>
      </tbody>
    </table>
    <div class="pagination">
      <span>共 {{ total }} 条</span>
      <select v-model="pageSize"><option :value="10">10</option><option :value="20">20</option><option :value="50">50</option></select>
      <button @click="currentPage > 1 && (currentPage--, getRoleData())">上一页</button>
      <span>第 <input type="number" v-model="currentPage" style="width:40px;text-align:center;" @change="getRoleData"/> 页</span>
      <button @click="currentPage < Math.ceil(total/pageSize) && (currentPage++, getRoleData())">下一页</button>
    </div>

    <!-- 原生弹窗(新增/编辑) -->
    <div class="modal-overlay" v-if="roleDialogVisible">
      <div class="modal-box" style="width:440px;">
        <div class="modal-header"><h3>{{ editingId ? '编辑角色' : '新增角色' }}</h3><span class="close" @click="roleDialogVisible=false">×</span></div>
        <div class="modal-body"><div class="form-group"><label>角色名称</label><input type="text" v-model="roleForm.name" /></div></div>
        <div class="modal-footer"><button @click="roleDialogVisible=false">取消</button><button class="btn-primary" @click="submitRole">{{ editingId ? '保存修改' : '新增角色' }}</button></div>
      </div>
    </div>

    <!-- 原生弹窗(授权) - 使用递归 TreeItem 组件 -->
    <div class="modal-overlay" v-if="authorizeDialogVisible">
      <div class="modal-box" style="width:600px;">
        <div class="modal-header"><h3>角色授权 - {{ currentRole?.name }}</h3><span class="close" @click="authorizeDialogVisible=false">×</span></div>
        <div class="modal-body" style="max-height:400px; overflow-y:auto;">
          <ul class="tree-container">
            <TreeItem
                v-for="node in permissionTree"
                :key="node.id"
                :node="node"
                :checkedIds="checkedPermissionIds"
                :expandedIds="expandedNodes"
                :toggleExpand="toggleExpand"
                :toggleCheck="toggleCheckNode"
            />
          </ul>
        </div>
        <div class="modal-footer"><button @click="authorizeDialogVisible=false">取消</button><button class="btn-primary" @click="handleSaveAuthorize">确定</button></div>
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
.btn-success { background: #2e7d32; color: #fff; border: 1px solid #2e7d32; }
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
.form-group input { width: 100%; border: 1px solid #ccc; padding: 4px 6px; box-sizing:border-box; }
.close { cursor: pointer; font-size: 24px; line-height:1; }

/* 原生树形结构 */
.tree-container { list-style: none; padding-left: 0; margin:0; }
.tree-container ul { list-style: none; padding-left: 20px; margin:0; }
.tree-node { padding: 4px 0; display: flex; align-items: center; gap:6px; }
.tree-toggle { cursor:pointer; font-family:monospace; display:inline-block; width:18px; text-align:center; border:1px solid #ccc; background:#eee; margin-right:4px; }
.tree-label { font-size: 14px; }
.tree-type { font-size: 11px; color:#888; background:#f5f5f5; padding:0 4px; border:1px solid #eee; }
</style>