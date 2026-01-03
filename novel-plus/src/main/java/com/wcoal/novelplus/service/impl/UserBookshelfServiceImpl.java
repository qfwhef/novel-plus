package com.wcoal.novelplus.service.impl;

import com.wcoal.novelplus.dao.entity.UserBookShelf;
import com.wcoal.novelplus.dao.mapper.UserBookshelfMapper;
import com.wcoal.novelplus.manager.redis.ReadProgressManager;
import com.wcoal.novelplus.service.IUserBookshelfService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 用户书架 服务实现类
 * </p>
 *
 * @author wcoal
 * @since 2025-10-21
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserBookshelfServiceImpl extends ServiceImpl<UserBookshelfMapper, UserBookShelf> implements IUserBookshelfService {

    private final ReadProgressManager readProgressManager;
    private final UserBookshelfMapper userBookshelfMapper;

    /**
     * 获取用户的阅读进度
     * 优化：优先从 Redis 读取，Redis 没有再查数据库
     *
     * @param userId 用户ID
     * @param bookId 书籍ID
     * @return 章节ID，如果不存在返回 null
     */
    @Override
    public Long getReadProgress(Long userId, Long bookId) {
        // 1. 先从 Redis 获取
        Long chapterId = readProgressManager.getReadProgress(userId, bookId);
        
        if (chapterId != null) {
            log.debug("从Redis获取阅读进度: userId={}, bookId={}, chapterId={}", userId, bookId, chapterId);
            return chapterId;
        }

        // 2. Redis 没有，从数据库获取
        chapterId = userBookshelfMapper.selectPreContentIdByUserIdAndBookId(userId, bookId);
        
        if (chapterId != null) {
            log.debug("从数据库获取阅读进度: userId={}, bookId={}, chapterId={}", userId, bookId, chapterId);
            
            // 3. 回写到 Redis，下次直接从 Redis 读取
            readProgressManager.updateReadProgress(userId, bookId, chapterId);
        }

        return chapterId;
    }
}
