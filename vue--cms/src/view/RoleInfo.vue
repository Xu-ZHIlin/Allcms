<script lang="ts" setup>
import { nextTick, onMounted, reactive, ref } from 'vue'
import type { FormInstance } from 'element-plus'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  authorizeRole,
  createRole,
  deleteRole,
  getPermissionTree,
  getRoleInfoPage,
  getRolePermissionIds,
  updateRole
} from '@/http/role.ts'

interface RoleItem {
  id: number
  name: string
}

interface RoleForm {
  name: string
}

interface PermissionItem {
  id: number
  name: string
  menuType?: 'DIRECTORY' | 'MENU' | 'BUTTON'
  children?: PermissionItem[]
}

const roleFormRef = ref<FormInstance>()
const roleForm = ref<RoleForm>({ name: '' })
const editingId = ref<number | null>(null)
const roleDialogVisible = ref(false)
const authorizeDialogVisible = ref(false)
const submitting = ref(false)
const authorizing = ref(false)
const tableLoading = ref(false)
const roleList = ref<RoleItem[]>([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const currentRole = ref<RoleItem | null>(null)
const permissionTree = ref<PermissionItem[]>([])
const permissionTreeRef = ref<any>(null)

const queryForm = reactive({ roleName: '' })

const roleRules = {
  name: [
    { required: true, message: '请输入角色名称', trigger: 'blur' },
    { max: 50, message: '长度不能超过50个字符', trigger: 'blur' }
  ]
}

const permissionTreeProps = { label: 'name', children: 'children' }

// 给对象加索引签名，解决TS索引报错
const permissionTypeTagMap: Record<string, string> = {
  DIRECTORY: 'primary',
  MENU: 'success',
  BUTTON: 'warning',
}

// 限定返回类型，避免string索引问题
const normalizeMenuType = (menuType?: string): 'DIRECTORY' | 'MENU' | 'BUTTON' | '' => {
  const type = (menuType || '').toUpperCase()
  if (['DIRECTORY', 'MENU', 'BUTTON'].includes(type)) {
    return type as 'DIRECTORY' | 'MENU' | 'BUTTON'
  }
  return ''
}

const getRoleData = async () => {
  tableLoading.value = true
  try {
    const result = await getRoleInfoPage({
      currentPage: currentPage.value,
      pageSize: pageSize.value,
      params: [{ name: 'roleName', value: queryForm.roleName }],
    })
    roleList.value = result.data?.records || []
    total.value = Number(result.data?.total || 0)
  } finally {
    tableLoading.value = false
  }
}

// 修复异步警告：加async/await
const handleSearch = async () => {
  currentPage.value = 1
  await getRoleData()
}

// 修复异步警告：加async/await
const resetSearch = async () => {
  queryForm.roleName = ''
  currentPage.value = 1
  await getRoleData()
}

const handleSizeChange = (size: number) => {
  pageSize.value = size
  currentPage.value = 1
  getRoleData()
}

const handleCurrentChange = (page: number) => {
  currentPage.value = page
  getRoleData()
}

const openRoleDialog = (row?: RoleItem) => {
  editingId.value = row?.id || null
  roleForm.value = row ? { name: row.name } : { name: '' }
  roleFormRef.value?.clearValidate()
  roleDialogVisible.value = true
}

const submitRole = async () => {
  if (!roleFormRef.value) return
  const valid = await roleFormRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    const payload = { name: roleForm.value.name }
    const result = editingId.value
        ? await updateRole(editingId.value, payload)
        : await createRole(payload)

    // 修复message类型报错：用可选链兜底
    ElMessage.success((result as any).message || (editingId.value ? '更新成功' : '创建成功'))
    roleDialogVisible.value = false
    await getRoleData()
  } catch (e: any) {
    ElMessage.error(e.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

const handleDelete = async (row: RoleItem) => {
  await ElMessageBox.confirm(`确定删除 ${row.name} 吗？`, '提示', { type: 'warning' })
  try {
    await deleteRole(row.id)
    ElMessage.success('删除成功')
    await getRoleData()
  } catch (e: any) {
    ElMessage.error(e.message || '删除失败')
  }
}

const handleAuthorize = async (role: RoleItem) => {
  currentRole.value = role
  authorizeDialogVisible.value = true

  if (permissionTree.value.length === 0) {
    const tree = await getPermissionTree()
    permissionTree.value = tree.data || []
  }

  const res = await getRolePermissionIds(role.id)
  const ids: number[] = res.data || []

  await nextTick()
  permissionTreeRef.value?.setCheckedKeys([])
  // 修复id隐式any报错：加类型注解
  ids.forEach((id: number) => permissionTreeRef.value?.setChecked(id, true, false))
}

const handleSaveAuthorize = async () => {
  if (!currentRole.value) return
  authorizing.value = true
  try {
    const checked = permissionTreeRef.value?.getCheckedKeys(false) || []
    const half = permissionTreeRef.value?.getHalfCheckedKeys() || []
    const all = [...new Set([...checked, ...half])].map(Number)

    await authorizeRole({
      roleId: currentRole.value.id,
      permissionIds: all
    })
    ElMessage.success('授权成功')
    authorizeDialogVisible.value = false
  } catch (e: any) {
    ElMessage.error(e.message || '授权失败')
  } finally {
    authorizing.value = false
  }
}

const handleAuthorizeDialogClosed = () => {
  permissionTreeRef.value?.setCheckedKeys([])
  currentRole.value = null
}

onMounted(() => getRoleData())
</script>

<template>
  <div class="role-page">
    <div class="page-toolbar">
      <div>
        <h2>角色管理</h2>
        <p>维护系统角色，并给角色分配菜单和按钮权限。</p>
      </div>
      <el-button type="primary" @click="openRoleDialog()">新增角色</el-button>
    </div>

    <div class="list-panel">
      <el-form :model="queryForm" inline class="query-form">
        <el-form-item label="角色名称">
          <el-input
              v-model="queryForm.roleName"
              clearable
              placeholder="请输入角色名称"
              @clear="handleSearch"
              @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="tableLoading" :data="roleList" border>
        <el-table-column prop="id" label="角色ID" width="120"/>
        <el-table-column prop="name" label="角色名称" min-width="220" show-overflow-tooltip/>
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="openRoleDialog(row)">编辑</el-button>
            <el-button type="success" size="small" @click="handleAuthorize(row)">授权</el-button>
            <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[10,20,50]"
            :total="total"
            layout="total,sizes,prev,pager,next,jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
        />
      </div>
    </div>
  </div>

  <!-- 修复：v-model:visible -->
  <el-dialog
      v-model:visible="roleDialogVisible"
      :title="editingId ? '编辑角色' : '新增角色'"
      width="460px"
      destroy-on-close
  >
    <el-form ref="roleFormRef" :model="roleForm" :rules="roleRules" label-width="88px">
      <el-form-item label="角色名称" prop="name">
        <el-input v-model="roleForm.name" maxlength="50" placeholder="请输入角色名称"/>
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="roleDialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="submitRole">
        {{ editingId ? '保存修改' : '新增角色' }}
      </el-button>
    </template>
  </el-dialog>

  <!-- 修复：v-model:visible -->
  <el-dialog
      v-model:visible="authorizeDialogVisible"
      :title="`角色授权 - ${currentRole?.name}`"
      width="640px"
      destroy-on-close
      @closed="handleAuthorizeDialogClosed"
  >
    <div class="permission-tree-wrapper">
      <el-tree
          ref="permissionTreeRef"
          :data="permissionTree"
          :props="permissionTreeProps"
          node-key="id"
          show-checkbox
          default-expand-all
          :check-on-click-node="true"
      >
        <template #default="{ node, data }">
          <div class="permission-tree-node">
            <span>{{ node.label }}</span>
            <el-tag
                size="small"
                :type="permissionTypeTagMap[normalizeMenuType(data.menuType)] || 'info'"
            >
              {{ normalizeMenuType(data.menuType) }}
            </el-tag>
          </div>
        </template>
      </el-tree>
    </div>

    <template #footer>
      <el-button @click="authorizeDialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="authorizing" @click="handleSaveAuthorize">确定</el-button>
    </template>
  </el-dialog>
</template>

<style scoped>
.role-page {
  height: 100%;
  padding: 22px;
  background: #f5f7fa;
  box-sizing: border-box;
}
.page-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fff;
  padding: 16px 20px;
  border-radius: 8px;
  margin-bottom: 16px;
  border: 1px solid #e4e7ed;
}
.list-panel {
  background: #fff;
  padding: 16px;
  border-radius: 8px;
  border: 1px solid #e4e7ed;
}
.pagination-wrapper {
  display: flex;
  justify-content: end;
  margin-top: 12px;
}
.permission-tree-wrapper {
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  max-height: 400px;
  overflow-y: auto;
  padding: 10px;
}
.permission-tree-node {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>