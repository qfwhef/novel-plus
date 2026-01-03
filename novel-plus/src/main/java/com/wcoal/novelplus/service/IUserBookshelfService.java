package com.wcoal.novelplus.service;

import com.wcoal.novelplus.dao.entity.UserBookShelf;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 用户书架 服务类
 * </p>
 *
 * @author wcoal
 * @since 2025-10-21
 */
public interface IUserBookshelfService extends IService<UserBookShelf> {

    /**
     * 获取用户的阅读进度（优先从 Redis 读取）
     *
     * @param userId 用户ID
     * @param bookId 书籍ID
     * @return 章节ID，如果不存在返回 null
     */
    Long getReadProgress(Long userId, Long bookId);

}
