import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'

const routes = [
    {
        path: '/',
        name: 'Home',
        component: () => import('../views/Home.vue')
    },
    {
        path: '/login',
        name: 'Login',
        component: () => import('../views/Login.vue'),
        meta: { guest: true }
    },
    {
        path: '/register',
        name: 'Register',
        component: () => import('../views/Register.vue'),
        meta: { guest: true }
    },
    {
        path: '/article/:id',
        name: 'ArticleDetail',
        component: () => import('../views/ArticleDetail.vue')
    },
    {
        path: '/dashboard',
        name: 'Dashboard',
        component: () => import('../views/Dashboard.vue'),
        meta: { requiresAuth: true }
    },
    {
        path: '/admin',
        name: 'Admin',
        component: () => import('../views/Admin.vue'),
        meta: { requiresAuth: true, requiresAdmin: true }
    },
    {
        path: '/:pathMatch(.*)*',
        name: 'NotFound',
        component: () => import('../views/NotFound.vue')
    }
]

const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
    const token = localStorage.getItem('token')
    const user = JSON.parse(localStorage.getItem('user') || '{}')

    // 需要认证的页面
    if (to.matched.some(record => record.meta.requiresAuth)) {
        if (!token) {
            ElMessage.warning('请先登录')
            next('/login')
        } else if (to.matched.some(record => record.meta.requiresAdmin) && user.role !== 'ROLE_ADMIN') {
            ElMessage.error('需要管理员权限')
            next('/')
        } else {
            next()
        }
    }
    // 游客页面（已登录用户不能访问）
    else if (to.matched.some(record => record.meta.guest)) {
        if (token) {
            next('/')
        } else {
            next()
        }
    }
    // 其他页面
    else {
        next()
    }
})

export default router