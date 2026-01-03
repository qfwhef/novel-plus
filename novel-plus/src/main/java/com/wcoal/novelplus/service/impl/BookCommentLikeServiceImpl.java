package com.wcoal.novelplus.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wcoal.novelplus.core.annotation.Key;
import com.wcoal.novelplus.core.annotation.Lock;
import com.wcoal.novelplus.core.common.enums.ErrorCodeEnum;
import com.wcoal.novelplus.core.common.resp.RestResp;
import com.wcoal.novelplus.dao.entity.BookComment;
import com.wcoal.novelplus.dao.entity.BookCommentLike;
import com.wcoal.novelplus.dao.mapper.BookCommentLikeMapper;
import com.wcoal.novelplus.dao.mapper.BookCommentMapper;
import com.wcoal.novelplus.manager.cache.CommentLikeCacheManager;
import com.wcoal.novelplus.service.IBookCommentLikeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 评论点赞服务实现类 - 纯 Redis 方案
 * 所有增删改操作都在 Redis 中完成，定时任务异步持久化到 MySQL
 *
 * @author wcoal
 * @since 2025-11-12
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BookCommentLikeServiceImpl extends ServiceImpl<BookCommentLikeMapper, BookCommentLike>
        implements IBookCommentLikeService {

    private final BookCommentMapper bookCommentMapper;
    private final CommentLikeCacheManager commentLikeCacheManager;

    @Override
    @Lock(prefix = "comment:like:toggle", isWait = false, timeout = 3, failCode = ErrorCodeEnum.USER_REQUEST_FREQUENT)
    public RestResp<Void> toggleLike(@Key Long commentId, @Key Long userId, Boolean liked) {
        if (Boolean.TRUE.equals(liked)) {
            // 点赞
            return like(commentId, userId);
        } else {
            // 取消点赞
            return unlike(commentId, userId);
        }
    }

    @Override
    @Lock(prefix = "comment:like", isWait = false, timeout = 3, failCode = ErrorCodeEnum.USER_REQUEST_FREQUENT)
    public RestResp<Void> like(@Key Long commentId, @Key Long userId) {
        // 1. 检查是否已点赞
        Boolean isLiked = commentLikeCacheManager.isLiked(commentId, userId);
        if (Boolean.TRUE.equals(isLiked)) {
            return RestResp.fail(ErrorCodeEnum.COMMENT_ALREADY_LIKED);
        }

        // 2. 校验评论是否存在
        BookComment comment = bookCommentMapper.selectById(commentId);
        if (comment == null) {
            return RestResp.fail(ErrorCodeEnum.USER_COMMENT_NOT_EXIST);
        }

        // 3. 执行点赞（只操作 Redis）
        commentLikeCacheManager.like(commentId, userId);

        log.info("用户点赞成功: commentId={}, userId={}", commentId, userId);
        return RestResp.ok();
    }

    @Override
    @Lock(prefix = "comment:unlike", isWait = false, timeout = 3, failCode = ErrorCodeEnum.USER_REQUEST_FREQUENT)
    public RestResp<Void> unlike(@Key Long commentId, @Key Long userId) {
        // 1. 检查是否已点赞
        Boolean isLiked = commentLikeCacheManager.isLiked(commentId, userId);
        if (Boolean.FALSE.equals(isLiked)) {
            return RestResp.fail(ErrorCodeEnum.COMMENT_NOT_LIKED);
        }

        // 2. 执行取消点赞（只操作 Redis）
        commentLikeCacheManager.unlike(commentId, userId);

        log.info("用户取消点赞成功: commentId={}, userId={}", commentId, userId);
        return RestResp.ok();
    }

    @Override
    public RestResp<Integer> getLikeCount(Long commentId) {
        // 从缓存管理器获取（自动处理 Redis 未命中的情况）
        Integer count = commentLikeCacheManager.getLikeCount(commentId);
        return RestResp.ok(count);
    }

    @Override
    public RestResp<Boolean> isLiked(Long commentId, Long userId) {
        // 从缓存管理器获取（自动处理 Redis 未命中的情况）
        Boolean isLiked = commentLikeCacheManager.isLiked(commentId, userId);
        return RestResp.ok(isLiked);
    }

    @Override
    public RestResp<Map<Long, Boolean>> batchIsLiked(List<Long> commentIds, Long userId) {
        if (commentIds == null || commentIds.isEmpty()) {
            return RestResp.ok(new HashMap<>());
        }

        // 批量查询点赞状态（利用缓存）
        Set<Long> commentIdSet = commentIds.stream().collect(Collectors.toSet());
        Set<Long> likedComments = commentLikeCacheManager.getUserLikedComments(userId, commentIdSet);

        // 转换为 Map
        Map<Long, Boolean> result = commentIds.stream()
                .collect(Collectors.toMap(
                        id -> id,
                        likedComments::contains
                ));

        return RestResp.ok(result);
    }
}
