<script setup lang="ts">
import {nextTick, ref, shallowRef} from "vue";
import '@wangeditor/editor/dist/css/style.css'
import {Editor, Toolbar} from '@wangeditor/editor-for-vue'
import {
  ElMessage,
  type FormInstance,
  type FormRules,
} from "element-plus";
import type {IDomEditor, IEditorConfig, IToolbarConfig} from "@wangeditor/editor";
import {createNews} from "@/http/news.ts";

interface NewsForm {
  title: string
  category: string
  supplier: string
  reviewer: string
  content: string
}

const emptyForm = (): NewsForm => ({
  title: '',
  category: '学院新闻',
  supplier: '',
  reviewer: '',
  content: '',
})

const rules: FormRules<NewsForm> = {
  title: [{required: true, message: '请输入新闻标题', trigger: 'blur'}],
  category: [{required: true, message: '请选择栏目', trigger: 'change'}],
  content: [{required: true, message: '请输入新闻正文', trigger: 'change'}],
}

const categoryOptions = ['学院新闻', '通知公告', '学术活动', '学工新闻']
const formRef = ref<FormInstance>()
const editorRef = shallowRef<IDomEditor>()
const newsForm = ref<NewsForm>(emptyForm())
const dialogVisible = ref(false)
const submitting = ref(false)
const shouldAutoScrollEditor = ref(false)
const removeEditorListeners = ref<(() => void) | null>(null)
let editorMutationObserver: MutationObserver | null = null
const editorWrapperRef = ref<HTMLElement>()

const toolbarConfig: Partial<IToolbarConfig> = {
  excludeKeys: ['fullScreen'],
}

const editorConfig: Partial<IEditorConfig> = {
  placeholder: '请输入新闻正文内容...',
  MENU_CONF: {
    uploadImage: {
      base64LimitSize: 5 * 1024 * 1024,
    },
  },
}

const openAddDialog = () => {
  shouldAutoScrollEditor.value = false
  Object.assign(newsForm.value, emptyForm())
  formRef.value?.clearValidate()
  dialogVisible.value = true
}

const getEditorScrollContainer = () => {
  return editorWrapperRef.value?.querySelector<HTMLElement>('.w-e-scroll')
}

const bindEditorAutoScroll = async () => {
  removeEditorListeners.value?.()
  editorMutationObserver?.disconnect()

  await nextTick()

  const scrollContainer = getEditorScrollContainer()
  const editable = editorWrapperRef.value?.querySelector<HTMLElement>('[data-slate-editor]')

  if (!scrollContainer || !editable) return

  const handleInput = () => {
    if (!shouldAutoScrollEditor.value) return

    requestAnimationFrame(() => {
      scrollContainer.scrollTop = scrollContainer.scrollHeight
    })
  }

  const enableAutoScroll = () => {
    shouldAutoScrollEditor.value = true
  }

  editable.addEventListener('compositionstart', enableAutoScroll)
  editable.addEventListener('beforeinput', enableAutoScroll)
  editable.addEventListener('input', handleInput)
  editable.addEventListener('keyup', handleInput)
  editable.addEventListener('paste', handleInput)
  editorMutationObserver = new MutationObserver(handleInput)
  editorMutationObserver.observe(editable, {
    childList: true,
    characterData: true,
    subtree: true,
  })

  removeEditorListeners.value = () => {
    editable.removeEventListener('compositionstart', enableAutoScroll)
    editable.removeEventListener('beforeinput', enableAutoScroll)
    editable.removeEventListener('input', handleInput)
    editable.removeEventListener('keyup', handleInput)
    editable.removeEventListener('paste', handleInput)
    editorMutationObserver?.disconnect()
    editorMutationObserver = null
  }
}

const handleEditorCreated = (editor: IDomEditor) => {
  editorRef.value = editor
  bindEditorAutoScroll()
}

const scrollEditorToBottom = async () => {
  if (!shouldAutoScrollEditor.value) return

  await nextTick()

  const scrollToBottom = () => {
    const scrollContainer = getEditorScrollContainer()
    if (!scrollContainer) return

    scrollContainer.scrollTop = scrollContainer.scrollHeight
  }

  requestAnimationFrame(scrollToBottom)
  setTimeout(scrollToBottom, 0)
}

const showRequestError = (error: any, fallback: string) => {
  ElMessage.error(error?.response?.data?.message || error?.message || fallback)
}

const submitNews = async () => {
  if (!formRef.value) return

  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    const result: any = await createNews(newsForm.value)

    if (result.code !== 200) {
      ElMessage.error(result.message || '新闻保存失败')
      return
    }

    dialogVisible.value = false
    ElMessage.success(result.message || '新闻已提交审核')
  } catch (error: any) {
    showRequestError(error, '新闻保存失败')
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="news-page">
    <div class="page-toolbar">
      <div>
        <h2>新闻管理</h2>
        <p>维护标题、栏目、审核信息和正文内容。</p>
      </div>

      <el-button type="primary" @click="openAddDialog">添加新闻</el-button>
    </div>

    <el-dialog
        v-model="dialogVisible"
        title="添加新闻"
        width="1040px"
        destroy-on-close
        align-center
        class="news-dialog"
    >
      <el-form ref="formRef" :model="newsForm" :rules="rules" label-width="86px" class="news-form">
        <div class="form-basic">
          <el-form-item label="新闻标题" prop="title">
            <el-input v-model="newsForm.title" maxlength="80" show-word-limit placeholder="请输入新闻标题"/>
          </el-form-item>

          <el-row :gutter="18">
            <el-col :span="8">
              <el-form-item label="栏目" prop="category">
                <el-select v-model="newsForm.category" placeholder="请选择栏目" style="width: 100%">
                  <el-option
                      v-for="item in categoryOptions"
                      :key="item"
                      :label="item"
                      :value="item"
                  />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="18">
            <el-col :span="12">
              <el-form-item label="供稿">
                <el-input v-model="newsForm.supplier" placeholder="请输入供稿人"/>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="审稿">
                <el-input v-model="newsForm.reviewer" placeholder="请输入审稿人"/>
              </el-form-item>
            </el-col>
          </el-row>
        </div>

        <el-form-item label="正文内容" prop="content" class="content-form-item">
          <div ref="editorWrapperRef" class="editor-wrapper">
            <Toolbar
                :editor="editorRef"
                :default-config="toolbarConfig"
                mode="default"
                class="editor-toolbar"
            />
            <Editor
                v-model="newsForm.content"
                :default-config="editorConfig"
                mode="default"
                class="editor-content"
                @on-created="handleEditorCreated"
                @on-change="scrollEditorToBottom"
                @keyup="scrollEditorToBottom"
                @input="scrollEditorToBottom"
            />
          </div>
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <div class="editor-footer-border"></div>
          <div class="dialog-footer-actions">
            <el-button @click="dialogVisible = false">取消</el-button>
            <el-button type="primary" :loading="submitting" @click="submitNews">
              保存新闻
            </el-button>
          </div>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.news-page {
  height: 100%;
  padding: 22px;
  background: #f5f7fb;
  box-sizing: border-box;
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