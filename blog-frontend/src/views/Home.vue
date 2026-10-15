<template>
  <div class="home">
    <!-- 导航栏 -->
    <nav class="navbar">
      <div class="container">
        <router-link to="/" class="logo">个人博客</router-link>
        <div class="nav-links">
          <router-link to="/">首页</router-link>
          <a v-for="category in categories" :key="category.id"
             @click="selectCategory(category.id)">
            {{ category.name }}
          </a>
          <router-link v-if="!isAuthenticated" to="/login">登录</router-link>
          <router-link v-if="!isAuthenticated" to="/register">注册</router-link>
          <el-dropdown v-else>
            <span class="el-dropdown-link">
              {{ currentUser.nickname || currentUser.username }}
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item>
                  <router-link to="/dashboard">个人中心</router-link>
                </el-dropdown-item>
                <el-dropdown-item v-if="isAdmin">
                  <router-link to="/admin">后台管理</router-link>
                </el-dropdown-item>
                <el-dropdown-item divided @click="handleLogout">
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </nav>

    <!-- 主要内容 -->
    <div class="container main-content">
      <div class="articles">
        <div v-if="loading" class="loading">
          加载中...
        </div>
        <div v-else-if="articles.length === 0" class="empty">
          暂无文章
        </div>
        <div v-else>
          <div v-for="article in articles" :key="article.id" class="article-card">
            <router-link :to="`/article/${article.id}`" class="article-link">
              <div class="article-cover" v-if="article.coverImage">
                <img :src="article.coverImage" alt="封面">
              </div>
              <div class="article-info">
                <h3 class="article-title">{{ article.title }}</h3>
                <p class="article-summary">{{ article.summary }}</p>
                <div class="article-meta">
                  <span class="category">{{ article.categoryName }}</span>
                  <span class="author">{{ article.authorName }}</span>
                  <span class="views">
                    <el-icon><View /></el-icon>
                    {{ article.views }}
                  </span>
                  <span class="time">{{ formatTime(article.publishedAt) }}</span>
                </div>
              </div>
            </router-link>
          </div>
        </div>

        <!-- 分页 -->
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

      <!-- 侧边栏 -->
      <div class="sidebar">
        <div class="sidebar-section">
          <h3>热门文章</h3>
          <ul class="hot-articles">
            <li v-for="article in hotArticles" :key="article.id">
              <router-link :to="`/article/${article.id}`">
                {{ article.title }}
              </router-link>
            </li>
          </ul>
        </div>

        <div class="sidebar-section">
          <h3>分类</h3>
          <ul class="categories">
            <li>
              <a @click="selectCategory(null)" :class="{ active: !selectedCategory }">
                全部
              </a>
            </li>
            <li v-for="category in categories" :key="category.id">
              <a @click="selectCategory(category.id)"
                 :class="{ active: selectedCategory === category.id }">
                {{ category.name }}
              </a>
            </li>
          </ul>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useStore } from 'vuex'
import { useRouter } from 'vue-router'
import { getArticles } from '@/api/article'
import { getCategories } from '@/api/category'
import { ElMessage } from 'element-plus'
import { ArrowDown, View } from '@element-plus/icons-vue'

const store = useStore()
const router = useRouter()

// 数据
const articles = ref([])
const hotArticles = ref([])
const selectedCategory = ref(null)
const loading = ref(false)

// 分页
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 计算属性
const isAuthenticated = computed(() => store.getters.isAuthenticated)
const currentUser = computed(() => store.getters.currentUser)
const isAdmin = computed(() => store.getters.isAdmin)
const categories = computed(() => store.getters.getCategories)

// 方法
// 修改 loadArticles 方法
const loadArticles = async () => {
  try {
    loading.value = true
    const params = {
      page: currentPage.value - 1,
      size: pageSize.value,
      categoryId: selectedCategory.value
    }

    // 使用简化的方式调用 API
    try {
      const response = await getArticles(params)
      articles.value = response.content || []
      total.value = response.totalElements || 0
    } catch (err) {
      console.error('API 调用失败:', err)
      // 出错时使用空数组，防止渲染问题
      articles.value = []
      total.value = 0
    } finally {
      loading.value = false
    }
    
  } catch (error) {
    console.error('加载文章失败:', error)
    ElMessage.error('加载文章失败')
    loading.value = false
  }
}

// 修改 loadCategories 方法
const loadCategories = async () => {
  try {
    const categories = await getCategories()
    store.dispatch('setCategories', categories)
  } catch (error) {
    console.error('加载分类失败:', error)
    ElMessage.error('加载分类失败')
    // 出错时设置一个默认分类，防止界面崩溃
    store.dispatch('setCategories', [{id: 1, name: '默认分类'}])
  }
}

const loadHotArticles = async () => {
  // 模拟热门文章
  hotArticles.value = [
    { id: 1, title: 'Spring Boot入门教程' },
    { id: 2, title: 'Vue 3新特性详解' },
    { id: 3, title: 'MySQL性能优化技巧' }
  ]
}

const selectCategory = (categoryId) => {
  selectedCategory.value = categoryId
  currentPage.value = 1
  loadArticles()
}

const handlePageChange = (page) => {
  currentPage.value = page
  loadArticles()
}

const handleLogout = () => {
  store.dispatch('logout')
  ElMessage.success('已退出登录')
  router.push('/')
}

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')}`
}

// 生命周期
onMounted(() => {
  loadCategories()
  loadArticles()
  loadHotArticles()
})
</script>

<style scoped>
.home {
  min-height: 100vh;
  background-color: #f0f2f5;
}

.navbar {
  background: white;
  box-shadow: 0 2px 12px rgba(0,0,0,0.08);
  padding: 0.8rem 0;
  position: sticky;
  top: 0;
  z-index: 1000;
  border-bottom: 3px solid #667eea;
}

.navbar .container {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.logo {
  font-size: 1.5rem;
  font-weight: bold;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  text-decoration: none;
  transition: all 0.3s ease;
}

.logo:hover {
  transform: scale(1.05);
}

.nav-links {
  display: flex;
  gap: 1.5rem;
  align-items: center;
}

.nav-links a {
  color: #666;
  text-decoration: none;
  cursor: pointer;
  transition: all 0.3s;
  font-size: 0.95rem;
  padding: 0.5rem 1rem;
  border-radius: 6px;
  position: relative;
}

.nav-links a::before {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  width: 0;
  height: 2px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  transition: all 0.3s ease;
  transform: translateX(-50%);
}

.nav-links a:hover {
  color: #667eea;
  background: rgba(102, 126, 234, 0.1);
}

.nav-links a:hover::before {
  width: 60%;
}

.nav-links .router-link-active {
  color: #667eea;
  font-weight: 600;
  background: rgba(102, 126, 234, 0.1);
}

.main-content {
  display: flex;
  gap: 2rem;
  padding: 2rem 1rem;
  min-height: calc(100vh - 60px);
}

.articles {
  flex: 1;
  min-height: 400px;
}

.loading, .empty {
  text-align: center;
  padding: 3rem;
  color: #999;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.08);
  font-size: 1rem;
}

.article-card {
  background: white;
  border-radius: 12px;
  margin-bottom: 1.5rem;
  box-shadow: 0 2px 12px rgba(0,0,0,0.08);
  overflow: hidden;
  transition: all 0.3s ease;
  border: 1px solid #eee;
}

.article-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 12px 40px rgba(102, 126, 234, 0.2);
  border-color: rgba(102, 126, 234, 0.3);
}

.article-link {
  display: block;
  text-decoration: none;
  color: inherit;
  padding: 1.5rem;
}

.article-cover img {
  width: 100%;
  height: 220px;
  object-fit: cover;
  border-radius: 8px;
  margin-bottom: 1.2rem;
  transition: transform 0.3s ease;
}

.article-card:hover .article-cover img {
  transform: scale(1.02);
}

.article-title {
  font-size: 1.3rem;
  margin: 0 0 0.8rem 0;
  color: #1f2d3d;
  line-height: 1.4;
  font-weight: 600;
  transition: color 0.3s;
}

.article-card:hover .article-title {
  color: #667eea;
}

.article-summary {
  color: #666;
  margin-bottom: 1rem;
  line-height: 1.7;
  font-size: 0.95rem;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.article-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 1rem;
  color: #999;
  font-size: 0.85rem;
  align-items: center;
}

.article-meta .category {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
  color: #667eea;
  padding: 0.3rem 0.8rem;
  border-radius: 20px;
  font-weight: 500;
}

.article-meta .author {
  font-weight: 500;
  color: #666;
}

.article-meta .views {
  display: flex;
  align-items: center;
  gap: 0.3rem;
}

.article-meta .time {
  color: #999;
}

.sidebar {
  width: 300px;
}

.sidebar-section {
  background: white;
  border-radius: 12px;
  padding: 1.5rem;
  margin-bottom: 1.5rem;
  box-shadow: 0 2px 12px rgba(0,0,0,0.08);
  border: 1px solid #eee;
  transition: all 0.3s ease;
}

.sidebar-section:hover {
  box-shadow: 0 8px 30px rgba(102, 126, 234, 0.15);
  transform: translateY(-2px);
}

.sidebar-section h3 {
  margin: 0 0 1.2rem 0;
  color: #1f2d3d;
  font-size: 1.1rem;
  font-weight: 600;
  padding-bottom: 0.8rem;
  border-bottom: 2px solid;
  border-image: linear-gradient(135deg, #667eea 0%, #764ba2 100%) 1;
}

.hot-articles li,
.categories li {
  margin-bottom: 0.3rem;
}

.hot-articles a,
.categories a {
  color: #666;
  text-decoration: none;
  cursor: pointer;
  display: block;
  padding: 0.5rem 0.8rem;
  border-radius: 6px;
  transition: all 0.3s;
  font-size: 0.9rem;
  position: relative;
  overflow: hidden;
}

.hot-articles a::before,
.categories a::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  transition: height 0.3s ease;
}

.hot-articles a:hover,
.categories a:hover {
  color: #667eea;
  background: rgba(102, 126, 234, 0.08);
  padding-left: 1.2rem;
}

.hot-articles a:hover::before,
.categories a:hover::before {
  height: 60%;
}

.categories .active {
  color: #667eea;
  font-weight: 600;
  background: rgba(102, 126, 234, 0.08);
  padding-left: 1.2rem;
}

.categories .active::before {
  height: 60%;
}

.pagination {
  margin-top: 2rem;
  justify-content: center;
}

.pagination :deep(.el-pager li) {
  border-radius: 8px;
  transition: all 0.3s;
}

.pagination :deep(.el-pager li:hover) {
  color: #667eea;
}

.pagination :deep(.el-pager li.is-active) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.el-dropdown-link {
  cursor: pointer;
  color: #666;
  display: flex;
  align-items: center;
  gap: 0.3rem;
  transition: all 0.3s;
  padding: 0.5rem 1rem;
  border-radius: 6px;
}

.el-dropdown-link:hover {
  color: #667eea;
  background: rgba(102, 126, 234, 0.1);
}

:deep(.el-dropdown-menu__item) {
  border-radius: 4px;
  margin: 0.2rem 0.5rem;
}

:deep(.el-dropdown-menu__item:hover) {
  background: rgba(102, 126, 234, 0.1);
  color: #667eea;
}

@media (max-width: 992px) {
  .main-content {
    flex-direction: column;
  }

  .sidebar {
    width: 100%;
    display: flex;
    gap: 1rem;
    flex-wrap: wrap;
  }

  .sidebar-section {
    flex: 1;
    min-width: 280px;
    margin-bottom: 0;
  }

  .nav-links {
    gap: 0.5rem;
  }

  .nav-links a {
    padding: 0.4rem 0.6rem;
    font-size: 0.9rem;
  }
}

@media (max-width: 576px) {
  .navbar .container {
    flex-direction: column;
    gap: 1rem;
    padding: 1rem;
  }

  .nav-links {
    flex-wrap: wrap;
    justify-content: center;
  }

  .article-meta {
    font-size: 0.8rem;
    gap: 0.5rem;
  }

  .article-cover img {
    height: 160px;
  }

  .sidebar {
    flex-direction: column;
  }

  .sidebar-section {
    min-width: 100%;
  }
}
</style>