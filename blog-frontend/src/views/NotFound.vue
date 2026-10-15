<template>
  <div class="not-found">
    <h1>404 - 页面不存在</h1>
    <p>抱歉，您访问的页面不存在或已被移除。</p>
    <router-link to="/">返回首页</router-link>
  </div>
</template>

<script setup>
// 404页面组件
</script>

<style scoped>
.not-found {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100vh;
  text-align: center;
}

.not-found h1 {
  font-size: 2.5rem;
  color: #f56c6c;
  margin-bottom: 1rem;
}

.not-found p {
  font-size: 1.2rem;
  color: #666;
  margin-bottom: 2rem;
}

.not-found a {
  color: #409eff;
  text-decoration: none;
  font-size: 1.1rem;
}

.not-found a:hover {
  text-decoration: underline;
}
</style>