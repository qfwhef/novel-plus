import request from '../utils/request'

export function register(params) {
    return request.post('/front/user/register', params);
}

export function login(params) {
    return request.post('/front/user/login', params);
}

export function logout() {
    return request.post('/front/user/logout');
}

export function submitFeedBack(params) {
    return request.post('/front/user/feedback', params);
}

export function comment(params) {
    return request.post('/front/user/comment',params);
}

export function deleteComment(id) {
    return request.delete(`/front/user/comment/${id}`);
}

export function updateComment(id, content) {
    const formData = new FormData();
    formData.append('content', content);
    return request.put(`/front/user/comment/${id}`, formData);
}

export function getUserinfo() {
    return request.get('/front/user');
}

export function updateUserInfo(userInfo) {
    return request.put('/front/user',userInfo);
}

export function listComments(params) {
    return request.get('/front/user/comments', { params });
}

export function saveCommentReply(params) {
    return request.post('/front/user/comment/reply', params);
}

export function deleteCommentReply(replyId) {
    return request.delete(`/front/user/comment/reply/${replyId}`);
}

export function listCommentReplies(commentId) {
    return request.get(`/front/user/comment/${commentId}/replies`);
}