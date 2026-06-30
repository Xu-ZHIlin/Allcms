<script setup lang="ts">
import { ref, reactive, onMounted, shallowRef } from 'vue'
import { approveNews, createNews, deleteNews, getNewsPage, rejectNews, updateNews } from '@/http/news.ts'
import { Editor, Toolbar } from '@wangeditor/editor-for-vue'
import '@wangeditor/editor/dist/css/style.css'
import type { IDomEditor, IEditorConfig, IToolbarConfig } from '@wangeditor/editor'

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

const emptyForm = (): NewsForm => ({
  title: '', category: '学院新闻', supplier: '', reviewer: '', content: '',
})

const categoryOptions = ['学院新闻', '通知公告', '学术活动', '学工新闻']

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
const queryForm = reactive({ keyword: '', category: '', status: '' })
const deletingIds = ref<string[]>([])
const auditingIds = ref<string[]>([])

const isAuditing = (id: string) => auditingIds.value.includes(id)
const canAudit = (row: NewsItem) => row.status === 'PENDING_REVIEW'

const toolbarConfig: Partial<IToolbarConfig> = { excludeKeys: ['fullScreen'] }
const editorConfig: Partial<IEditorConfig> = {
  placeholder: '请输入新闻正文内容...',
  MENU_CONF: { uploadImage: { base64LimitSize: 5 * 1024 * 1024 } },
}

const openDialog = (row?: NewsItem) => {
  editingId.value = row?.id || null
  newsForm.value = row
      ? { title: row.title, category: row.category, supplier: row.supplier || '', reviewer: row.reviewer || '', content: row.content }
      : emptyForm()
  dialogVisible.value = true
}

const submitNews = async () => {
  if (!newsForm.value.title || !newsForm.value.category || !newsForm.value.content) {
    alert('请填写完整的标题、栏目和正文'); return
  }
  submitting.value = true
  try {
    const payload = { ...newsForm.value }
    if (editingId.value) await updateNews(editingId.value, payload)
    else await createNews(payload)
    alert('保存成功')
    dialogVisible.value = false
    await getNewsList()
  } catch (error: any) {
    alert('保存失败: ' + (error?.response?.data?.message || error?.message))
  } finally { submitting.value = false }
}

const deleteNewsItem = async (row: NewsItem) => {
  if (!confirm(`确认删除“${row.title}”吗？`)) return
  deletingIds.value.push(row.id)
  try {
    await deleteNews(row.id)
    alert('新闻已删除')
    if (newsList.value.length === 1 && currentPage.value > 1) currentPage.value -= 1
    await getNewsList()
  } catch (error: any) {
    alert('删除失败: ' + (error?.response?.data?.message || error?.message))
  } finally {
    deletingIds.value = deletingIds.value.filter(id => id !== row.id)
  }
}

const getNewsList = async () => {
  tableLoading.value = true
  try {
    const result: any = await getNewsPage({
      currentPage: currentPage.value, pageSize: pageSize.value,
      params: [
        { name: 'keyword', value: queryForm.keyword },
        { name: 'category', value: queryForm.category },
        { name: 'status', value: queryForm.status },
      ],
    })
    newsList.value = result.data?.records || []
    total.value = Number(result.data?.total || 0)
  } catch (error: any) {
    alert('加载失败: ' + (error?.response?.data?.message || error?.message))
  } finally { tableLoading.value = false }
}

const handleSearch = () => { currentPage.value = 1; getNewsList() }
const resetSearch = () => { queryForm.keyword = ''; queryForm.category = ''; queryForm.status = ''; handleSearch() }

const auditNews = async (row: NewsItem, action: 'approve' | 'reject') => {
  const actionText = action === 'approve' ? '通过' : '驳回'
  if (!confirm(`确认${actionText}这条新闻吗？`)) return
  auditingIds.value.push(row.id)
  try {
    if (action === 'approve') await approveNews(row.id)
    else await rejectNews(row.id)
    alert(`新闻已${actionText}`)
    await getNewsList()
  } catch (error: any) {
    alert(`新闻${actionText}失败: ` + (error?.response?.data?.message || error?.message))
  } finally { auditingIds.value = auditingIds.value.filter(id => id !== row.id) }
}

const getStatusText = (s: string) => {
  const map = { PENDING_REVIEW: '待审核', PUBLISHED: '已发布', REJECTED: '已驳回' }
  return map[s as keyof typeof map] || s || '-'
}

const formatDate = (v?: string) => (v ? v.replace('T', ' ').slice(0, 16) : '-')
const summary = (html: string, len = 60) => {
  const text = html.replace(/<[^>]+>/g, '').replace(/&nbsp;/g, ' ').replace(/\s+/g, ' ').trim()
  return text ? (text.length > len ? text.slice(0, len) + '...' : text) : '-'
}

onMounted(() => getNewsList())
</script>

<template>
  <div class="layout-container">
    <!-- 工具栏 -->
    <div class="toolbar">
      <div class="title"><h2>新闻管理</h2><p>维护标题、栏目、审核信息和正文内容。</p></div>
      <button class="btn-primary" @click="openDialog()">添加新闻</button>
    </div>

    <!-- 搜索 -->
    <div class="search-panel">
      <input type="text" v-model="queryForm.keyword" placeholder="标题、供稿、审稿" />
      <select v-model="queryForm.category"><option value="">全部栏目</option><option v-for="item in categoryOptions" :key="item" :value="item">{{ item }}</option></select>
      <select v-model="queryForm.status"><option value="">全部状态</option><option value="PENDING_REVIEW">待审核</option><option value="PUBLISHED">已发布</option><option value="REJECTED">已驳回</option></select>
      <button class="btn-primary" @click="handleSearch">查询</button>
      <button @click="resetSearch">重置</button>
    </div>

    <!-- 表格 -->
    <div class="table-wrap">
      <table border="1" class="raw-table">
        <thead><tr><th>标题</th><th>栏目</th><th>摘要</th><th>供稿</th><th>审稿</th><th>状态</th><th>发布时间</th><th>更新时间</th><th>审核</th><th>操作</th></tr></thead>
        <tbody>
        <tr v-if="tableLoading"><td colspan="10" style="text-align:center; padding:20px;">加载中...</td></tr>
        <tr v-else-if="newsList.length === 0"><td colspan="10" style="text-align:center; padding:20px;">暂无数据</td></tr>
        <tr v-for="row in newsList" :key="row.id">
          <td>{{ row.title }}</td>
          <td>{{ row.category }}</td>
          <td>{{ summary(row.content) }}</td>
          <td>{{ row.supplier || '-' }}</td>
          <td>{{ row.reviewer || '-' }}</td>
          <td><span class="tag-status">{{ getStatusText(row.status) }}</span></td>
          <td>{{ formatDate(row.publishTime) }}</td>
          <td>{{ formatDate(row.updateTime || row.createTime) }}</td>
          <td>
            <div class="act-btns">
              <button :disabled="!canAudit(row)" :class="['btn-xs', canAudit(row) ? 'btn-success' : 'btn-disabled']" @click="auditNews(row, 'approve')">通过</button>
              <button :disabled="!canAudit(row)" :class="['btn-xs', canAudit(row) ? 'btn-danger' : 'btn-disabled']" @click="auditNews(row, 'reject')">驳回</button>
            </div>
          </td>
          <td>
            <div class="act-btns">
              <button class="btn-xs btn-primary" @click="openDialog(row)">编辑</button>
              <button class="btn-xs btn-danger" @click="deleteNewsItem(row)">删除</button>
            </div>
          </td>
        </tr>
        </tbody>
      </table>
    </div>

    <!-- 分页 -->
    <div class="pagination">
      <span>共 {{ total }} 条</span>
      <select v-model="pageSize"><option :value="10">10</option><option :value="20">20</option><option :value="50">50</option></select>
      <button @click="currentPage > 1 && (currentPage--, getNewsList())">上一页</button>
      <span>第 <input type="number" v-model="currentPage" style="width:40px;text-align:center;" @change="getNewsList"/> 页</span>
      <button @click="currentPage < Math.ceil(total/pageSize) && (currentPage++, getNewsList())">下一页</button>
    </div>

    <!-- 原生弹窗 -->
    <div class="modal-overlay" v-if="dialogVisible">
      <div class="modal-box">
        <div class="modal-header"><h3>{{ editingId ? '编辑新闻' : '添加新闻' }}</h3><span class="close" @click="dialogVisible=false">×</span></div>
        <div class="modal-body">
          <div class="form-group"><label>标题</label><input type="text" v-model="newsForm.title" /></div>
          <div class="form-group"><label>栏目</label><select v-model="newsForm.category"><option v-for="item in categoryOptions" :key="item" :value="item">{{ item }}</option></select></div>
          <div class="form-row"><div class="form-group half"><label>供稿</label><input type="text" v-model="newsForm.supplier" /></div><div class="form-group half"><label>审稿</label><input type="text" v-model="newsForm.reviewer" /></div></div>
          <div class="form-group"><label>正文</label>
            <div class="editor-wrap"><Toolbar :editor="editorRef" :default-config="toolbarConfig" /><Editor v-model="newsForm.content" :default-config="editorConfig" @on-created="editorRef = $event" /></div>
          </div>
        </div>
        <div class="modal-footer"><button @click="dialogVisible=false">取消</button><button class="btn-primary" :disabled="submitting" @click="submitNews">{{ editingId ? '保存修改' : '保存新闻' }}</button></div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* 纯粹原生黑白灰极简样式 */
.layout-container { padding: 15px; background: #f5f7fa; font-family: sans-serif; }
.toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.search-panel { padding: 10px; background: #fff; border: 1px solid #ccc; margin-bottom: 12px; display: flex; gap: 8px; flex-wrap: wrap; }
.search-panel input, .search-panel select { border: 1px solid #ccc; padding: 4px 8px; }
.raw-table { width: 100%; border-collapse: collapse; background: #fff; font-size: 13px; }
.raw-table th, .raw-table td { border: 1px solid #ccc; padding: 6px 10px; text-align: left; }
.raw-table th { background: #eee; font-weight: bold; }
.tag-status { color: #333; } /* 原始无色块 */
.btn-primary { background: #0056b3; color: #fff; border: 1px solid #0056b3; padding: 4px 10px; cursor: pointer; }
.btn-danger { background: #d32f2f; color: #fff; border: 1px solid #d32f2f; }
.btn-success { background: #2e7d32; color: #fff; border: 1px solid #2e7d32; }
.btn-xs { padding: 2px 6px; font-size: 12px; border: 1px solid transparent; cursor: pointer; }
.btn-disabled { background: #ccc; color: #666; border: 1px solid #bbb; cursor: not-allowed; }
.act-btns { display: flex; gap: 4px; flex-wrap: wrap; }
.pagination { display: flex; align-items: center; gap: 10px; margin-top: 12px; font-size: 13px; }
.modal-overlay { position: fixed; top:0; left:0; width:100%; height:100%; background: rgba(0,0,0,0.4); z-index:2000; display:flex; justify-content:center; align-items:center; }
.modal-box { background: #fff; width: 800px; max-height: 85vh; border: 2px solid #000; display:flex; flex-direction:column; }
.modal-header { border-bottom: 1px solid #ccc; padding: 12px 20px; display:flex; justify-content:space-between; align-items:center; }
.modal-body { padding: 16px 20px; overflow-y: auto; flex:1; }
.modal-footer { border-top: 1px solid #ccc; padding: 12px 20px; text-align:right; }
.form-group { margin-bottom: 12px; }
.form-group label { display: block; font-size: 13px; font-weight:bold; margin-bottom:4px; }
.form-group input, .form-group select { width: 100%; border: 1px solid #ccc; padding: 4px 6px; box-sizing:border-box; }
.form-row { display: flex; gap: 16px; }
.form-group.half { flex:1; }
.editor-wrap { border: 1px solid #ccc; }
.close { cursor: pointer; font-size: 24px; line-height:1; }
</style>