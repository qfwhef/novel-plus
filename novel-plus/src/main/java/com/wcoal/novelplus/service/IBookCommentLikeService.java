package com.wcoal.novelplus.service;

import com.wcoal.novelplus.core.common.resp.RestResp;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 评论点赞 服务类
 * </p>
 *
 * @author wcoal
 * @since 2025-11-12
 */
public interface IBookCommentLikeService {

    /**
     * 切换点赞状态（统一接口）
     */
    RestResp<Void> toggleLike(Long commentId, Long userId, Boolean liked);

    /**
     * 点赞评论（内部方法）
     */
    RestResp<Void> like(Long commentId, Long userId);

    /**
     * 取消点赞（内部方法）
     */
    RestResp<Void> unlike(Long commentId, Long userId);

    /**
     * 查询点赞数量
     */
    RestResp<Integer> getLikeCount(Long commentId);

    /**
     * 查询用户是否点赞
     */
    RestResp<Boolean> isLiked(Long commentId, Long userId);

    /**
     * 批量查询点赞状态
     */
    RestResp<Map<Long, Boolean>> batchIsLiked(List<Long> commentIds, Long userId);
}
