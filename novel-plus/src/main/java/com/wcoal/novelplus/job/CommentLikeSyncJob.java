package com.wcoal.novelplus.job;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wcoal.novelplus.dao.entity.BookComment;
import com.wcoal.novelplus.dao.entity.BookCommentLike;
import com.wcoal.novelplus.dao.mapper.BookCommentLikeMapper;
import com.wcoal.novelplus.dao.mapper.BookCommentMapper;
import com.wcoal.novelplus.manager.cache.CommentLikeCacheManager;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 评论点赞数据同步定时任务
 * 将 Redis 中的点赞数据定期同步到 MySQL
 *
 * @author wcoal
 * @since 2025-11-13
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CommentLikeSyncJob {

    private final CommentLikeCacheManager commentLikeCacheManager;
    private final BookCommentMapper bookCommentMapper;
    private final BookCommentLikeMapper bookCommentLikeMapper;

    /**
     * 同步点赞数据到数据库
     * Cron: 0 *\/5 * * * ? (每5分钟执行一次)
     */
    @XxlJob("commentLikeSyncJob")
    public void execute() {
        long startTime = System.currentTimeMillis();
        XxlJobHelper.log("========== 开始同步评论点赞数据 ==========");

        try {
            // 1. 获取待同步的评论ID列表
            Set<String> commentIds = commentLikeCacheManager.getCommentIdsToSync();

            if (commentIds == null || commentIds.isEmpty()) {
                XxlJobHelper.log("没有待同步的数据");
                return;
            }

            XxlJobHelper.log("待同步评论数量: " + commentIds.size());

            int successCount = 0;
            int failCount = 0;

            // 2. 遍历每个评论进行同步
            for (String commentIdStr : commentIds) {
                try {
                    Long commentId = Long.parseLong(commentIdStr);
                    syncCommentLikeData(commentId);
                    successCount++;

                    XxlJobHelper.log("同步成功: commentId=" + commentId);

                } catch (Exception e) {
                    failCount++;
                    log.error("同步评论点赞数据失败: commentId={}", commentIdStr, e);
                    XxlJobHelper.log("同步失败: commentId=" + commentIdStr + ", error=" + e.getMessage());
                }
            }

            long endTime = System.currentTimeMillis();
            String result = String.format(
                    "========== 同步完成 ========== 总数: %d, 成功: %d, 失败: %d, 耗时: %dms",
                    commentIds.size(), successCount, failCount, (endTime - startTime)
            );

            XxlJobHelper.log(result);
            log.info(result);

        } catch (Exception e) {
            log.error("评论点赞数据同步任务执行失败", e);
            XxlJobHelper.log("任务执行失败: " + e.getMessage());
            XxlJobHelper.handleFail(e.getMessage());
        }
    }

    /**
     * 同步单个评论的点赞数据
     */
    @Transactional(rollbackFor = Exception.class)
    public void syncCommentLikeData(Long commentId) {
        log.info("开始同步评论点赞数据: commentId={}", commentId);

        // 1. 从 Redis 获取点赞用户列表
        Set<String> redisUserIdStrs = commentLikeCacheManager.getLikeUserIds(commentId);

        Set<Long> redisUserIds = new HashSet<>();
        if (redisUserIdStrs != null && !redisUserIdStrs.isEmpty()) {
            redisUserIds = redisUserIdStrs.stream()
                    .map(Long::parseLong)
                    .collect(Collectors.toSet());
        }

        // 2. 查询数据库中已有的点赞记录
        QueryWrapper<BookCommentLike> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("comment_id", commentId);
        List<BookCommentLike> existingLikes = bookCommentLikeMapper.selectList(queryWrapper);

        Set<Long> dbUserIds = existingLikes.stream()
                .map(BookCommentLike::getUserId)
                .collect(Collectors.toSet());

        // 3. 计算需要新增的点赞记录（Redis 有，数据库没有）
        Set<Long> toAdd = new HashSet<>(redisUserIds);
        toAdd.removeAll(dbUserIds);

        // 4. 计算需要删除的点赞记录（数据库有，Redis 没有）
        Set<Long> toRemove = new HashSet<>(dbUserIds);
        toRemove.removeAll(redisUserIds);

        // 5. 批量新增点赞记录
        if (!toAdd.isEmpty()) {
            int addCount = 0;
            for (Long userId : toAdd) {
                try {
                    BookCommentLike like = new BookCommentLike();
                    like.setCommentId(commentId);
                    like.setUserId(userId);
                    like.setCreateTime(LocalDateTime.now());
                    like.setUpdateTime(LocalDateTime.now());

                    bookCommentLikeMapper.insert(like);
                    addCount++;

                } catch (DuplicateKeyException e) {
                    // 忽略重复记录
                    log.debug("点赞记录已存在: commentId={}, userId={}", commentId, userId);
                }
            }

            log.info("新增点赞记录: commentId={}, count={}", commentId, addCount);
            XxlJobHelper.log("  新增点赞记录: " + addCount);
        }

        // 6. 批量删除点赞记录
        if (!toRemove.isEmpty()) {
            QueryWrapper<BookCommentLike> deleteWrapper = new QueryWrapper<>();
            deleteWrapper.eq("comment_id", commentId)
                    .in("user_id", toRemove);

            int deleteCount = bookCommentLikeMapper.delete(deleteWrapper);

            log.info("删除点赞记录: commentId={}, count={}", commentId, deleteCount);
            XxlJobHelper.log("  删除点赞记录: " + deleteCount);
        }

        // 7. 更新评论的点赞数量
        BookComment comment = bookCommentMapper.selectById(commentId);
        if (comment != null) {
            int newLikeCount = redisUserIds.size();
            int oldLikeCount = comment.getLikeCount();

            if (oldLikeCount != newLikeCount) {
                comment.setLikeCount(newLikeCount);
                comment.setUpdateTime(LocalDateTime.now());
                bookCommentMapper.updateById(comment);

                log.info("更新评论点赞数: commentId={}, {} -> {}", 
                        commentId, oldLikeCount, newLikeCount);
                XxlJobHelper.log(String.format("  更新点赞数: %d -> %d", oldLikeCount, newLikeCount));
            }
        } else {
            log.warn("评论不存在: commentId={}", commentId);
            XxlJobHelper.log("  警告: 评论不存在");
        }

        // 8. 移除同步标记
        commentLikeCacheManager.removeSyncMark(commentId);

        log.info("同步评论点赞数据完成: commentId={}, likeCount={}", commentId, redisUserIds.size());
    }
}
