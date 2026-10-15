<template>
  <router-view />
</template>

<script setup>
</script>

<style>
:root {
  --primary-color: #667eea;
  --primary-color-dark: #764ba2;
  --primary-gradient: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  --background-color: #f0f2f5;
  --card-shadow: 0 2px 12px rgba(0,0,0,0.08);
  --card-shadow-hover: 0 12px 40px rgba(102, 126, 234, 0.2);
  --transition-fast: 0.2s ease;
  --transition-normal: 0.3s ease;
  --border-radius: 12px;
}

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Roboto', 'Oxygen',
  'Ubuntu', 'Cantarell', 'Fira Sans', 'Droid Sans', 'Helvetica Neue',
  sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  background-color: var(--background-color);
  color: #1f2d3d;
  line-height: 1.6;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 1rem;
}

::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 4px;
}

::-webkit-scrollbar-thumb {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 4px;
  transition: var(--transition-fast);
}

::-webkit-scrollbar-thumb:hover {
  background: linear-gradient(135deg, #5a6fd6 0%, #6a4190 100%);
}

::selection {
  background: rgba(102, 126, 234, 0.3);
  color: #1f2d3d;
}

a {
  color: var(--primary-color);
  text-decoration: none;
  transition: var(--transition-fast);
}

a:hover {
  color: var(--primary-color-dark);
}

img {
  max-width: 100%;
  height: auto;
}

button {
  cursor: pointer;
  border: none;
  outline: none;
  font-family: inherit;
}

input, textarea, select {
  font-family: inherit;
  outline: none;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.slide-up-enter-active,
.slide-up-leave-active {
  transition: all 0.3s ease;
}

.slide-up-enter-from,
.slide-up-leave-to {
  opacity: 0;
  transform: translateY(20px);
}
</style>