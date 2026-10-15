<template>
  <div class="admin-container">
    <nav class="navbar">
      <div class="container">
        <router-link to="/" class="logo">个人博客管理</router-link>
        <div class="nav-links">
          <router-link to="/">返回前台</router-link>
          <router-link to="/dashboard">个人中心</router-link>
          <span class="user-info">
            <el-icon><User /></el-icon>
            {{ currentUser.nickname || currentUser.username }} (管理员)
          </span>
          <el-button type="text" @click="handleLogout">退出登录</el-button>
        </div>
      </div>
    </nav>

    <div class="container main-content">
      <div class="admin-header">
        <h2>后台管理</h2>
      </div>

      <div class="statistics-cards">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <el-icon class="stat-icon total"><Document /></el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.totalArticles || 0 }}</div>
              <div class="stat-label">文章总数</div>
            </div>
          </div>
        </el-card>
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <el-icon class="stat-icon published"><CircleCheck /></el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.publishedArticles || 0 }}</div>
              <div class="stat-label">已发布</div>
            </div>
          </div>
        </el-card>
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <el-icon class="stat-icon pending"><Clock /></el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.pendingArticles || 0 }}</div>
              <div class="stat-label">待审核</div>
            </div>
          </div>
        </el-card>
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <el-icon class="stat-icon draft"><EditPen /></el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.draftArticles || 0 }}</div>
              <div class="stat-label">草稿</div>
            </div>
          </div>
        </el-card>
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <el-icon class="stat-icon rejected"><CircleClose /></el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.rejectedArticles || 0 }}</div>
              <div class="stat-label">已拒绝</div>
            </div>
          </div>
        </el-card>
      </div>

      <el-card class="articles-card">
        <template #header>
          <div class="card-header">
            <span>文章管理</span>
            <el-button-group>
              <el-button
                :type="activeTab === 'pending' ? 'primary' : ''"
                @click="activeTab = 'pending'"
              >
                待审核 ({{ statistics.pendingArticles || 0 }})
              </el-button>
              <el-button
                :type="activeTab === 'all' ? 'primary' : ''"
                @click="activeTab = 'all'"
              >
                全部文章
              </el-button>
            </el-button-group>
          </div>
        </template>

        <div v-if="loading" class="loading">
          <el-icon class="is-loading"><Loading /></el-icon>
          加载中...
        </div>

        <div v-else-if="articles.length === 0" class="empty">
          <el-empty :description="activeTab === 'pending' ? '暂无待审核文章' : '暂无文章'">
            <el-button v-if="activeTab === 'all'" type="primary" @click="$router.push('/dashboard')">
              发布文章
            </el-button>
          </el-empty>
        </div>

        <div v-else class="article-list">
          <el-table :data="articles" style="width: 100%" stripe>
            <el-table-column prop="title" label="标题" min-width="200">
              <template #default="{ row }">
                <router-link :to="`/article/${row.id}`" class="article-title-link">
                  {{ row.title }}
                </router-link>
              </template>
            </el-table-column>
            <el-table-column label="作者" width="120">
              <template #default="{ row }">
                {{ row.author?.nickname || row.author?.username }}
              </template>
            </el-table-column>
            <el-table-column label="分类" width="100">
              <template #default="{ row }">
                <el-tag size="small">{{ row.category?.name || '-' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.status)" size="small">
                  {{ getStatusText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="浏览量" width="100" align="center">
              <template #default="{ row }">
                <span>{{ row.views || 0 }}</span>
              </template>
            </el-table-column>
            <el-table-column label="发布时间" width="160">
              <template #default="{ row }">
                {{ formatTime(row.createdAt) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200" fixed="right">
              <template #default="{ row }">
                <el-button-group>
                  <el-button type="primary" size="small" @click="viewArticle(row)">
                    <el-icon><View /></el-icon>
                  </el-button>
                  <el-button
                    v-if="row.status === 'PENDING'"
                    type="success"
                    size="small"
                    @click="reviewArticle(row, 'PUBLISHED')"
                  >
                    <el-icon><CircleCheck /></el-icon>
                  </el-button>
                  <el-button
                    v-if="row.status === 'PENDING'"
                    type="danger"
                    size="small"
                    @click="reviewArticle(row, 'REJECTED')"
                  >
                    <el-icon><CircleClose /></el-icon>
                  </el-button>
                  <el-button
                    type="warning"
                    size="small"
                    @click="handleDeleteArticle(row)"
                  >
                    <el-icon><Delete /></el-icon>
                  </el-button>
                </el-button-group>
              </template>
            </el-table-column>
          </el-table>

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
      </el-card>
    </div>

    <el-dialog v-model="reviewDialogVisible" title="审核文章" width="500px">
      <div class="review-content">
        <h3>{{ reviewingArticle?.title }}</h3>
        <p class="review-meta">
          作者：{{ reviewingArticle?.author?.nickname || reviewingArticle?.author?.username }}
        </p>
        <div class="article-summary">
          <strong>摘要：</strong>
          {{ reviewingArticle?.summary || '暂无摘要' }}
        </div>
        <el-input
          v-model="reviewComment"
          type="textarea"
          placeholder="请输入审核意见（可选）"
          :rows="3"
        />
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="reviewDialogVisible = false">取消</el-button>
          <el-button type="danger" :loading="reviewing" @click="confirmReview('REJECTED')">
            拒绝
          </el-button>
          <el-button type="success" :loading="reviewing" @click="confirmReview('PUBLISHED')">
            通过
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useStore } from 'vuex'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Document, CircleCheck, Clock, EditPen, CircleClose,
  View, Delete, User, Loading
} from '@element-plus/icons-vue'
import {
  getAllArticles, getPendingArticles, reviewArticle as reviewArticleApi,
  deleteArticle as deleteArticleApi, getArticleStatistics
} from '@/api/article'

const store = useStore()
const router = useRouter()

const activeTab = ref('pending')
const articles = ref([])
const statistics = reactive({
  totalArticles: 0,
  publishedArticles: 0,
  pendingArticles: 0,
  draftArticles: 0,
  rejectedArticles: 0
})
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const reviewDialogVisible = ref(false)
const reviewingArticle = ref(null)
const reviewing = ref(false)
const reviewComment = ref('')

const currentUser = computed(() => store.getters.currentUser)

const loadStatistics = async () => {
  try {
    const stats = await getArticleStatistics()
    Object.assign(statistics, stats)
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

const loadArticles = async () => {
  try {
    loading.value = true
    const params = {
      page: currentPage.value - 1,
      size: pageSize.value
    }

    let response
    if (activeTab.value === 'pending') {
      response = await getPendingArticles(params)
    } else {
      response = await getAllArticles(params)
    }

    articles.value = response.content || []
    total.value = response.totalElements || 0
  } catch (error) {
    console.error('加载文章失败:', error)
    ElMessage.error('加载文章失败')
  } finally {
    loading.value = false
  }
}

const viewArticle = (article) => {
  router.push(`/article/${article.id}`)
}

const reviewArticle = (article, status) => {
  reviewingArticle.value = article
  reviewComment.value = ''
  reviewingArticle.value.reviewStatus = status
  reviewDialogVisible.value = true
}

const confirmReview = async (status) => {
  try {
    reviewing.value = true
    await reviewArticleApi(reviewingArticle.value.id, {
      status: status,
      reviewComment: reviewComment.value
    })
    ElMessage.success(status === 'PUBLISHED' ? '审核通过' : '已拒绝')
    reviewDialogVisible.value = false
    loadArticles()
    loadStatistics()
  } catch (error) {
    console.error('审核失败:', error)
    ElMessage.error(error.response?.data?.message || '审核失败')
  } finally {
    reviewing.value = false
  }
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
    loadStatistics()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除文章失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

const handlePageChange = (page) => {
  currentPage.value = page
  loadArticles()
}

const getStatusType = (status) => {
  const typeMap = {
    DRAFT: 'info',
    PENDING: 'warning',
    PUBLISHED: 'success',
    REJECTED: 'danger'
  }
  return typeMap[status] || 'info'
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

watch(activeTab, () => {
  currentPage.value = 1
  loadArticles()
})

onMounted(() => {
  loadStatistics()
  loadArticles()
})
</script>

<style scoped>
.admin-container {
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
  font-size: 1.3rem;
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

.admin-header {
  margin-bottom: 2rem;
}

.admin-header h2 {
  margin: 0;
  color: #303133;
}

.statistics-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 1rem;
  margin-bottom: 2rem;
}

.stat-card {
  border-radius: 8px;
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.stat-icon {
  font-size: 2.5rem;
  padding: 0.5rem;
  border-radius: 8px;
}

.stat-icon.total {
  color: #409eff;
  background: #ecf5ff;
}

.stat-icon.published {
  color: #67c23a;
  background: #f0f9eb;
}

.stat-icon.pending {
  color: #e6a23c;
  background: #fdf6ec;
}

.stat-icon.draft {
  color: #909399;
  background: #f4f4f5;
}

.stat-icon.rejected {
  color: #f56c6c;
  background: #fef0f0;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 1.8rem;
  font-weight: bold;
  color: #303133;
  line-height: 1.2;
}

.stat-label {
  font-size: 0.85rem;
  color: #909399;
  margin-top: 0.25rem;
}

.articles-card {
  border-radius: 8px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.loading {
  text-align: center;
  padding: 3rem;
  color: #909399;
}

.empty {
  padding: 3rem;
}

.article-title-link {
  color: #303133;
  text-decoration: none;
  transition: color 0.3s;
}

.article-title-link:hover {
  color: #409eff;
}

.pagination {
  justify-content: center;
  margin-top: 2rem;
}

.review-content {
  padding: 0.5rem 0;
}

.review-content h3 {
  margin: 0 0 0.5rem 0;
  color: #303133;
}

.review-meta {
  color: #909399;
  font-size: 0.9rem;
  margin-bottom: 1rem;
}

.article-summary {
  background: #f5f7fa;
  padding: 1rem;
  border-radius: 4px;
  margin-bottom: 1rem;
  color: #606266;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 0.5rem;
}

@media (max-width: 768px) {
  .statistics-cards {
    grid-template-columns: repeat(2, 1fr);
  }

  .stat-card {
    min-width: 0;
  }

  .card-header {
    flex-direction: column;
    gap: 1rem;
    align-items: flex-start;
  }
}
</style>