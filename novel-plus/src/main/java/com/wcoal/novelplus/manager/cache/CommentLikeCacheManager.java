package com.wcoal.novelplus.manager.cache;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wcoal.novelplus.dao.entity.BookComment;
import com.wcoal.novelplus.dao.entity.BookCommentLike;
import com.wcoal.novelplus.dao.mapper.BookCommentLikeMapper;
import com.wcoal.novelplus.dao.mapper.BookCommentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 评论点赞缓存管理器
 * 所有增删改操作都在 Redis 中完成，定时任务异步持久化到 MySQL
 *
 * @author wcoal
 * @since 2025-11-13
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CommentLikeCacheManager {

    private final BookCommentMapper bookCommentMapper;
    private final BookCommentLikeMapper bookCommentLikeMapper;
    private final StringRedisTemplate stringRedisTemplate;

    /**
     * Redis Key 前缀
     */
    private static final String COMMENT_LIKE_SET_PREFIX = "comment:like:set:";
    private static final String COMMENT_LIKE_COUNT_PREFIX = "comment:like:count:";
    private static final String USER_LIKE_COMMENTS_PREFIX = "user:like:comments:";
    private static final String COMMENT_LIKE_SYNC_SET = "comment:like:sync:set";
    private static final String COMMENT_LIKE_EMPTY_PREFIX = "comment:like:empty:";

    /**
     * 缓存过期时间（7天）
     */
    private static final long CACHE_EXPIRE_DAYS = 7;

    // ==================== 点赞操作 ====================

    /**
     * 点赞（只操作 Redis）
     */
    public void like(Long commentId, Long userId) {
        String setKey = COMMENT_LIKE_SET_PREFIX + commentId;
        String countKey = COMMENT_LIKE_COUNT_PREFIX + commentId;
        String userLikeKey = USER_LIKE_COMMENTS_PREFIX + userId;

        // 1. 添加到点赞集合
        stringRedisTemplate.opsForSet().add(setKey, userId.toString());

        // 2. 增加点赞数量（如果 count 不存在，先从数据库获取初始值）
        String currentCount = stringRedisTemplate.opsForValue().get(countKey);
        if (currentCount == null) {
            // count 不存在，从数据库获取当前值
            BookComment comment = bookCommentMapper.selectById(commentId);
            int dbCount = (comment != null && comment.getLikeCount() != null) ? comment.getLikeCount() : 0;
            // 设置为数据库值 + 1
            stringRedisTemplate.opsForValue().set(countKey, String.valueOf(dbCount + 1), CACHE_EXPIRE_DAYS, TimeUnit.DAYS);
        } else {
            // count 存在，直接增加
            stringRedisTemplate.opsForValue().increment(countKey);
        }

        // 3. 添加到用户点赞列表
        stringRedisTemplate.opsForSet().add(userLikeKey, commentId.toString());

        // 4. 标记为待同步
        stringRedisTemplate.opsForSet().add(COMMENT_LIKE_SYNC_SET, commentId.toString());

        // 5. 设置过期时间
        stringRedisTemplate.expire(setKey, CACHE_EXPIRE_DAYS, TimeUnit.DAYS);
        stringRedisTemplate.expire(countKey, CACHE_EXPIRE_DAYS, TimeUnit.DAYS);
        stringRedisTemplate.expire(userLikeKey, CACHE_EXPIRE_DAYS, TimeUnit.DAYS);

        log.debug("Redis 点赞成功: commentId={}, userId={}", commentId, userId);
    }

    /**
     * 取消点赞（只操作 Redis）
     */
    public void unlike(Long commentId, Long userId) {
        String setKey = COMMENT_LIKE_SET_PREFIX + commentId;
        String countKey = COMMENT_LIKE_COUNT_PREFIX + commentId;
        String userLikeKey = USER_LIKE_COMMENTS_PREFIX + userId;

        // 1. 从点赞集合中移除
        stringRedisTemplate.opsForSet().remove(setKey, userId.toString());

        // 2. 减少点赞数量（如果 count 不存在，先从数据库获取初始值）
        String currentCount = stringRedisTemplate.opsForValue().get(countKey);
        if (currentCount == null) {
            // count 不存在，从数据库获取当前值
            BookComment comment = bookCommentMapper.selectById(commentId);
            int dbCount = (comment != null && comment.getLikeCount() != null) ? comment.getLikeCount() : 0;
            // 设置为数据库值 - 1（最小为0）
            int newCount = Math.max(0, dbCount - 1);
            stringRedisTemplate.opsForValue().set(countKey, String.valueOf(newCount), CACHE_EXPIRE_DAYS, TimeUnit.DAYS);
        } else {
            // count 存在，直接减少（但不能小于0）
            Long count = stringRedisTemplate.opsForValue().decrement(countKey);
            if (count != null && count < 0) {
                stringRedisTemplate.opsForValue().set(countKey, "0", CACHE_EXPIRE_DAYS, TimeUnit.DAYS);
            }
        }

        // 3. 检查 Set 是否为空，如果为空则删除
        Long setSize = stringRedisTemplate.opsForSet().size(setKey);
        if (setSize == null || setSize == 0) {
            stringRedisTemplate.delete(setKey);
            log.debug("点赞集合为空，删除 Set: commentId={}", commentId);
        }

        // 4. 从用户点赞列表中移除
        stringRedisTemplate.opsForSet().remove(userLikeKey, commentId.toString());

        // 5. 标记为待同步
        stringRedisTemplate.opsForSet().add(COMMENT_LIKE_SYNC_SET, commentId.toString());

        log.debug("Redis 取消点赞成功: commentId={}, userId={}", commentId, userId);
    }

    // ==================== 查询操作 ====================

    /**
     * 判断用户是否点赞（优先 Redis，未命中时查数据库）
     */
    public Boolean isLiked(Long commentId, Long userId) {
        String setKey = COMMENT_LIKE_SET_PREFIX + commentId;

        // 1. 检查 Redis 是否存在该评论的点赞数据
        Boolean hasKey = stringRedisTemplate.hasKey(setKey);

        if (Boolean.TRUE.equals(hasKey)) {
            // Redis 中有数据，直接返回
            Boolean isMember = stringRedisTemplate.opsForSet().isMember(setKey, userId.toString());
            log.debug("从 Redis 查询点赞状态: commentId={}, userId={}, liked={}", 
                    commentId, userId, isMember);
            return isMember;
        }

        // 2. Redis 未命中，查询数据库
        QueryWrapper<BookCommentLike> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("comment_id", commentId)
                .eq("user_id", userId);

        Long count = bookCommentLikeMapper.selectCount(queryWrapper);
        boolean liked = count > 0;

        log.debug("从数据库查询点赞状态: commentId={}, userId={}, liked={}", 
                commentId, userId, liked);

        // 3. 如果数据库中有点赞数据，重建缓存
        if (liked) {
            rebuildCommentLikeCache(commentId);
        }

        return liked;
    }

    /**
     * 获取点赞数量（只查 Redis，未命中时查数据库）
     */
    public Integer getLikeCount(Long commentId) {
        String countKey = COMMENT_LIKE_COUNT_PREFIX + commentId;

        // 1. 从 Redis 获取
        String countStr = stringRedisTemplate.opsForValue().get(countKey);
        if (countStr != null) {
            log.debug("从 Redis 查询点赞数量: commentId={}, count={}", commentId, countStr);
            return Integer.parseInt(countStr);
        }

        // 2. Redis 未命中，查询数据库
        BookComment comment = bookCommentMapper.selectById(commentId);
        if (comment == null) {
            log.warn("评论不存在: commentId={}", commentId);
            return 0;
        }

        Integer likeCount = (comment.getLikeCount() != null) ? comment.getLikeCount() : 0;
        log.debug("从数据库查询点赞数量: commentId={}, count={}", commentId, likeCount);

        // 3. 缓存到 Redis
        stringRedisTemplate.opsForValue().set(
                countKey,
                likeCount.toString(),
                CACHE_EXPIRE_DAYS,
                TimeUnit.DAYS
        );

        return likeCount;
    }

    /**
     * 批量查询用户对评论的点赞状态
     */
    public Set<Long> getUserLikedComments(Long userId, Set<Long> commentIds) {
        if (commentIds == null || commentIds.isEmpty()) {
            return new HashSet<>();
        }

        Set<Long> likedComments = new HashSet<>();

        for (Long commentId : commentIds) {
            if (Boolean.TRUE.equals(isLiked(commentId, userId))) {
                likedComments.add(commentId);
            }
        }

        return likedComments;
    }

    // ==================== 缓存管理 ====================

    /**
     * 重建评论点赞缓存（从数据库加载）
     */
    public void rebuildCommentLikeCache(Long commentId) {
        log.info("开始重建评论点赞缓存: commentId={}", commentId);

        // 1. 查询该评论的所有点赞记录
        QueryWrapper<BookCommentLike> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("comment_id", commentId);

        List<BookCommentLike> likes = bookCommentLikeMapper.selectList(queryWrapper);

        if (likes == null || likes.isEmpty()) {
            log.debug("评论无点赞记录: commentId={}", commentId);
            // 设置空标记
            stringRedisTemplate.opsForValue().set(
                    COMMENT_LIKE_EMPTY_PREFIX + commentId,
                    "1",
                    1,
                    TimeUnit.HOURS
            );
            return;
        }

        // 2. 批量添加到 Redis Set
        String setKey = COMMENT_LIKE_SET_PREFIX + commentId;
        String[] userIds = likes.stream()
                .map(like -> like.getUserId().toString())
                .toArray(String[]::new);

        stringRedisTemplate.opsForSet().add(setKey, userIds);

        // 3. 设置点赞数量
        String countKey = COMMENT_LIKE_COUNT_PREFIX + commentId;
        stringRedisTemplate.opsForValue().set(
                countKey,
                String.valueOf(likes.size()),
                CACHE_EXPIRE_DAYS,
                TimeUnit.DAYS
        );

        // 4. 设置过期时间
        stringRedisTemplate.expire(setKey, CACHE_EXPIRE_DAYS, TimeUnit.DAYS);

        log.info("重建评论点赞缓存成功: commentId={}, likeCount={}", commentId, likes.size());
    }

    /**
     * 清除评论的点赞缓存
     */
    public void clearCommentLikeCache(Long commentId) {
        String setKey = COMMENT_LIKE_SET_PREFIX + commentId;
        String countKey = COMMENT_LIKE_COUNT_PREFIX + commentId;
        String emptyKey = COMMENT_LIKE_EMPTY_PREFIX + commentId;

        stringRedisTemplate.delete(setKey);
        stringRedisTemplate.delete(countKey);
        stringRedisTemplate.delete(emptyKey);

        log.debug("清除评论点赞缓存: commentId={}", commentId);
    }

    // ==================== 定时任务相关 ====================

    /**
     * 获取需要同步的评论ID列表
     */
    public Set<String> getCommentIdsToSync() {
        return stringRedisTemplate.opsForSet().members(COMMENT_LIKE_SYNC_SET);
    }

    /**
     * 获取评论的所有点赞用户ID（从 Redis）
     */
    public Set<String> getLikeUserIds(Long commentId) {
        String setKey = COMMENT_LIKE_SET_PREFIX + commentId;
        return stringRedisTemplate.opsForSet().members(setKey);
    }

    /**
     * 移除待同步标记
     */
    public void removeSyncMark(Long commentId) {
        stringRedisTemplate.opsForSet().remove(COMMENT_LIKE_SYNC_SET, commentId.toString());
        log.debug("移除同步标记: commentId={}", commentId);
    }

    /**
     * 获取 Redis 中的点赞数量（不查数据库）
     */
    public Long getLikeCountFromRedis(Long commentId) {
        String setKey = COMMENT_LIKE_SET_PREFIX + commentId;
        return stringRedisTemplate.opsForSet().size(setKey);
    }
}
