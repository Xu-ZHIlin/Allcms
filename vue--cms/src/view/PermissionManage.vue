<script setup lang="ts">
import {ref, reactive, onMounted} from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getPermissionPage,
  createPermission,
  updatePermission,
  deletePermission,
  getPermissionTree,
} from "@/http/permission.ts";

// 分页
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

// 加载状态
const tableLoading = ref(false)

// 查询表单
const queryForm = reactive({
  name: '',
  code: '',
  menuType: '',
})

// 列表 & 树形数据
const permissionList = ref<any[]>([])
const parentOptions = ref<any[]>([])
const parentNameMap = new Map()

// 弹窗 & 编辑
const dialogVisible = ref(false)
const editingId = ref<number | null>(null)

// 表单
const formRef = ref()
const permissionForm = ref({
  name: '',
  menuType: '',
  parentId: 0,
  code: '',
  path: '',
  sort: 0,
})

// 校验规则
const rules = reactive({
  name: [{ required: true, message: '请输入权限名称', trigger: 'blur' }],
  menuType: [{ required: true, message: '请选择权限类型', trigger: 'change' }],
  code: [{ required: true, message: '请输入权限标识', trigger: 'blur' }],
})

// 权限类型映射
const permissionTypeTagMap = {
  DIRECTORY: 'primary',
  MENU: 'success',
  BUTTON: 'warning',
}
const permissionTypeTextMap = {
  DIRECTORY: '目录',
  MENU: '菜单',
  BUTTON: '按钮',
}
const normalizeMenuType = (type:String) => type?.toUpperCase()

// 获取权限树（父级选择器）
const getPermissionTreeData = async () => {
  const res = await getPermissionTree()
  parentOptions.value = res.data || []
  parentOptions.value.forEach((item) => {
    parentNameMap.set(item.id, item.label)
  })
}

// 打开弹窗（新增/编辑）
const openPermissionDialog = (row?:any) => {
  editingId.value = row?.id || null
  dialogVisible.value = true
  if (row) permissionForm.value = { ...row }
}

const getPermissions = async () => {
  const result = await getPermissionPage({
    currentPage: currentPage.value,
    pageSize: pageSize.value,
    params: [
      {name: 'name', value: queryForm.name},
      {name: 'code', value: queryForm.code},
      {name: 'menuType', value: queryForm.menuType},
    ],
  })
  permissionList.value = result.data?.records || []
  total.value = Number(result.data?.total || 0)
}

const submitPermission = async () => {
  await formRef.value.validate()
  const payload = {
    name: permissionForm.value.name,
    menuType: permissionForm.value.menuType,
    code: permissionForm.value.code,
    path: permissionForm.value.path,
    parentId: permissionForm.value.parentId,
    sort: permissionForm.value.sort,
  }
  const result = editingId.value
      ? await updatePermission(editingId.value, payload)
      : await createPermission(payload)

  ElMessage.success((result as any).message || (editingId.value ? '权限已更新' : '权限已新增'))
  dialogVisible.value = false
  await Promise.all([getPermissions(), getPermissionTreeData()])
}

const handleDelete = async (row:any) => {
  await ElMessageBox.confirm(`确认删除权限“${row.name}”吗？`, '删除确认', {
    confirmButtonText: '删除',
    cancelButtonText: '取消',
    type: 'warning',
  })

  const result = await deletePermission(row.id)
  ElMessage.success((result as any).message || '权限已删除')
  await Promise.all([getPermissions(), getPermissionTreeData()])
}

// 页面加载时自动获取列表
onMounted(() => {
  getPermissions()
  getPermissionTreeData()
})
</script>

<template>
  <!-- 搜索栏 + 新增按钮 -->
  <div style="padding: 16px; background: #fff; margin-bottom: 16px; border-radius: 4px;">
    <div style="display: flex; justify-content: space-between; margin-bottom: 12px;">
      <h3 style="margin: 0;">权限管理</h3>
      <el-button type="primary" @click="openPermissionDialog()">+ 新增权限</el-button>
    </div>

    <el-form :model="queryForm" inline>
      <el-form-item label="权限名称">
        <el-input v-model="queryForm.name" placeholder="权限名称" clearable />
      </el-form-item>
      <el-form-item label="权限标识">
        <el-input v-model="queryForm.code" placeholder="权限标识" clearable />
      </el-form-item>
      <el-form-item label="权限类型">
        <el-select v-model="queryForm.menuType" placeholder="请选择" clearable style="width: 120px">
          <el-option label="目录" value="DIRECTORY" />
          <el-option label="菜单" value="MENU" />
          <el-option label="按钮" value="BUTTON" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="getPermissions">搜索</el-button>
        <el-button @click="queryForm = { name: '', code: '', menuType: '' }">重置</el-button>
      </el-form-item>
    </el-form>
  </div>
  <!-- 分页器 -->
  <el-pagination
      v-model:current-page="currentPage"
      v-model:page-size="pageSize"
      :total="total"
      background
      layout="total, sizes, prev, pager, next, jumper"
      @size-change="getPermissions"
      @current-change="getPermissions"
      style="margin-top: 16px; text-align: right;"
  />
  <el-table v-loading="tableLoading" :data="permissionList" border height="100%">
    <el-table-column prop="id" label="权限ID" width="90"/>
    <el-table-column prop="name" label="权限名称" min-width="150" show-overflow-tooltip/>
    <el-table-column label="类型" width="110">
      <template #default="{ row }">
        <el-tag :type="permissionTypeTagMap[normalizeMenuType(row.menuType)] || 'info'" size="small">
          {{ permissionTypeTextMap[normalizeMenuType(row.menuType)] || row.menuType }}
        </el-tag>
      </template>
    </el-table-column>
    <el-table-column prop="code" label="权限标识" min-width="190" show-overflow-tooltip/>
    <el-table-column prop="path" label="路由路径" min-width="170" show-overflow-tooltip/>
    <el-table-column label="父级权限" min-width="140" show-overflow-tooltip>
      <template #default="{ row }">{{ parentNameMap.get(row.parentId) || '顶级权限' }}</template>
    </el-table-column>
    <el-table-column prop="sort" label="排序" width="90"/>
    <el-table-column label="操作" width="150" fixed="right">
      <template #default="{ row }">
        <el-button type="primary" size="small" @click="openPermissionDialog(row)">编辑</el-button>
        <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
      </template>
    </el-table-column>
  </el-table>

  <el-dialog
      v-model="dialogVisible"
      :title="editingId ? '编辑权限' : '新增权限'"
      width="560px"
      destroy-on-close
      align-center
  >
    <el-form ref="formRef" :model="permissionForm" :rules="rules" label-width="92px">
      <el-form-item label="权限名称" prop="name">
        <el-input v-model="permissionForm.name" maxlength="50" placeholder="请输入权限名称"/>
      </el-form-item>
      <el-form-item label="权限类型" prop="menuType">
        <el-select v-model="permissionForm.menuType" placeholder="请选择权限类型" style="width: 100%">
          <el-option label="目录" value="DIRECTORY"/>
          <el-option label="菜单" value="MENU"/>
          <el-option label="按钮" value="BUTTON"/>
        </el-select>
      </el-form-item>
      <el-form-item label="父级权限">
        <el-select v-model="permissionForm.parentId" filterable placeholder="请选择父级权限" style="width: 100%">
          <el-option label="顶级权限" :value="0"/>
          <el-option
              v-for="option in parentOptions"
              :key="option.id"
              :label="option.label"
              :value="option.id"
              :disabled="option.id === editingId"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="权限标识">
        <el-input v-model="permissionForm.code" maxlength="100" placeholder="例如 role:add"/>
      </el-form-item>
      <el-form-item label="路由路径">
        <el-input v-model="permissionForm.path" maxlength="255" placeholder="例如 /permission/list"/>
      </el-form-item>
      <el-form-item label="排序">
        <el-input-number v-model="permissionForm.sort" :min="0" :max="9999" controls-position="right"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" @click="submitPermission">保存</el-button>
    </template>
  </el-dialog>
</template>

<style scoped>

</style>