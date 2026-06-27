<script lang="ts" setup>
import { nextTick, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules, type ElTreeInstance } from 'element-plus'
import {
  authorizeRole,
  createRole,
  deleteRole,
  getPermissionTree,
  getRoleInfoPage,
  getRolePermissionIds,
  updateRole
} from '@/http/role.ts'
import { useUserStore } from '@/stores/user'

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
  menuType: string
  children?: PermissionItem[]
}

// 引入用户 Store 用于校验当前登录用户的权限
const userStore = useUserStore()

// 角色校验规则
const roleRules: FormRules = {
  name: [
    { required: true, message: '请输入角色名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ]
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
const permissionTreeRef = ref<ElTreeInstance | null>(null)
const queryForm = reactive({
  roleName: '',
})

const permissionTreeProps = {
  label: 'name',
  children: 'children',
}

const permissionTypeTagMap = {
  DIRECTORY: 'primary',
  MENU: 'success',
  BUTTON: 'warning',
}

const normalizeMenuType = (menuType?: string) => (menuType || '').toUpperCase()

// 【核心逻辑】检查当前登录用户是否具备特定权限码
const hasPermission = (permCode: string) => {
  if (!userStore.userInfo || !userStore.userInfo.permissionList) {
    return false
  }
  return userStore.userInfo.permissionList.some(
      (p: any) => p.code === permCode
  )
}

// 加载角色分页列表
const getRoleData = async () => {
  tableLoading.value = true
  try {
    const result = await getRoleInfoPage({
      currentPage: currentPage.value,
      pageSize: pageSize.value,
      roleName: queryForm.roleName
    })
    roleList.value = result.data?.records || []
    total.value = Number(result.data?.total || 0)
  } catch (err) {
    ElMessage.error('获取角色列表失败')
  } finally {
    tableLoading.value = false
  }
}

// 分页-每页条数改变
const handleSizeChange = () => {
  currentPage.value = 1
  getRoleData()
}

// 分页-页码切换
const handleCurrentChange = () => {
  getRoleData()
}

// 查询
let searchTimer: number | null = null
const handleSearch = () => {
  if (searchTimer) clearTimeout(searchTimer)
  searchTimer = window.setTimeout(() => {
    currentPage.value = 1
    getRoleData()
  }, 300)
}

// 重置搜索
const resetSearch = () => {
  queryForm.roleName = ''
  currentPage.value = 1
  getRoleData()
}

// 打开新增/编辑弹窗
const openRoleDialog = (row?: RoleItem) => {
  editingId.value = row?.id || null
  roleForm.value = row ? { name: row.name } : { name: '' }
  roleFormRef.value?.clearValidate()
  roleDialogVisible.value = true
}

// 提交角色新增/编辑
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

    ElMessage.success(result.message || (editingId.value ? '角色已更新' : '角色已新增'))
    roleDialogVisible.value = false
    await getRoleData()
  } catch (err) {
    ElMessage.error('保存角色失败')
  } finally {
    submitting.value = false
  }
}

// 删除角色
const handleDelete = async (row: RoleItem) => {
  try {
    await ElMessageBox.confirm(`确认删除角色“${row.name}”吗？`, '删除确认', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await deleteRole(row.id)
    ElMessage.success('角色已删除')
    await getRoleData()
  } catch (err: any) {
    if (err !== 'cancel') ElMessage.error('删除角色失败')
  }
}

// 打开授权弹窗，回显权限
const handleAuthorize = async (role: RoleItem) => {
  currentRole.value = role
  authorizeDialogVisible.value = true

  if (permissionTree.value.length === 0) {
    try {
      const treeResult = await getPermissionTree()
      permissionTree.value = treeResult.data || []
    } catch (err) {
      ElMessage.error('加载权限树失败')
      authorizeDialogVisible.value = false
      return
    }
  }

  let permissionIds: number[] = []
  try {
    const result = await getRolePermissionIds(role.id)
    permissionIds = result.data || []
  } catch (err) {
    ElMessage.error('获取角色权限失败')
    return
  }

  await nextTick()
  permissionTreeRef.value?.setCheckedKeys(permissionIds)
}

// 保存角色权限授权
const handleSaveAuthorize = async () => {
  if (!currentRole.value || !permissionTreeRef.value) return
  authorizing.value = true

  try {
    const checkedKeys = permissionTreeRef.value.getCheckedKeys(false) || []
    const halfCheckedKeys = permissionTreeRef.value.getHalfCheckedKeys() || []
    const selectedPermissionIds = [...new Set([...checkedKeys, ...halfCheckedKeys])].map(Number)

    if (selectedPermissionIds.length === 0) {
      ElMessage.warning('至少需要分配一项权限')
      authorizing.value = false
      return
    }

    await authorizeRole({
      roleId: currentRole.value.id,
      permissionIds: selectedPermissionIds,
    })
    ElMessage.success('授权保存成功')
    authorizeDialogVisible.value = false
  } catch (err) {
    ElMessage.error('权限授权失败')
  } finally {
    authorizing.value = false
  }
}

// 关闭授权弹窗，清除回显状态
const handleAuthorizeDialogClosed = async () => {
  await nextTick()
  if (permissionTreeRef.value && typeof permissionTreeRef.value.setCheckedKeys === 'function') {
    permissionTreeRef.value.setCheckedKeys([])
  }
  if (permissionTreeRef.value && typeof permissionTreeRef.value.setExpandedKeys === 'function') {
    permissionTreeRef.value.setExpandedKeys([])
  }
  currentRole.value = null
}

// 页面初始化加载列表
onMounted(() => {
  getRoleData()
})
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
        <el-table-column prop="id" label="角色ID" width="120" />
        <el-table-column prop="name" label="角色名称" min-width="220" show-overflow-tooltip />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <!-- 编辑按钮：动态权限 role:edit -->
            <el-tooltip
                content="您没有编辑角色的操作权限"
                placement="top"
                :disabled="hasPermission('role:edit')"
            >
              <span>
                <el-button
                    type="primary"
                    size="small"
                    @click="openRoleDialog(row)"
                    :disabled="!hasPermission('role:edit')"
                >
                  编辑
                </el-button>
              </span>
            </el-tooltip>

            <!-- 授权按钮：动态权限 role:assign-permission -->
            <el-tooltip
                content="您没有分配权限的操作权限"
                placement="top"
                :disabled="hasPermission('role:assign-permission')"
            >
              <span>
                <el-button
                    type="success"
                    size="small"
                    @click="handleAuthorize(row)"
                    :disabled="!hasPermission('role:assign-permission')"
                >
                  授权
                </el-button>
              </span>
            </el-tooltip>

            <!-- 删除按钮：动态权限 role:delete -->
            <el-tooltip
                content="您没有删除角色的操作权限"
                placement="top"
                :disabled="hasPermission('role:delete')"
            >
              <span>
                <el-button
                    type="danger"
                    size="small"
                    @click="handleDelete(row)"
                    :disabled="!hasPermission('role:delete')"
                >
                  删除
                </el-button>
              </span>
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[10, 20, 50]"
            :total="total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
        />
      </div>
    </div>
  </div>

  <!-- 新增/编辑角色弹窗 -->
  <el-dialog
      v-model="roleDialogVisible"
      :title="editingId ? '编辑角色' : '新增角色'"
      width="460px"
      destroy-on-close
      align-center
  >
    <el-form ref="roleFormRef" :model="roleForm" :rules="roleRules" label-width="88px">
      <el-form-item label="角色名称" prop="name">
        <el-input v-model="roleForm.name" maxlength="50" placeholder="请输入角色名称" />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="roleDialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="submitRole">
        {{ editingId ? '保存修改' : '新增角色' }}
      </el-button>
    </template>
  </el-dialog>

  <!-- 角色授权弹窗 -->
  <el-dialog
      v-model="authorizeDialogVisible"
      :title="`角色授权${currentRole ? ` - ${currentRole.name}` : ''}`"
      width="640px"
      destroy-on-close
      align-center
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
          :check-strictly="true"
      >
        <template #default="{ node, data }">
          <div class="permission-tree-node">
            <span>{{ node.label }}</span>
            <el-tag
                class="permission-type-tag"
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
  background: #f5f7fb;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.page-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;
  padding: 18px 20px;
  background: #fff;
  border: 1px solid #e5e6eb;
  border-radius: 8px;
}

.list-panel {
  flex: 1;
  min-height: 0;
  display: grid;
  grid-template-rows: auto minmax(0, 1fr) auto;
  gap: 12px;
  padding: 16px;
  background: #fff;
  border: 1px solid #e5e6eb;
  border-radius: 8px;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
}

.permission-tree-wrapper {
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 6px;
  max-height: 420px;
  overflow-y: auto;
  padding: 10px 12px;
}

.permission-tree-node {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
}

.permission-type-tag {
  margin-left: auto;
}
</style>