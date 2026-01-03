import request from '../utils/request'

/**
 * 切换点赞状态
 * @param {number} commentId - 评论ID
 * @param {boolean} liked - 是否点赞（true-点赞，false-取消点赞）
 */
export function toggleCommentLike(commentId, liked) {
  return request.post('/front/comment/like', {
    commentId,
    liked
  });
}

// ========== 查询接口 ==========

/**
 * 获取点赞数量
 */
export function getLikeCount(commentId) {
  return request.get(`/front/comment/${commentId}/like/count`);
}

/**
 * 获取点赞状态
 */
export function getLikeStatus(commentId) {
  return request.get(`/front/comment/${commentId}/like/status`);
}

/**
 * 批量获取点赞状态
 */
export function batchGetLikeStatus(commentIds) {
  return request.post('/front/comment/like/batch-status', commentIds);
}
