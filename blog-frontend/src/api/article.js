import request from './index'

export function getArticles(params) {
    return request({
        url: '/articles',
        method: 'get',
        params
    })
}

export function getArticle(id) {
    return request({
        url: `/articles/${id}`,
        method: 'get'
    })
}

export function createArticle(data) {
    return request({
        url: '/articles',
        method: 'post',
        data
    })
}

export function updateArticle(id, data) {
    return request({
        url: `/articles/${id}`,
        method: 'put',
        data
    })
}

export function deleteArticle(id) {
    return request({
        url: `/articles/${id}`,
        method: 'delete'
    })
}

export function getMyArticles(params) {
    return request({
        url: '/articles/my',
        method: 'get',
        params
    })
}

export function reviewArticle(id, data) {
    return request({
        url: `/articles/${id}/review`,
        method: 'post',
        data
    })
}

export function getAllArticles(params) {
    return request({
        url: '/articles/admin/all',
        method: 'get',
        params
    })
}

export function getPendingArticles(params) {
    return request({
        url: '/articles/admin/pending',
        method: 'get',
        params
    })
}

export function getArticleStatistics() {
    return request({
        url: '/articles/admin/statistics',
        method: 'get'
    })
}