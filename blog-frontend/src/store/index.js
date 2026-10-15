import { createStore } from 'vuex'

export default createStore({
    state: {
        token: localStorage.getItem('token') || '',
        user: JSON.parse(localStorage.getItem('user') || '{}'),
        categories: []
    },
    mutations: {
        SET_TOKEN(state, token) {
            state.token = token
            localStorage.setItem('token', token)
        },
        SET_USER(state, user) {
            state.user = user
            localStorage.setItem('user', JSON.stringify(user))
        },
        SET_CATEGORIES(state, categories) {
            state.categories = categories
        },
        CLEAR_AUTH(state) {
            state.token = ''
            state.user = {}
            localStorage.removeItem('token')
            localStorage.removeItem('user')
        }
    },
    actions: {
        login({ commit }, { token, user }) {
            commit('SET_TOKEN', token)
            commit('SET_USER', user)
        },
        logout({ commit }) {
            commit('CLEAR_AUTH')
        },
        setCategories({ commit }, categories) {
            commit('SET_CATEGORIES', categories)
        }
    },
    getters: {
        isAuthenticated: state => !!state.token,
        currentUser: state => state.user,
        isAdmin: state => state.user.role === 'ROLE_ADMIN',
        getCategories: state => state.categories
    }
})