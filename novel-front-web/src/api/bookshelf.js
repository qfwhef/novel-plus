import request from '../utils/request'

/**
 * 获取用户书架列表
 * @param {Object} params - 查询参数
 * @returns {Promise}
 */
export function getBookshelfList(params) {
    return request.get('/front/user/bookshelf', { params });
}

/**
 * 添加书籍到书架
 * @param {Object} params - { bookId: 书籍ID }
 * @returns {Promise}
 */
export function addToBookshelf(params) {
    return request.post('/front/user/bookshelf', params);
}

/**
 * 从书架移除书籍
 * @param {Number} bookId - 书籍ID
 * @returns {Promise}
 */
export function removeFromBookshelf(bookId) {
    return request.delete(`/front/user/bookshelf/${bookId}`);
}

/**
 * 检查书籍是否在书架中
 * @param {Number} bookId - 书籍ID
 * @returns {Promise}
 */
export function checkInBookshelf(bookId) {
    return request.get(`/front/user/bookshelf/check/${bookId}`);
}
