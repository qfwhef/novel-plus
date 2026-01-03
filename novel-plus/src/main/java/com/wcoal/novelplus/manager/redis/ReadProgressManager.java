package com.wcoal.novelplus.manager.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 阅读进度 Redis 管理器
 * 负责管理用户阅读进度的缓存操作
 *
 * @author wcoal
 * @since 2025-11-18
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ReadProgressManager {

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 阅读进度缓存 Key 前缀
     * Key 格式: Cache::Novel::ReadProgress::{userId}:{bookId}
     * Value 格式: {chapterId}:{timestamp}
     */
    private static final String READ_PROGRESS_KEY_PREFIX = "Cache::Novel::ReadProgress::";

    /**
     * 待同步标记 Key
     * 使用 Set 存储所有待同步的 userId:bookId
     */
    private static final String SYNC_PENDING_KEY = "Cache::Novel::ReadProgress::Pending";

    /**
     * 缓存过期时间：7天
     */
    private static final Duration CACHE_EXPIRE_TIME = Duration.ofDays(7);

    /**
     * 更新阅读进度到 Redis
     *
     * @param userId    用户ID
     * @param bookId    书籍ID
     * @param chapterId 章节ID
     */
    public void updateReadProgress(Long userId, Long bookId, Long chapterId) {
        try {
            String key = buildProgressKey(userId, bookId);
            String value = chapterId + ":" + System.currentTimeMillis();

            // 1. 更新阅读进度
            stringRedisTemplate.opsForValue().set(key, value, CACHE_EXPIRE_TIME);

            // 2. 添加到待同步集合
            String pendingKey = userId + ":" + bookId;
            stringRedisTemplate.opsForSet().add(SYNC_PENDING_KEY, pendingKey);

            log.debug("更新阅读进度到Redis: userId={}, bookId={}, chapterId={}", userId, bookId, chapterId);

        } catch (Exception e) {
            log.error("更新阅读进度到Redis失败: userId={}, bookId={}, chapterId={}", 
                    userId, bookId, chapterId, e);
        }
    }

    /**
     * 获取用户的阅读进度
     *
     * @param userId 用户ID
     * @param bookId 书籍ID
     * @return 章节ID，如果不存在返回 null
     */
    public Long getReadProgress(Long userId, Long bookId) {
        try {
            String key = buildProgressKey(userId, bookId);
            String value = stringRedisTemplate.opsForValue().get(key);

            if (value != null && value.contains(":")) {
                String chapterIdStr = value.split(":")[0];
                return Long.parseLong(chapterIdStr);
            }

        } catch (Exception e) {
            log.error("从Redis获取阅读进度失败: userId={}, bookId={}", userId, bookId, e);
        }

        return null;
    }

    /**
     * 获取所有待同步的阅读进度
     *
     * @return 待同步的 userId:bookId 集合
     */
    public Set<String> getPendingProgressKeys() {
        try {
            return stringRedisTemplate.opsForSet().members(SYNC_PENDING_KEY);
        } catch (Exception e) {
            log.error("获取待同步阅读进度列表失败", e);
            return null;
        }
    }

    /**
     * 批量获取阅读进度数据
     *
     * @param pendingKeys 待同步的 userId:bookId 集合
     * @return Map<userId:bookId, chapterId>
     */
    public Map<String, Long> batchGetProgress(Set<String> pendingKeys) {
        Map<String, Long> progressMap = new HashMap<>();

        if (pendingKeys == null || pendingKeys.isEmpty()) {
            return progressMap;
        }

        try {
            for (String pendingKey : pendingKeys) {
                String[] parts = pendingKey.split(":");
                if (parts.length != 2) {
                    log.warn("无效的待同步Key: {}", pendingKey);
                    continue;
                }

                Long userId = Long.parseLong(parts[0]);
                Long bookId = Long.parseLong(parts[1]);

                String key = buildProgressKey(userId, bookId);
                String value = stringRedisTemplate.opsForValue().get(key);

                if (value != null && value.contains(":")) {
                    String chapterIdStr = value.split(":")[0];
                    Long chapterId = Long.parseLong(chapterIdStr);
                    progressMap.put(pendingKey, chapterId);
                }
            }

        } catch (Exception e) {
            log.error("批量获取阅读进度失败", e);
        }

        return progressMap;
    }

    /**
     * 移除待同步标记
     *
     * @param userId 用户ID
     * @param bookId 书籍ID
     */
    public void removeSyncMark(Long userId, Long bookId) {
        try {
            String pendingKey = userId + ":" + bookId;
            stringRedisTemplate.opsForSet().remove(SYNC_PENDING_KEY, pendingKey);

            log.debug("移除待同步标记: userId={}, bookId={}", userId, bookId);

        } catch (Exception e) {
            log.error("移除待同步标记失败: userId={}, bookId={}", userId, bookId, e);
        }
    }

    /**
     * 删除阅读进度缓存
     *
     * @param userId 用户ID
     * @param bookId 书籍ID
     */
    public void deleteProgress(Long userId, Long bookId) {
        try {
            String key = buildProgressKey(userId, bookId);
            stringRedisTemplate.delete(key);

            // 同时移除待同步标记
            removeSyncMark(userId, bookId);

            log.debug("删除阅读进度缓存: userId={}, bookId={}", userId, bookId);

        } catch (Exception e) {
            log.error("删除阅读进度缓存失败: userId={}, bookId={}", userId, bookId, e);
        }
    }

    /**
     * 构建阅读进度缓存 Key
     *
     * @param userId 用户ID
     * @param bookId 书籍ID
     * @return 缓存Key
     */
    private String buildProgressKey(Long userId, Long bookId) {
        return READ_PROGRESS_KEY_PREFIX + userId + ":" + bookId;
    }
}
