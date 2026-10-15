<template>
  <div class="article-detail">
    <!-- 导航栏 -->
    <nav class="navbar">
      <div class="container">
        <router-link to="/" class="logo">个人博客</router-link>
        <div class="nav-links">
          <router-link to="/">首页</router-link>
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

    <!-- 文章内容 -->
    <div class="container article-content">
      <div v-if="loading" class="loading">加载中...</div>
      <div v-else-if="!article" class="empty">文章不存在</div>
      <div v-else class="article-main">
        <!-- 文章头部 -->
        <div class="article-header">
          <div class="article-title">{{ article.title }}</div>
          <div class="article-meta">
            <div class="meta-left">
              <span class="category">{{ article.categoryName }}</span>
              <span class="author">{{ article.authorName }}</span>
              <span class="time">{{ formatTime(article.publishedAt) }}</span>
            </div>
            <div class="meta-right">
              <el-icon><View /></el-icon>
              <span class="views">{{ article.viewCount }}</span>
            </div>
          </div>
        </div>

        <!-- 文章正文 -->
        <div class="article-body">
          {{ article.content }}
        </div>

        <!-- 文章底部 -->
        <div class="article-footer">
          <div class="tags">
            <el-tag v-for="tag in article.tags" :key="tag" size="small">{{ tag }}</el-tag>
          </div>
        </div>

        <!-- 评论区 -->
        <div class="comment-section">
          <div class="comment-header">
            <h3>评论 ({{ comments.length }})</h3>
          </div>

          <!-- 评论表单 -->
          <div class="comment-form">
            <div v-if="isAuthenticated">
              <el-input v-model="newComment" type="textarea" placeholder="写下你的评论..." :rows="4" />
              <div class="form-actions">
                <el-button type="primary" @click="submitComment" :loading="submitting">
                  发表评论
                </el-button>
              </div>
            </div>
            <div v-else class="login-prompt">
              请<router-link to="/login">登录</router-link>后发表评论
            </div>

            <!-- 评论列表 -->
            <div class="comment-list">
              <div v-if="comments.length === 0" class="no-comments">
                还没有评论，快来抢沙发吧！
              </div>
              <div v-else>
                <div v-for="comment in comments" :key="comment.id" class="comment-item">
                  <div class="comment-avatar">
                    <el-avatar :size="40" :src="comment.user.avatar" />
                  </div>
                  <div class="comment-content">
                    <div class="comment-header">
                      <span class="comment-user">{{ comment.user.nickname }}</span>
                      <span class="comment-time">{{ formatTime(comment.createdAt) }}</span>
                    </div>
                    <div class="comment-text">{{ comment.content }}</div>
                    <div class="comment-actions">
                      <el-button type="text" size="small" @click="replyComment(comment)">回复</el-button>
                      <el-button v-if="comment.user.id === currentUser.id" type="text" size="small" @click="deleteComment(comment)">删除</el-button>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { useStore } from 'vuex'
import { ElMessage } from 'element-plus'
import { ArrowDown, View } from '@element-plus/icons-vue'
import { getArticle } from '@/api/article'
import { getComments, createComment } from '@/api/comment'

const route = useRoute()
const store = useStore()

// 数据
const article = ref(null)
const comments = ref([])
const newComment = ref('')
const loading = ref(false)
const submitting = ref(false)

// 计算属性
const isAuthenticated = computed(() => store.getters.isAuthenticated)
const currentUser = computed(() => store.getters.currentUser)
const isAdmin = computed(() => store.getters.isAdmin)

// 方法
const loadArticle = async () => {
  try {
    loading.value = true
    const articleId = route.params.id

    const response = await getArticle(articleId)
    article.value = response

    // 加载评论
    await loadComments()
  } catch (error) {
    console.error('加载文章失败:', error)
    ElMessage.error('加载文章失败')
  } finally {
    loading.value = false
  }
}

const loadComments = async () => {
  try {
    const articleId = route.params.id
    const response = await getComments(articleId)
    comments.value = response.content || []
  } catch (error) {
    console.error('加载评论失败:', error)
  }
}

const submitComment = async () => {
  if (!newComment.value.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }

  try {
    submitting.value = true
    await createComment(article.value.id, { content: newComment.value })
    await loadComments()
    newComment.value = ''
    ElMessage.success('评论发表成功')
  } catch (error) {
    console.error('提交评论失败:', error)
    ElMessage.error('提交评论失败')
  } finally {
    submitting.value = false
  }
}

const replyComment = (comment) => {
  ElMessage.info('回复功能开发中...')
}

const deleteComment = (comment) => {
  ElMessage.info('删除功能开发中...')
}

const formatTime = (time) => {
  return new Date(time).toLocaleString()
}

const handleLogout = () => {
  store.dispatch('logout')
}

// 生命周期钩子
onMounted(() => {
  loadArticle()
})
</script>

<style scoped>
.article-detail {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #f0f2f5;
}

.navbar {
  background-color: #fff;
  box-shadow: 0 2px 12px rgba(0,0,0,0.08);
  padding: 0.8rem 0;
  position: sticky;
  top: 0;
  z-index: 1000;
  border-bottom: 3px solid #667eea;
}

.container {
  max-width: 900px;
  margin: 0 auto;
  padding: 0 1rem;
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

.el-dropdown-link {
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: #666;
  transition: all 0.3s;
  padding: 0.5rem 1rem;
  border-radius: 6px;
}

.el-dropdown-link:hover {
  color: #667eea;
  background: rgba(102, 126, 234, 0.1);
}

.article-content {
  padding: 2.5rem 0;
  flex: 1;
}

.loading {
  text-align: center;
  padding: 4rem 2rem;
  color: #999;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.08);
  font-size: 1.1rem;
}

.empty {
  text-align: center;
  padding: 4rem 2rem;
  color: #666;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.08);
  font-size: 1.1rem;
}

.article-main {
  background-color: #fff;
  border-radius: 16px;
  padding: 3rem;
  box-shadow: 0 2px 20px rgba(0,0,0,0.08);
  transition: box-shadow 0.3s ease;
}

.article-main:hover {
  box-shadow: 0 8px 40px rgba(102, 126, 234, 0.15);
}

.article-header {
  margin-bottom: 2.5rem;
  padding-bottom: 2rem;
  border-bottom: 1px solid #eee;
}

.article-title {
  font-size: 2.2rem;
  font-weight: 700;
  margin-bottom: 1.5rem;
  color: #1f2d3d;
  line-height: 1.4;
  letter-spacing: 0.5px;
}

.article-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: #999;
  font-size: 0.9rem;
  flex-wrap: wrap;
  gap: 1rem;
}

.meta-left {
  display: flex;
  align-items: center;
  gap: 1.2rem;
  flex-wrap: wrap;
}

.category {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
  color: #667eea;
  padding: 0.4rem 1rem;
  border-radius: 20px;
  font-weight: 500;
  font-size: 0.85rem;
}

.author {
  color: #666;
  font-weight: 500;
}

.time {
  color: #999;
}

.meta-right {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: #999;
  background: #f8f9fa;
  padding: 0.4rem 0.8rem;
  border-radius: 6px;
}

.meta-right .el-icon {
  font-size: 1rem;
}

.article-body {
  line-height: 2;
  color: #333;
  margin-bottom: 2.5rem;
  font-size: 1.05rem;
  white-space: pre-wrap;
}

.article-footer {
  padding-top: 2rem;
  border-top: 1px solid #eee;
}

.tags {
  display: flex;
  gap: 0.6rem;
  flex-wrap: wrap;
}

.tags :deep(.el-tag) {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
  color: #667eea;
  border: none;
  border-radius: 12px;
  padding: 0.3rem 0.8rem;
  transition: all 0.3s ease;
}

.tags :deep(.el-tag:hover) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.2);
}

.comment-section {
  margin-top: 3rem;
  padding-top: 2.5rem;
  border-top: 1px solid #eee;
}

.comment-header {
  margin-bottom: 1.5rem;
}

.comment-header h3 {
  font-size: 1.3rem;
  color: #1f2d3d;
  margin: 0;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.comment-header h3::before {
  content: '';
  width: 4px;
  height: 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 2px;
}

.comment-form {
  margin-bottom: 2.5rem;
}

.comment-form :deep(.el-textarea__inner) {
  border-radius: 12px;
  padding: 1rem;
  font-size: 0.95rem;
  border-color: #e8e8e8;
  transition: all 0.3s ease;
  resize: none;
}

.comment-form :deep(.el-textarea__inner:focus) {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.form-actions {
  margin-top: 1rem;
  text-align: right;
}

.form-actions :deep(.el-button--primary) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  border-radius: 8px;
  padding: 0.8rem 2rem;
  font-weight: 500;
  letter-spacing: 1px;
  transition: all 0.3s ease;
}

.form-actions :deep(.el-button--primary:hover) {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.4);
}

.comment-list {
  margin-top: 2rem;
}

.no-comments {
  text-align: center;
  padding: 3rem 2rem;
  color: #999;
  background: #f8f9fa;
  border-radius: 12px;
  font-size: 0.95rem;
}

.comment-item {
  display: flex;
  gap: 1rem;
  margin-bottom: 1.5rem;
  padding-bottom: 1.5rem;
  border-bottom: 1px solid #f0f0f0;
  transition: all 0.3s ease;
  border-radius: 12px;
  padding: 1.2rem;
}

.comment-item:hover {
  background: #f8f9fa;
  margin: 0 -1.2rem 1.5rem;
  padding-bottom: 1.5rem;
  padding-left: 1.2rem;
  padding-right: 1.2rem;
}

.comment-item:last-child {
  margin-bottom: 0;
  padding-bottom: 0;
  border-bottom: none;
}

.comment-item:last-child:hover {
  margin-bottom: 0;
  padding-bottom: 1.2rem;
}

.comment-avatar {
  flex-shrink: 0;
}

.comment-avatar :deep(.el-avatar) {
  border: 2px solid #667eea;
  transition: transform 0.3s ease;
}

.comment-avatar :deep(.el-avatar):hover {
  transform: scale(1.1);
}

.comment-content {
  flex: 1;
  min-width: 0;
}

.comment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.6rem;
}

.comment-user {
  font-weight: 600;
  color: #1f2d3d;
  font-size: 0.95rem;
}

.comment-time {
  color: #999;
  font-size: 0.8rem;
}

.comment-text {
  color: #555;
  line-height: 1.7;
  margin-bottom: 0.8rem;
  font-size: 0.95rem;
}

.comment-actions {
  display: flex;
  gap: 1rem;
}

.comment-actions :deep(.el-button--text) {
  color: #999;
  transition: all 0.3s ease;
  font-size: 0.85rem;
  padding: 0.3rem 0.6rem;
  border-radius: 4px;
}

.comment-actions :deep(.el-button--text:hover) {
  color: #667eea;
  background: rgba(102, 126, 234, 0.1);
}

.login-prompt {
  text-align: center;
  padding: 2rem;
  color: #666;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.05) 0%, rgba(118, 75, 162, 0.05) 100%);
  border-radius: 12px;
  margin-bottom: 2rem;
  font-size: 0.95rem;
}

.login-prompt a {
  color: #667eea;
  text-decoration: none;
  font-weight: 500;
  transition: all 0.3s ease;
}

.login-prompt a:hover {
  color: #764ba2;
  text-decoration: underline;
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
  .article-main {
    padding: 2rem;
  }

  .article-title {
    font-size: 1.8rem;
  }

  .nav-links {
    gap: 0.5rem;
  }

  .nav-links a {
    padding: 0.4rem 0.6rem;
    font-size: 0.9rem;
  }
}

@media (max-width: 768px) {
  .article-title {
    font-size: 1.5rem;
  }

  .article-meta {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
  }

  .meta-left {
    flex-wrap: wrap;
    gap: 0.8rem;
  }

  .article-main {
    padding: 1.5rem;
    border-radius: 12px;
  }

  .article-body {
    font-size: 1rem;
  }

  .comment-item {
    padding: 1rem;
  }

  .comment-item:hover {
    margin: 0 -1rem 1.5rem;
    padding-left: 1rem;
    padding-right: 1rem;
  }

  .navbar .container {
    flex-direction: column;
    gap: 1rem;
    padding: 1rem;
  }

  .nav-links {
    flex-wrap: wrap;
    justify-content: center;
  }
}

@media (max-width: 480px) {
  .article-content {
    padding: 1.5rem 0;
  }

  .article-main {
    padding: 1.2rem;
  }

  .article-title {
    font-size: 1.3rem;
  }

  .article-header {
    margin-bottom: 1.5rem;
    padding-bottom: 1.5rem;
  }

  .article-body {
    line-height: 1.8;
  }

  .comment-section {
    margin-top: 2rem;
    padding-top: 1.5rem;
  }
}
</style>