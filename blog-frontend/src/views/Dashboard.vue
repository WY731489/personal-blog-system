<template>
  <div class="dashboard">
    <nav class="navbar">
      <div class="container">
        <router-link to="/" class="logo">个人博客</router-link>
        <div class="nav-links">
          <router-link to="/">首页</router-link>
          <span class="user-info">
            <el-icon><User /></el-icon>
            {{ currentUser.nickname || currentUser.username }}
          </span>
          <el-button type="text" @click="handleLogout">退出登录</el-button>
        </div>
      </div>
    </nav>

    <div class="container main-content">
      <div class="dashboard-header">
        <h2>个人中心</h2>
        <el-button type="primary" @click="showCreateDialog">
          <el-icon><Plus /></el-icon>
          发布文章
        </el-button>
      </div>

      <el-tabs v-model="activeTab" class="dashboard-tabs">
        <el-tab-pane label="我的文章" name="articles">
          <div class="articles-section">
            <div v-if="loading" class="loading">
              <el-icon class="is-loading"><Loading /></el-icon>
              加载中...
            </div>
            <div v-else-if="articles.length === 0" class="empty">
              <el-empty description="还没有发布过文章">
                <el-button type="primary" @click="showCreateDialog">立即发布</el-button>
              </el-empty>
            </div>
            <div v-else class="article-list">
              <div v-for="article in articles" :key="article.id" class="article-item">
                <div class="article-info">
                  <h3 class="article-title">{{ article.title }}</h3>
                  <div class="article-meta">
                    <span class="category">{{ article.categoryName }}</span>
                    <span class="status" :class="getStatusClass(article.status)">
                      {{ getStatusText(article.status) }}
                    </span>
                    <span class="time">{{ formatTime(article.createdAt) }}</span>
                  </div>
                </div>
                <div class="article-actions">
                  <el-button-group>
                    <el-button type="primary" size="small" @click="viewArticle(article)">
                      <el-icon><View /></el-icon>
                    </el-button>
                    <el-button type="warning" size="small" @click="editArticle(article)">
                      <el-icon><Edit /></el-icon>
                    </el-button>
                    <el-button type="danger" size="small" @click="handleDeleteArticle(article)">
                      <el-icon><Delete /></el-icon>
                    </el-button>
                  </el-button-group>
                </div>
              </div>

              <el-pagination
                v-if="total > pageSize"
                v-model:current-page="currentPage"
                :page-size="pageSize"
                :total="total"
                @current-change="handlePageChange"
                layout="prev, pager, next"
                class="pagination"
              />
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="个人信息" name="profile">
          <div class="profile-section">
            <el-card class="profile-card">
              <template #header>
                <div class="card-header">
                  <span>个人信息</span>
                </div>
              </template>
              <div class="profile-info">
                <div class="info-item">
                  <label>用户名：</label>
                  <span>{{ currentUser.username }}</span>
                </div>
                <div class="info-item">
                  <label>昵称：</label>
                  <span>{{ currentUser.nickname || '-' }}</span>
                </div>
                <div class="info-item">
                  <label>邮箱：</label>
                  <span>{{ currentUser.email || '-' }}</span>
                </div>
                <div class="info-item">
                  <label>角色：</label>
                  <el-tag>{{ currentUser.role === 'ROLE_ADMIN' ? '管理员' : '普通用户' }}</el-tag>
                </div>
              </div>
            </el-card>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑文章' : '发布文章'"
      width="70%"
      destroy-on-close
    >
      <el-form
        ref="formRef"
        :model="articleForm"
        :rules="formRules"
        label-width="80px"
        class="article-form"
      >
        <el-form-item label="标题" prop="title">
          <el-input
            v-model="articleForm.title"
            placeholder="请输入文章标题"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="分类" prop="categoryId">
          <el-select
            v-model="articleForm.categoryId"
            placeholder="请选择分类"
            style="width: 100%"
          >
            <el-option
              v-for="category in categories"
              :key="category.id"
              :label="category.name"
              :value="category.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="摘要" prop="summary">
          <el-input
            v-model="articleForm.summary"
            type="textarea"
            placeholder="请输入文章摘要（可选）"
            :rows="3"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="封面图" prop="coverImage">
          <el-input
            v-model="articleForm.coverImage"
            placeholder="请输入封面图片 URL（可选）"
          />
        </el-form-item>

        <el-form-item label="内容" prop="content">
          <el-input
            v-model="articleForm.content"
            type="textarea"
            placeholder="请输入文章内容"
            :rows="15"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="submitting" @click="submitArticle">
            {{ isEdit ? '保存修改' : '发布' }}
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useStore } from 'vuex'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus, Edit, Delete, View, User, Loading
} from '@element-plus/icons-vue'
import { getMyArticles, createArticle, updateArticle, deleteArticle as deleteArticleApi } from '@/api/article'
import { getCategories } from '@/api/category'

const store = useStore()
const router = useRouter()

const activeTab = ref('articles')
const articles = ref([])
const categories = ref([])
const loading = ref(false)
const submitting = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const dialogVisible = ref(false)
const isEdit = ref(false)
const editingArticleId = ref(null)
const formRef = ref(null)

const articleForm = reactive({
  title: '',
  content: '',
  summary: '',
  coverImage: '',
  categoryId: null
})

const formRules = {
  title: [
    { required: true, message: '请输入文章标题', trigger: 'blur' },
    { min: 2, max: 200, message: '标题长度在 2 到 200 个字符', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '请输入文章内容', trigger: 'blur' }
  ]
}

const currentUser = computed(() => store.getters.currentUser)

const loadCategories = async () => {
  try {
    categories.value = await getCategories()
  } catch (error) {
    console.error('加载分类失败:', error)
  }
}

const loadArticles = async () => {
  try {
    loading.value = true
    const response = await getMyArticles({
      page: currentPage.value - 1,
      size: pageSize.value
    })
    articles.value = response.content || []
    total.value = response.totalElements || 0
  } catch (error) {
    console.error('加载文章失败:', error)
    ElMessage.error('加载文章失败')
  } finally {
    loading.value = false
  }
}

const showCreateDialog = () => {
  isEdit.value = false
  editingArticleId.value = null
  resetForm()
  dialogVisible.value = true
}

const viewArticle = (article) => {
  router.push(`/article/${article.id}`)
}

const editArticle = (article) => {
  isEdit.value = true
  editingArticleId.value = article.id
  articleForm.title = article.title
  articleForm.content = article.content
  articleForm.summary = article.summary || ''
  articleForm.coverImage = article.coverImage || ''
  articleForm.categoryId = article.categoryId
  dialogVisible.value = true
}

const handleDeleteArticle = async (article) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除文章《${article.title}》吗？`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await deleteArticleApi(article.id)
    ElMessage.success('删除成功')
    loadArticles()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除文章失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

const submitArticle = async () => {
  try {
    await formRef.value.validate()
    submitting.value = true

    const data = {
      title: articleForm.title,
      content: articleForm.content,
      summary: articleForm.summary,
      coverImage: articleForm.coverImage,
      categoryId: articleForm.categoryId
    }

    if (isEdit.value) {
      await updateArticle(editingArticleId.value, data)
      ElMessage.success('修改成功，文章已重新提交审核')
    } else {
      await createArticle(data)
      ElMessage.success('发布成功！请等待管理员审核，审核通过后将在前台展示')
    }

    dialogVisible.value = false
    loadArticles()
  } catch (error) {
    console.error('提交文章失败:', error)
    console.error('错误响应:', error.response)
    if (error.response?.data?.message) {
      ElMessage.error('错误: ' + error.response.data.message)
    } else if (error.response?.status) {
      ElMessage.error('HTTP错误: ' + error.response.status + ' - ' + (error.response.statusText || ''))
    } else if (error.message) {
      ElMessage.error('错误: ' + error.message)
    } else {
      ElMessage.error(isEdit.value ? '修改失败' : '发布失败')
    }
  } finally {
    submitting.value = false
  }
}

const resetForm = () => {
  articleForm.title = ''
  articleForm.content = ''
  articleForm.summary = ''
  articleForm.coverImage = ''
  articleForm.categoryId = null
}

const handlePageChange = (page) => {
  currentPage.value = page
  loadArticles()
}

const getStatusClass = (status) => {
  const classMap = {
    DRAFT: 'status-draft',
    PENDING: 'status-pending',
    PUBLISHED: 'status-published',
    REJECTED: 'status-rejected'
  }
  return classMap[status] || ''
}

const getStatusText = (status) => {
  const textMap = {
    DRAFT: '草稿',
    PENDING: '待审核',
    PUBLISHED: '已发布',
    REJECTED: '已拒绝'
  }
  return textMap[status] || status
}

const formatTime = (time) => {
  if (!time) return '-'
  const date = new Date(time)
  return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')} ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
}

const handleLogout = () => {
  store.dispatch('logout')
  ElMessage.success('已退出登录')
  router.push('/')
}

onMounted(() => {
  loadCategories()
  loadArticles()
})
</script>

<style scoped>
.dashboard {
  min-height: 100vh;
  background-color: #f5f7fa;
}

.navbar {
  background: white;
  box-shadow: 0 2px 12px rgba(0,0,0,.1);
  padding: 0.8rem 0;
  position: sticky;
  top: 0;
  z-index: 1000;
}

.navbar .container {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.logo {
  font-size: 1.5rem;
  font-weight: bold;
  color: #1890ff;
  text-decoration: none;
}

.nav-links {
  display: flex;
  gap: 1.5rem;
  align-items: center;
}

.nav-links a {
  color: #666;
  text-decoration: none;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: #666;
}

.main-content {
  padding: 2rem 1rem;
}

.dashboard-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
}

.dashboard-header h2 {
  margin: 0;
  color: #303133;
}

.dashboard-tabs {
  background: white;
  border-radius: 8px;
  padding: 1rem 2rem;
  box-shadow: 0 2px 12px rgba(0,0,0,.1);
}

.loading {
  text-align: center;
  padding: 3rem;
  color: #909399;
}

.empty {
  padding: 3rem;
}

.article-list {
  min-height: 300px;
}

.article-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.2rem;
  border-bottom: 1px solid #ebeef5;
  transition: background-color 0.3s;
}

.article-item:hover {
  background-color: #f5f7fa;
}

.article-item:last-child {
  border-bottom: none;
}

.article-title {
  margin: 0 0 0.5rem 0;
  font-size: 1.1rem;
  color: #303133;
}

.article-meta {
  display: flex;
  gap: 1rem;
  font-size: 0.85rem;
  color: #909399;
}

.category {
  color: #409eff;
}

.status {
  padding: 0.15rem 0.5rem;
  border-radius: 4px;
  font-size: 0.8rem;
}

.status-draft {
  background: #fdf6ec;
  color: #e6a23c;
}

.status-pending {
  background: #ecf5ff;
  color: #409eff;
}

.status-published {
  background: #f0f9eb;
  color: #67c23a;
}

.status-rejected {
  background: #fef0f0;
  color: #f56c6c;
}

.pagination {
  justify-content: center;
  margin-top: 2rem;
}

.profile-section {
  max-width: 600px;
}

.profile-card {
  margin-bottom: 1rem;
}

.card-header {
  font-weight: 600;
}

.profile-info .info-item {
  display: flex;
  align-items: center;
  padding: 1rem 0;
  border-bottom: 1px solid #ebeef5;
}

.profile-info .info-item:last-child {
  border-bottom: none;
}

.profile-info label {
  width: 80px;
  color: #909399;
  flex-shrink: 0;
}

.article-form {
  padding: 1rem 0;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
}

@media (max-width: 768px) {
  .article-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
  }

  .article-meta {
    flex-wrap: wrap;
    gap: 0.5rem;
  }

  .dashboard-header {
    flex-direction: column;
    gap: 1rem;
    align-items: flex-start;
  }
}
</style>
