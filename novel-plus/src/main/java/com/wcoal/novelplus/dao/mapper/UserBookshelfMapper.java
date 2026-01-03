package com.wcoal.novelplus.dao.mapper;

import com.wcoal.novelplus.dao.entity.UserBookShelf;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * <p>
 * 用户书架 Mapper 接口
 * </p>
 *
 * @author wcoal
 * @since 2025-10-21
 */
public interface UserBookshelfMapper extends BaseMapper<UserBookShelf> {

    /**
     * 根据用户ID和小说ID查询上一次阅读的章节ID
     *
     * @param userId 用户ID
     * @param bookId 书籍ID
     * @return 章节ID
     */
    @Select("select pre_content_id from user_bookshelf where user_id = #{userId} and book_id = #{bookId}")
    Long selectPreContentIdByUserIdAndBookId(@Param("userId") Long userId, @Param("bookId") Long bookId);

    /**
     * 更新用户书架阅读进度
     *
     * @param userId    用户ID
     * @param bookId    书籍ID
     * @param chapterId 章节ID
     * @return 更新行数
     */
    @Update("update user_bookshelf set pre_content_id = #{chapterId}, update_time = now() " +
            "where user_id = #{userId} and book_id = #{bookId}")
    int updateReadProgress(@Param("userId") Long userId, @Param("bookId") Long bookId, @Param("chapterId") Long chapterId);

}
