import request from './index'

export function getComments(articleId, params) {
    return request({
        url: `/comments/article/${articleId}`,
        method: 'get',
        params
    })
}

export function createComment(articleId, data) {
    return request({
        url: `/comments/article/${articleId}`,
        method: 'post',
        data
    })
}

export function deleteComment(id) {
    return request({
        url: `/comments/${id}`,
        method: 'delete'
    })
}
