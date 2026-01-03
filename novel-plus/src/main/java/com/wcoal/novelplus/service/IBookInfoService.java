package com.wcoal.novelplus.service;

import com.wcoal.novelplus.core.common.req.PageReqDto;
import com.wcoal.novelplus.core.common.resp.PageRespDto;
import com.wcoal.novelplus.core.common.resp.RestResp;
import com.wcoal.novelplus.dao.entity.BookInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wcoal.novelplus.dto.req.BookAddReqDto;
import com.wcoal.novelplus.dto.req.ChapterAddReqDto;
import com.wcoal.novelplus.dto.req.ChapterUpdateReqDto;
import com.wcoal.novelplus.dto.req.UserCommentReqDto;
import com.wcoal.novelplus.dto.resp.*;
import jakarta.validation.Valid;

import java.util.List;

/**
 * <p>
 * 小说信息 服务类
 * </p>
 *
 * @author wcoal
 * @since 2025-09-30
 */
public interface IBookInfoService extends IService<BookInfo> {

    /**
     * 获取小说分类列表
     * @param workDirection 作品方向
     * @return 小说分类列表
     */
    RestResp<List<BookCategoryRespDto>> listCategories(Integer workDirection);

    /**
     * 获取小说详情
     * @param bookId 小说ID
     * @return 小说详情
     */
    RestResp<BookInfoRespDto> getBookInfo(Long bookId);

    /**
     * 增加小说点击量
     * @param bookId 小说ID
     * @return 无
     */
    RestResp<Void> addVisitCount(Long bookId);

     /**
     * 获取小说最新章节相关信息
     * @param bookId 小说ID
     * @return 小说最新章节相关信息
     */
    RestResp<BookChapterAboutRespDto> getLastChapterAbout(Long bookId);

    /**
     * 小说推荐列表查询接口
     * @param bookId 小说ID
     * @return 小说推荐列表
     */
    RestResp<List<BookInfoRespDto>> listRecBooks(Long bookId);

     /**
     * 小说章节列表查询接口
     * @param bookId 小说ID
     * @return 小说章节列表
     */
    RestResp<List<BookChapterRespDto>> listChapters(Long bookId);

     /**
     * 小说内容相关信息查询接口
     * @param chapterId 章节ID
     * @return 小说内容相关信息
     */
    RestResp<BookContentAboutRespDto> getBookContentAbout(Long chapterId);

     /**
     * 获取上一章节ID接口
     * @param chapterId 章节ID
     * @return 上一章节ID
     */
    RestResp<Long> getPreChapterId(Long chapterId);

     /**
     * 获取下一章节ID接口
     * @param chapterId 章节ID
     * @return 下一章节ID
     */
    RestResp<Long> getNextChapterId(Long chapterId);

     /**
     * 小说点击榜查询接口
     * @return 小说点击榜
     */
    RestResp<List<BookRankRespDto>> listVisitRankBooks();

     /**
     * 小说新书榜查询接口
     * @return 小说新书榜
     */
    RestResp<List<BookRankRespDto>> listNewestRankBooks();

     /**
     * 小说更新榜查询接口
     * @return 小说更新榜
     */
    RestResp<List<BookRankRespDto>> listUpdateRankBooks();

     /**
     * 小说最新评论查询接口
     * @param bookId 小说ID
     * @return 小说最新评论
     */
    RestResp<BookCommentRespDto> listNewestComments(Long bookId);

     /**
     * 用户发表评论接口
     * @param dto 用户评论请求参数
     * @return 无
     */
    RestResp<Void> userComment(@Valid UserCommentReqDto dto);

     /**
     * 修改评论接口
     * @param userId 用户ID
     * @param id 评论ID
     * @param content 评论内容
     * @return 无
     */
    RestResp<Void> updateComment(Long userId, Long id, String content);

     /**
     * 删除评论接口
     * @param userId 用户ID
     * @param commentId 评论ID
     * @return 无
     */
     RestResp<Void> deleteComment(Long userId, Long commentId);

    /**
     * 发布小说接口
     * @param dto 发布小说请求参数
     * @return 无
     */
    RestResp<Void> saveBook(@Valid BookAddReqDto dto);

     /**
     * 作者查询自己发布的小说接口
     * @param dto 分页查询参数
     * @return 作者发布的小说列表
     */
    RestResp<PageRespDto<BookInfoRespDto>> listAuthorBooks(PageReqDto dto);

     /**
     * 作者发布小说章节接口
     * @param dto 发布小说章节请求参数
     * @return 无
     */
    RestResp<Void> saveBookChapter(@Valid ChapterAddReqDto dto);

     /**
     * 小说章节发布列表查询接口
     * @param bookId 小说ID
     * @param dto 分页查询参数
     * @return 小说章节发布列表
     */
    RestResp<PageRespDto<BookChapterRespDto>> listBookChapters(Long bookId, PageReqDto dto);

     /**
     * 小说章节删除接口
     * @param chapterId 章节ID
     * @return 无
     */
    RestResp<Void> deleteBookChapter(Long chapterId);

     /**
     * 小说章节查询接口
     * @param chapterId 章节ID
     * @return 小说章节内容
     */
    RestResp<ChapterContentRespDto> getBookChapter(Long chapterId);

    /**
     * 小说章节修改接口
     * @param chapterId 章节ID
     * @param dto 小说章节修改请求参数
     * @return 无
     */
    RestResp<Void> updateBookChapter(Long chapterId, @Valid ChapterUpdateReqDto dto);

     /**
     * 小说删除接口
     * @param bookId 小说ID
     * @return 无
     */
    RestResp<Void> deleteBook(Long bookId);

     /**
     * 获取用户评论接口
     * @param userId 用户ID
     * @param pageReqDto 分页查询参数
     * @return 用户评论列表
     */
     RestResp<PageRespDto<UserCommentRespDto>> getUserComment(Long userId, PageReqDto pageReqDto);

    /**
     * 获取用户书架列表接口
     * @param userId 用户ID
     * @param pageReqDto 分页查询参数
     * @return 用户书架列表
     */
    RestResp<PageRespDto<UserBookShelfRespDto>> getUserBookShelf(Long userId, PageReqDto pageReqDto);

     /**
     * 检查书籍是否在书架中
     * @param userId 用户ID
     * @param bookId 书籍ID
     * @return 书籍是否在书架中
     */
    RestResp<Boolean> checkBookInShelf(Long userId, Long bookId);

    /**
     * 添加书籍到书架接口
     * @param userId 用户ID
     * @param bookId 书籍ID
     * @return 无
     */
    RestResp<Void> addToBookShelf(Long userId, Long bookId);

    /**
     * 从书架移除书籍接口
     * @param userId 用户ID
     * @param bookId 书籍ID
     * @return 无
     */
    RestResp<Void> removeFromBookShelf(Long userId, Long bookId);
}
