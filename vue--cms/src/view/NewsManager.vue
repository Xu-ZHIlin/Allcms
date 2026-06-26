<script setup lang="ts">
import { ref, reactive, onMounted, shallowRef } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { approveNews, createNews, deleteNews, getNewsPage, rejectNews, updateNews } from '@/http/news.ts'
import { Editor, Toolbar } from '@wangeditor/editor-for-vue'
import '@wangeditor/editor/dist/css/style.css'
import type { IDomEditor, IEditorConfig, IToolbarConfig } from '@wangeditor/editor'

// ---------- 类型 ----------
interface NewsForm {
  title: string
  category: string
  supplier: string
  reviewer: string
  content: string
}
interface NewsItem extends NewsForm {
  id: string
  status: string
  createTime?: string
  updateTime?: string
  publishTime?: string
}

// ---------- 状态 ----------
const emptyForm = (): NewsForm => ({
  title: '',
  category: '学院新闻',
  supplier: '',
  reviewer: '',
  content: '',
})

const rules: FormRules<NewsForm> = {
  title: [{ required: true, message: '请输入新闻标题', trigger: 'blur' }],
  category: [{ required: true, message: '请选择栏目', trigger: 'change' }],
  content: [{ required: true, message: '请输入新闻正文', trigger: 'change' }],
}

const categoryOptions = ['学院新闻', '通知公告', '学术活动', '学工新闻']

const formRef = ref<FormInstance>()
const editorRef = shallowRef<IDomEditor>()
const newsForm = ref<NewsForm>(emptyForm())
const dialogVisible = ref(false)
const submitting = ref(false)
const editingId = ref<string | null>(null)

const tableLoading = ref(false)
const newsList = ref<NewsItem[]>([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const queryForm = reactive({
  keyword: '',
  category: '',
  status: '',
})
const deletingIds = ref<string[]>([])

// ---------- 新增：审核状态 ----------
const auditingIds = ref<string[]>([])
const isAuditing = (id: string) => auditingIds.value.includes(id)
const canAudit = (row: NewsItem) => row.status === 'PENDING_REVIEW'

// ---------- 编辑器配置 ----------
const toolbarConfig: Partial<IToolbarConfig> = { excludeKeys: ['fullScreen'] }
const editorConfig: Partial<IEditorConfig> = {
  placeholder: '请输入新闻正文内容...',
  MENU_CONF: { uploadImage: { base64LimitSize: 5 * 1024 * 1024 } },
}

// ---------- 打开弹窗 ----------
const openDialog = (row?: NewsItem) => {
  editingId.value = row?.id || null
  newsForm.value = row
      ? { title: row.title, category: row.category, supplier: row.supplier || '', reviewer: row.reviewer || '', content: row.content }
      : emptyForm()
  formRef.value?.clearValidate()
  dialogVisible.value = true
}

// ---------- 保存 ----------
const submitNews = async () => {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    const payload = { ...newsForm.value }
    const result: any = editingId.value
        ? await updateNews(editingId.value, payload)
        : await createNews(payload)

    ElMessage.success('保存成功')
    dialogVisible.value = false
    await getNewsList()
  } catch (error: any) {
    ElMessage.error(error?.response?.data?.message || error?.message || '保存失败')
  } finally {
    submitting.value = false
  }
}

// ---------- 删除 ----------
const deleteNewsItem = async (row: NewsItem) => {
  try {
    await ElMessageBox.confirm(`确认删除“${row.title}”吗？`, '删除确认', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning',
    })
  } catch {
    return
  }
  deletingIds.value.push(row.id)
  try {
    const result: any = await deleteNews(row.id)
    ElMessage.success(result.message || '新闻已删除')
    if (newsList.value.length === 1 && currentPage.value > 1) currentPage.value -= 1
    await getNewsList()
  } catch (error: any) {
    ElMessage.error(error?.response?.data?.message || error?.message || '删除失败')
  } finally {
    deletingIds.value = deletingIds.value.filter(id => id !== row.id)
  }
}

// ---------- 列表 ----------
const getNewsList = async () => {
  tableLoading.value = true
  try {
    const result: any = await getNewsPage({
      currentPage: currentPage.value,
      pageSize: pageSize.value,
      params: [
        { name: 'keyword', value: queryForm.keyword },
        { name: 'category', value: queryForm.category },
        { name: 'status', value: queryForm.status },
      ],
    })
    newsList.value = result.data?.records || []
    total.value = Number(result.data?.total || 0)
  } catch (error: any) {
    ElMessage.error(error?.response?.data?.message || error?.message || '加载失败')
  } finally {
    tableLoading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  getNewsList()
}
const resetSearch = () => {
  queryForm.keyword = ''
  queryForm.category = ''
  queryForm.status = ''
  handleSearch()
}
const handleSizeChange = (size: number) => {
  pageSize.value = size
  currentPage.value = 1
  getNewsList()
}
const handleCurrentChange = (page: number) => {
  currentPage.value = page
  getNewsList()
}

// ---------- 辅助 ----------
const statusMap = { PENDING_REVIEW: '待审核', PUBLISHED: '已发布', REJECTED: '已驳回' }
const tagMap = { PENDING_REVIEW: 'warning', PUBLISHED: 'success', REJECTED: 'danger' } as const
const getStatusText = (s: string) => statusMap[s as keyof typeof statusMap] || s || '-'
const getStatusType = (s: string) => tagMap[s as keyof typeof tagMap] || 'info'
const formatDate = (v?: string) => (v ? v.replace('T', ' ').slice(0, 16) : '-')
const summary = (html: string, len = 60) => {
  const text = html.replace(/<[^>]+>/g, '').replace(/&nbsp;/g, ' ').replace(/\s+/g, ' ').trim()
  return text ? (text.length > len ? text.slice(0, len) + '...' : text) : '-'
}
const isDeleting = (id: string) => deletingIds.value.includes(id)

// ---------- 新增：审核方法 ----------
const auditNews = async (row: NewsItem, action: 'approve' | 'reject') => {
  const actionText = action === 'approve' ? '通过' : '驳回'
  try {
    await ElMessageBox.confirm(`确认${actionText}这条新闻吗？`, '审核确认', {
      confirmButtonText: actionText,
      cancelButtonText: '取消',
      type: action === 'approve' ? 'success' : 'warning',
    })
  } catch {
    return
  }

  auditingIds.value.push(row.id)
  try {
    const result: any = action === 'approve'
        ? await approveNews(row.id)
        : await rejectNews(row.id)

    ElMessage.success(result.message || `新闻已${actionText}`)
    await getNewsList()
  } catch (error: any) {
    ElMessage.error(error?.response?.data?.message || error?.message || `新闻${actionText}失败`)
  } finally {
    auditingIds.value = auditingIds.value.filter(id => id !== row.id)
  }
}

// ---------- 生命周期 ----------
onMounted(() => getNewsList())
</script>

<template>
  <div class="news-page">
    <!-- 工具栏 -->
    <div class="page-toolbar">
      <div>
        <h2>新闻管理</h2>
        <p>维护标题、栏目、审核信息和正文内容。</p>
      </div>
      <el-button type="primary" @click="openDialog()">添加新闻</el-button>
    </div>

    <!-- 列表 -->
    <div class="list-panel">
      <el-form :model="queryForm" inline class="query-form">
        <el-form-item label="关键词">
          <el-input v-model="queryForm.keyword" clearable placeholder="标题、供稿、审稿" @clear="handleSearch" @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="栏目">
          <el-select v-model="queryForm.category" clearable placeholder="全部栏目" style="width:150px" @clear="handleSearch">
            <el-option v-for="item in categoryOptions" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" clearable placeholder="全部状态" style="width:130px" @clear="handleSearch">
            <el-option label="待审核" value="PENDING_REVIEW" />
            <el-option label="已发布" value="PUBLISHED" />
            <el-option label="已驳回" value="REJECTED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="tableLoading" :data="newsList" border class="news-table" height="100%">
        <el-table-column prop="title" label="新闻标题" min-width="220" show-overflow-tooltip />
        <el-table-column prop="category" label="栏目" width="110" />
        <el-table-column label="正文摘要" min-width="240">
          <template #default="{ row }">{{ summary(row.content) }}</template>
        </el-table-column>
        <el-table-column prop="supplier" label="供稿" width="110" show-overflow-tooltip>
          <template #default="{ row }">{{ row.supplier || '-' }}</template>
        </el-table-column>
        <el-table-column prop="reviewer" label="审稿" width="110" show-overflow-tooltip>
          <template #default="{ row }">{{ row.reviewer || '-' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="发布时间" width="160">
          <template #default="{ row }">{{ formatDate(row.publishTime) }}</template>
        </el-table-column>
        <el-table-column label="更新时间" width="160">
          <template #default="{ row }">{{ formatDate(row.updateTime || row.createTime) }}</template>
        </el-table-column>

        <!-- 新增：审核列 -->
        <el-table-column label="审核" width="160" fixed="right">
          <template #default="{ row }">
            <el-button
                type="success"
                size="small"
                :disabled="!canAudit(row)"
                :loading="isAuditing(row.id)"
                @click="auditNews(row, 'approve')"
            >
              通过
            </el-button>
            <el-button
                type="danger"
                size="small"
                :disabled="!canAudit(row)"
                :loading="isAuditing(row.id)"
                @click="auditNews(row, 'reject')"
            >
              驳回
            </el-button>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="openDialog(row)">编辑</el-button>
            <el-button type="danger" size="small" :loading="isDeleting(row.id)" @click="deleteNewsItem(row)">删除</el-button>
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

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑新闻' : '添加新闻'" width="860px" destroy-on-close>
      <el-form ref="formRef" :model="newsForm" :rules="rules" label-width="86px" class="news-form">
        <div class="form-basic">
          <el-form-item label="新闻标题" prop="title">
            <el-input v-model="newsForm.title" maxlength="80" show-word-limit placeholder="请输入新闻标题" />
          </el-form-item>
          <el-row :gutter="18">
            <el-col :span="8">
              <el-form-item label="栏目" prop="category">
                <el-select v-model="newsForm.category" placeholder="请选择栏目" style="width:100%">
                  <el-option v-for="item in categoryOptions" :key="item" :label="item" :value="item" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="18">
            <el-col :span="12">
              <el-form-item label="供稿"><el-input v-model="newsForm.supplier" placeholder="请输入供稿人" /></el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="审稿"><el-input v-model="newsForm.reviewer" placeholder="请输入审稿人" /></el-form-item>
            </el-col>
          </el-row>
        </div>

        <el-form-item label="正文内容" prop="content" class="content-form-item">
          <div ref="editorWrapperRef" class="editor-wrapper">
            <Toolbar :editor="editorRef" :default-config="toolbarConfig" mode="default" class="editor-toolbar" />
            <Editor
                v-model="newsForm.content"
                :default-config="editorConfig"
                mode="default"
                class="editor-content"
                @on-created="editorRef = $event"
            />
          </div>
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <div class="editor-footer-border"></div>
          <div class="dialog-footer-actions">
            <el-button @click="dialogVisible = false">取消</el-button>
            <el-button type="primary" :loading="submitting" @click="submitNews">{{ editingId ? '保存修改' : '保存新闻' }}</el-button>
          </div>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
/* ----- 页面布局 ----- */
.news-page {
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
.page-toolbar h2 {
  margin: 0 0 6px;
  color: #1f2d3d;
  font-size: 22px;
  font-weight: 600;
}
.page-toolbar p {
  margin: 0;
  color: #7a8499;
  font-size: 14px;
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
.query-form {
  display: flex;
  flex-wrap: wrap;
  gap: 0 8px;
}
.query-form :deep(.el-form-item) {
  margin-bottom: 0;
}
.news-table {
  min-height: 0;
}
.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
}

/* ----- 弹窗样式 ----- */
:global(.news-dialog.el-dialog),
:global(.news-dialog .el-dialog) {
  width: min(1040px, calc(100vw - 48px));
  height: min(840px, calc(100vh - 48px));
  display: flex;
  flex-direction: column;
  margin: 0 auto;
}
:global(.news-dialog.el-dialog .el-dialog__header),
:global(.news-dialog .el-dialog__header),
:global(.news-dialog.el-dialog .el-dialog__footer),
:global(.news-dialog .el-dialog__footer) {
  flex-shrink: 0;
}
:global(.news-dialog.el-dialog .el-dialog__body),
:global(.news-dialog .el-dialog__body) {
  flex: 1;
  min-height: 0;
  overflow: hidden;
  padding-bottom: 0;
}
.news-form {
  height: 100%;
  display: grid;
  grid-template-rows: auto minmax(0, 1fr);
  overflow: hidden;
}
.form-basic {
  min-height: 0;
  padding-right: 8px;
}
.content-form-item {
  min-height: 0;
  margin-bottom: 0;
}
.content-form-item :deep(.el-form-item__content) {
  height: 100%;
  min-height: 0;
  display: flex;
  align-items: stretch;
}
.editor-wrapper {
  width: 100%;
  height: 100%;
  min-height: 0;
  display: grid;
  grid-template-rows: auto minmax(0, 1fr);
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  overflow: hidden;
}
.editor-toolbar {
  position: relative;
  z-index: 2;
  min-height: 40px;
  background: #fff;
  border-bottom: 1px solid #dcdfe6;
}
.editor-content {
  height: 100%;
  min-height: 0;
  overflow: hidden;
}
.editor-content :deep(> div) {
  height: 100% !important;
}
:deep(.editor-content .w-e-text-container) {
  height: 100% !important;
  overflow: hidden !important;
}
:deep(.editor-content .w-e-scroll) {
  height: 100% !important;
  overflow-y: auto !important;
  scroll-behavior: auto;
}
:deep(.editor-content [data-slate-editor]) {
  min-height: 100%;
}
:global(.news-dialog.el-dialog .el-dialog__footer),
:global(.news-dialog .el-dialog__footer) {
  padding-top: 0;
}
.dialog-footer {
  text-align: right;
}
.editor-footer-border {
  height: 1px;
  margin: 0 0 16px 86px;
  background: #dcdfe6;
}
.dialog-footer-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>