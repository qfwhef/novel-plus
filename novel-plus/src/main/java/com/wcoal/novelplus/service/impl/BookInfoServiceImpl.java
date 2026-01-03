package com.wcoal.novelplus.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wcoal.novelplus.core.auth.UserContext;
import com.wcoal.novelplus.core.common.constant.DatabaseConsts;
import com.wcoal.novelplus.core.annotation.Key;
import com.wcoal.novelplus.core.annotation.Lock;
import com.wcoal.novelplus.core.common.enums.ErrorCodeEnum;
import com.wcoal.novelplus.core.common.req.PageReqDto;
import com.wcoal.novelplus.core.common.resp.PageRespDto;
import com.wcoal.novelplus.core.common.resp.RestResp;
import com.wcoal.novelplus.core.utils.BeanUtils;
import com.wcoal.novelplus.core.utils.CollUtils;
import com.wcoal.novelplus.dao.entity.*;
import com.wcoal.novelplus.dao.mapper.*;
import com.wcoal.novelplus.dto.AuthorInfoDto;
import com.wcoal.novelplus.dto.req.BookAddReqDto;
import com.wcoal.novelplus.dto.req.ChapterAddReqDto;
import com.wcoal.novelplus.dto.req.ChapterUpdateReqDto;
import com.wcoal.novelplus.dto.req.UserCommentReqDto;
import com.wcoal.novelplus.dto.resp.*;
import com.wcoal.novelplus.manager.cache.*;
import com.wcoal.novelplus.manager.dao.UserDaoManager;
import com.wcoal.novelplus.service.IBookInfoService;
import com.wcoal.novelplus.core.mq.CommentAuditProducer;
import com.wcoal.novelplus.core.mq.BookshelfProgressProducer;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>
 * 小说信息 服务实现类
 * </p>
 *
 * @author wcoal
 * @since 2025-09-30
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BookInfoServiceImpl extends ServiceImpl<BookInfoMapper, BookInfo> implements IBookInfoService {

    private final BookRankCacheManager bookRankCacheManager;

    private final BookCategoryCacheManager bookCategoryCacheManager;

    private final BookInfoCacheManager bookInfoCacheManager;

    private final BookInfoMapper bookInfoMapper;

    private final BookChapterCacheManager bookChapterCacheManager;

    private final BookChapterMapper bookChapterMapper;

    private final BookContentCacheManager bookContentCacheManager;

    private final BookCommentMapper bookCommentMapper;

    private final BookContentMapper bookContentMapper;

    private final AuthorInfoCacheManager authorInfoCacheManager;

    private static final Integer REC_BOOK_COUNT = 4;

    private final UserDaoManager userDaoManager;

    private final UserBookshelfMapper userBookshelfMapper;

    private final CommentAuditProducer commentAuditProducer;

    private final BookshelfProgressProducer bookshelfProgressProducer;

    private final CommentLikeCacheManager commentLikeCacheManager;

    private final com.wcoal.novelplus.service.IUserBookshelfService userBookshelfService;

    private final com.wcoal.novelplus.manager.redis.ReadProgressManager readProgressManager;

    /**
     * 获取小说分类列表
     *
     * @param workDirection 作品方向
     * @return 小说分类列表
     */
    @Override
    public RestResp<List<BookCategoryRespDto>> listCategories(Integer workDirection) {
        return RestResp.ok(bookCategoryCacheManager.list(workDirection));
    }

    /**
     * 获取小说详情
     *
     * @param bookId 小说ID
     * @return 小说详情
     */
    @Override
    public RestResp<BookInfoRespDto> getBookInfo(Long bookId) {
        return RestResp.ok(bookInfoCacheManager.getBookInfo(bookId));
    }

    /**
     * 增加小说点击量
     *
     * @param bookId 小说ID
     * @return 无
     */
    @Override
    public RestResp<Void> addVisitCount(Long bookId) {
        // 更新数据库中的点击量
        bookInfoMapper.addVisitCount(bookId);

        // 更新缓存中的点击量
        BookInfoRespDto bookInfoRespDto = bookInfoCacheManager.getBookInfo(bookId);
        if (bookInfoRespDto != null) {
            // 从数据库重新获取最新的点击量
            BookInfo bookInfo = bookInfoMapper.selectById(bookId);
            if (bookInfo != null) {
                // 更新缓存中的点击量
                bookInfoRespDto.setVisitCount(bookInfo.getVisitCount());
                // 使用CachePut更新缓存
                bookInfoCacheManager.updateBookInfoVisitCount(bookId, bookInfo.getVisitCount());
            }
        }

        return RestResp.ok();
    }

    /**
     * 获取小说最新章节相关信息
     *
     * @param bookId 小说ID
     * @return 小说最新章节相关信息
     */
    @Override
    public RestResp<BookChapterAboutRespDto> getLastChapterAbout(Long bookId) {
        // 查询小说信息
        BookInfoRespDto bookInfo = bookInfoCacheManager.getBookInfo(bookId);

        // 查询最新章节信息
        BookChapterRespDto bookChapter = bookChapterCacheManager.getChapter(
                bookInfo.getLastChapterId());

        // 查询章节内容
        String content = bookContentCacheManager.getBookContent(bookInfo.getLastChapterId());

        // 查询章节总数
        QueryWrapper<BookChapter> chapterQueryWrapper = new QueryWrapper<>();
        chapterQueryWrapper.eq(DatabaseConsts.BookChapterTable.COLUMN_BOOK_ID, bookId);
        Long chapterTotal = bookChapterMapper.selectCount(chapterQueryWrapper);

        // 组装数据并返回
        return RestResp.ok(BookChapterAboutRespDto.builder()
                .chapterInfo(bookChapter)
                .chapterTotal(chapterTotal)
                .contentSummary(content.substring(0, 30))
                .build());
    }

    /**
     * 小说推荐列表查询接口
     *
     * @param bookId 小说ID
     * @return 小说推荐列表
     */
    @Override
    public RestResp<List<BookInfoRespDto>> listRecBooks(Long bookId) {
        //查询分类id和每个类别下最新更新的小说id
        Long categoryId = bookInfoCacheManager.getBookInfo(bookId).getCategoryId();
        List<Long> lastUpdateBookIds = bookInfoCacheManager.getLastUpdateBookIds(categoryId);//获取分类下最新更新的小说id列表
        ArrayList<BookInfoRespDto> respDtoList = new ArrayList<>();//推荐列表
        ArrayList<Integer> recBookIdIndex = new ArrayList<>();//推荐列表索引
        int count = 0;//推荐列表计数器
        // 使用 ThreadLocalRandom 替代 SecureRandom.getInstanceStrong() 避免生产环境阻塞问题
        Random rand = new Random();
        while (count < REC_BOOK_COUNT) {
            int recIdIndex = rand.nextInt(lastUpdateBookIds.size());
            if (!recBookIdIndex.contains(recIdIndex)) {
                recBookIdIndex.add(recIdIndex);
                bookId = lastUpdateBookIds.get(recIdIndex);
                BookInfoRespDto bookInfo = bookInfoCacheManager.getBookInfo(bookId);
                respDtoList.add(bookInfo);
                count++;
            }
        }

        return RestResp.ok(respDtoList);
    }

    /**
     * 小说章节列表查询接口
     *
     * @param bookId 小说ID
     * @return 小说章节列表
     */
    @Override
    public RestResp<List<BookChapterRespDto>> listChapters(Long bookId) {
        QueryWrapper<BookChapter> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(DatabaseConsts.BookChapterTable.COLUMN_BOOK_ID, bookId)
                .orderByAsc(DatabaseConsts.BookChapterTable.COLUMN_CHAPTER_NUM);
        return RestResp.ok(bookChapterMapper.selectList(queryWrapper).stream()
                .map(v -> BookChapterRespDto.builder()
                        .id(v.getId())
                        .chapterName(v.getChapterName())
                        .isVip(v.getIsVip())
                        .build()).toList());
    }

    /**
     * 小说内容相关信息查询接口
     *
     * @param chapterId 章节ID
     * @return 小说内容相关信息
     */
    @Override
    public RestResp<BookContentAboutRespDto> getBookContentAbout(Long chapterId) {
        //查询章节信息和内容
        BookChapterRespDto bookChapter = bookChapterCacheManager.getChapter(chapterId);
        String bookContent = bookContentCacheManager.getBookContent(chapterId);
        //查询小说信息
        BookInfoRespDto bookInfo = bookInfoCacheManager.getBookInfo(bookChapter.getBookId());

        // 更新用户书架阅读进度（如果用户已登录且书籍在书架中）
        bookshelfProgressProducer.sendProgressUpdate(UserContext.getUserId(), bookChapter.getBookId(), chapterId);

        //组装数据并返回
        return RestResp.ok(BookContentAboutRespDto.builder()
                .chapterInfo(bookChapter)
                .bookContent(bookContent)
                .bookInfo(bookInfo)
                .build());
    }

    @Override
    public RestResp<Long> getPreChapterId(Long chapterId) {
        // 查询小说ID 和 章节号
        BookChapterRespDto chapter = bookChapterCacheManager.getChapter(chapterId);
        Long bookId = chapter.getBookId();
        Integer chapterNum = chapter.getChapterNum();

        // 查询上一章信息并返回章节ID
        QueryWrapper<BookChapter> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(DatabaseConsts.BookChapterTable.COLUMN_BOOK_ID, bookId)
                .lt(DatabaseConsts.BookChapterTable.COLUMN_CHAPTER_NUM, chapterNum)
                .orderByDesc(DatabaseConsts.BookChapterTable.COLUMN_CHAPTER_NUM)
                .last(DatabaseConsts.SqlEnum.LIMIT_1.getSql());
        return RestResp.ok(
                Optional.ofNullable(bookChapterMapper.selectOne(queryWrapper))
                        .map(BookChapter::getId)
                        .orElse(null)
        );
    }

    @Override
    public RestResp<Long> getNextChapterId(Long chapterId) {
        // 查询小说ID 和 章节号
        BookChapterRespDto chapter = bookChapterCacheManager.getChapter(chapterId);
        Long bookId = chapter.getBookId();
        Integer chapterNum = chapter.getChapterNum();

        // 查询下一章信息并返回章节ID
        QueryWrapper<BookChapter> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(DatabaseConsts.BookChapterTable.COLUMN_BOOK_ID, bookId)
                .gt(DatabaseConsts.BookChapterTable.COLUMN_CHAPTER_NUM, chapterNum)
                .orderByAsc(DatabaseConsts.BookChapterTable.COLUMN_CHAPTER_NUM)
                .last(DatabaseConsts.SqlEnum.LIMIT_1.getSql());
        return RestResp.ok(
                Optional.ofNullable(bookChapterMapper.selectOne(queryWrapper))
                        .map(BookChapter::getId)
                        .orElse(null)
        );
    }

    /**
     * 小说点击榜查询接口
     *
     * @return 小说点击榜
     */
    @Override
    public RestResp<List<BookRankRespDto>> listVisitRankBooks() {
        return RestResp.ok(bookRankCacheManager.listVisitRankBooks());
    }

    /**
     * 小说新书榜查询接口
     *
     * @return 小说新书榜
     */
    @Override
    public RestResp<List<BookRankRespDto>> listNewestRankBooks() {
        return RestResp.ok(bookRankCacheManager.listNewestRankBooks());
    }

    /**
     * 小说更新榜查询接口
     *
     * @return 小说更新榜
     */
    @Override
    public RestResp<List<BookRankRespDto>> listUpdateRankBooks() {
        return RestResp.ok(bookRankCacheManager.listUpdateRankBooks());
    }

    /**
     * 小说最新评论查询接口
     *
     * @param bookId 小说ID
     * @return 小说最新评论
     */
    @Override
    public RestResp<BookCommentRespDto> listNewestComments(Long bookId) {
        //查询评论总数
        QueryWrapper<BookComment> bookCommentCountQueryWrapper = new QueryWrapper<>();
        bookCommentCountQueryWrapper.eq(DatabaseConsts.BookCommentTable.COLUMN_BOOK_ID, bookId)
                .eq(DatabaseConsts.BookCommentTable.AUDIT_STATUS_PENDING, DatabaseConsts.BookCommentTable.AUDIT_PASS);
        Long commentCount = bookCommentMapper.selectCount(bookCommentCountQueryWrapper);
        BookCommentRespDto bookCommentRespDto = BookCommentRespDto.builder().commentTotal(commentCount).build();//构建返回对象

        if (commentCount > 0) {
            //查询最新评论列表
            QueryWrapper<BookComment> commentQueryWrapper = new QueryWrapper<>();
            commentQueryWrapper.eq(DatabaseConsts.BookCommentTable.COLUMN_BOOK_ID, bookId)
                    .orderByDesc(DatabaseConsts.CommonColumnEnum.CREATE_TIME.getName())
                    .last(DatabaseConsts.SqlEnum.LIMIT_5.getSql());
            List<BookComment> bookComments = bookCommentMapper.selectList(commentQueryWrapper);

            //查询评论用户信息，设置需要返回的用户信息
            List<Long> userList = bookComments.stream().map(BookComment::getUserId).toList();
            List<UserInfo> userInfos = userDaoManager.listUserInfos(userList);
            Map<Long, UserInfo> userInfoMap = userInfos.stream()
                    .collect(Collectors.toMap(UserInfo::getId, Function.identity()));

            // 记录数据一致性检查日志
            Set<Long> uniqueUserIds = new HashSet<>(userList);
            Set<Long> foundUserIds = userInfos.stream().map(UserInfo::getId).collect(Collectors.toSet());
            if (!uniqueUserIds.equals(foundUserIds)) {
                Set<Long> missingUserIds = new HashSet<>(uniqueUserIds);
                missingUserIds.removeAll(foundUserIds);
                log.warn("评论用户信息不完整，书籍ID: " + bookId + ", 缺失的用户ID: " + missingUserIds);
            }

            if (CollUtils.isNotEmpty(userInfos)) {
                List<BookCommentRespDto.CommentInfo> commentInfos = bookComments.stream()
                        .filter(bookComment -> userInfoMap.containsKey(bookComment.getUserId())) // 过滤掉用户信息不存在的评论
                        .map(bookComment -> {
                            UserInfo userInfo = userInfoMap.get(bookComment.getUserId());
                            // 从 Redis 获取最新的点赞数量
                            Integer likeCount = commentLikeCacheManager.getLikeCount(bookComment.getId());
                            return BookCommentRespDto.CommentInfo.builder()
                                    .id(bookComment.getId())
                                    .commentUserId(bookComment.getUserId())
                                    .commentUser(userInfo.getUserName())
                                    .commentUserPhoto(userInfo.getUserPhoto())
                                    .commentContent(bookComment.getCommentContent())
                                    .commentTime(bookComment.getCreateTime())
                                    .replyCount(bookComment.getReplyCount())
                                    .likeCount(likeCount)  // 使用从 Redis 获取的点赞数量
                                    .build();
                        }).toList();
                bookCommentRespDto.setComments(commentInfos);
            } else {
                bookCommentRespDto.setComments(CollUtils.emptyList());
            }
        } else {
            bookCommentRespDto.setComments(CollUtils.emptyList());
        }
        return RestResp.ok(bookCommentRespDto);
    }

    /**
     * 用户发表评论接口
     * <p>
     * 优化说明：
     * 1. 旧版本：评论直接审核通过（auditStatus默认为1）
     * 2. 新版本：评论先进入待审核状态（auditStatus=0），通过RabbitMQ异步审核
     * <p>
     * 技术亮点：
     * - 使用消息队列实现异步审核，提升接口响应速度
     * - 解耦审核逻辑，便于后续扩展（接入第三方审核API、AI审核等）
     * - 通过死信队列处理审核失败场景，保证消息可靠性
     *
     * @param dto 用户评论请求参数
     * @return 无
     */
    @Override
    @Lock
    public RestResp<Void> userComment(@Key(expr = "#{userId + '::' + bookId}") UserCommentReqDto dto) {//根据用户ID和小说ID(在dto对象中)生成锁Key
        QueryWrapper<BookComment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(DatabaseConsts.BookCommentTable.COLUMN_USER_ID, dto.getUserId())
                .eq(DatabaseConsts.BookCommentTable.COLUMN_BOOK_ID, dto.getBookId());
        Long commentCount = bookCommentMapper.selectCount(queryWrapper);
        if (commentCount > 0) {
            return RestResp.fail(ErrorCodeEnum.USER_COMMENTED);
        }

        // ========== 旧版本代码（同步审核，已废弃） ==========
        // 旧版本：评论直接插入数据库，默认审核通过
        // 问题：审核逻辑阻塞接口响应，无法扩展复杂审核规则
        /*
        BookComment bookComment = BeanUtils.copyBean(dto, BookComment.class);
        bookComment.setCreateTime(LocalDateTime.now());
        bookComment.setUpdateTime(LocalDateTime.now());
        bookCommentMapper.insert(bookComment);
        */

        // ========== 新版本代码（异步审核） ==========
        // 新版本：评论先进入待审核状态，通过MQ异步处理
        BookComment bookComment = BeanUtils.copyBean(dto, BookComment.class);
        bookComment.setAuditStatus(0); // 设置为待审核状态
        bookComment.setCreateTime(LocalDateTime.now());
        bookComment.setUpdateTime(LocalDateTime.now());
        bookCommentMapper.insert(bookComment);

        // 发送评论审核消息到 RabbitMQ
        commentAuditProducer.sendCommentAuditMessage(
                bookComment.getId(),
                dto.getBookId(),
                dto.getUserId(),
                dto.getCommentContent()
        );

        return RestResp.ok();
    }

    /**
     * 修改评论接口
     *
     * @param userId  用户ID
     * @param id      评论ID
     * @param content 评论内容
     * @return 无
     */
    @Override
    public RestResp<Void> updateComment(Long userId, Long id, String content) {
        QueryWrapper<BookComment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(DatabaseConsts.CommonColumnEnum.ID.getName(), id)
                .eq(DatabaseConsts.BookCommentTable.COLUMN_USER_ID, userId);
        BookComment bookComment = bookCommentMapper.selectOne(queryWrapper);
        bookComment.setCommentContent(content);
        bookCommentMapper.update(bookComment, queryWrapper);

        //发送评论修改消息到 RabbitMQ
        commentAuditProducer.sendCommentAuditMessage(
                bookComment.getId(),
                bookComment.getBookId(),
                bookComment.getUserId(),
                content
        );

        return RestResp.ok();
    }


    /**
     * 删除评论接口
     *
     * @param userId    用户ID
     * @param commentId 评论ID
     * @return 无
     */
    @Override
    public RestResp<Void> deleteComment(Long userId, Long commentId) {
        QueryWrapper<BookComment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(DatabaseConsts.CommonColumnEnum.ID.getName(), commentId)
                .eq(DatabaseConsts.BookCommentTable.COLUMN_USER_ID, userId);
        bookCommentMapper.delete(queryWrapper);
        return RestResp.ok();
    }

    /**
     * 发布小说接口
     *
     * @param dto 发布小说请求参数
     * @return 无
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public RestResp<Void> saveBook(BookAddReqDto dto) {
        // 校验小说名是否已存在
        QueryWrapper<BookInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(DatabaseConsts.BookTable.COLUMN_BOOK_NAME, dto.getBookName());
        if (bookInfoMapper.selectCount(queryWrapper) > 0) {
            return RestResp.fail(ErrorCodeEnum.AUTHOR_BOOK_NAME_EXIST);
        }
        // 设置作家信息
        AuthorInfoDto author = authorInfoCacheManager.getAuthor(UserContext.getUserId());
        BookInfo bookInfo = BeanUtils.copyBean(dto, BookInfo.class);
        if (bookInfo.getPicUrl() == null) {
            bookInfo.setPicUrl(DatabaseConsts.DEFAULT_PICURL);
        }
        bookInfo.setAuthorId(author.getId());
        bookInfo.setAuthorName(author.getPenName());
        bookInfo.setScore(0);
        bookInfo.setWordCount(0);  // 初始化字数为0
        bookInfo.setVisitCount(0L);  // 初始化点击量为0
        bookInfo.setCommentCount(0);  // 初始化评论数为0
        bookInfo.setCreateTime(LocalDateTime.now());
        bookInfo.setUpdateTime(LocalDateTime.now());
        bookInfoMapper.insert(bookInfo);
        return RestResp.ok();
    }

    /**
     * 作者查询自己发布的小说接口
     *
     * @param dto 分页查询参数
     * @return 作者发布的小说列表
     */
    @Override
    public RestResp<PageRespDto<BookInfoRespDto>> listAuthorBooks(PageReqDto dto) {
        IPage<BookInfo> page = new Page<>();
        page.setCurrent(dto.getPageNum());
        page.setSize(dto.getPageSize());
        QueryWrapper<BookInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(DatabaseConsts.BookTable.AUTHOR_ID, UserContext.getAuthorId())
                .orderByDesc(DatabaseConsts.CommonColumnEnum.CREATE_TIME.getName());
        IPage<BookInfo> bookInfoPage = bookInfoMapper.selectPage(page, queryWrapper);
        return RestResp.ok(PageRespDto.of(dto.getPageNum(), dto.getPageSize(), page.getTotal(),
                bookInfoPage.getRecords().stream().map(bookInfo -> BookInfoRespDto.builder()
                        .id(bookInfo.getId())
                        .bookName(bookInfo.getBookName())
                        .picUrl(bookInfo.getPicUrl())
                        .categoryName(bookInfo.getCategoryName())
                        .wordCount(bookInfo.getWordCount())
                        .visitCount(bookInfo.getVisitCount())
                        .updateTime(bookInfo.getUpdateTime())
                        .build()).toList()));
    }

    /**
     * 作者发布小说章节接口
     *
     * @param dto 发布小说章节请求参数
     * @return 无
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public RestResp<Void> saveBookChapter(ChapterAddReqDto dto) {
        //校验小数是否属于当前作家
        BookInfo bookInfo = bookInfoMapper.selectById(dto.getBookId());
        if (!bookInfo.getAuthorId().equals(UserContext.getAuthorId())) {
            return RestResp.fail(ErrorCodeEnum.USER_UN_AUTH);
        }
        //保存小说章节相关信息到章节表
        //更新最新章节信息
        int chapterNum = 0;
        QueryWrapper<BookChapter> chapterQueryWrapper = new QueryWrapper<>();
        chapterQueryWrapper.eq(DatabaseConsts.BookChapterTable.COLUMN_BOOK_ID, dto.getBookId())
                .orderByDesc(DatabaseConsts.BookChapterTable.COLUMN_CHAPTER_NUM)
                .last(DatabaseConsts.SqlEnum.LIMIT_1.getSql());
        BookChapter bookChapter = bookChapterMapper.selectOne(chapterQueryWrapper);
        if (bookChapter != null) {
            chapterNum = bookChapter.getChapterNum() + 1;
        } else {
            //清除小说新书榜缓存
            bookRankCacheManager.evictNewestRankCache();
        }
        //设置章节相关信息并保存
        BookChapter newBookChapter = BeanUtils.copyBean(dto, BookChapter.class);
        newBookChapter.setChapterNum(chapterNum);
        newBookChapter.setWordCount(dto.getChapterContent().length());
        newBookChapter.setCreateTime(LocalDateTime.now());
        newBookChapter.setUpdateTime(LocalDateTime.now());
        bookChapterMapper.insert(newBookChapter);

        //保存章节内容到小说内容表
        BookContent bookContent = new BookContent();
        bookContent.setContent(dto.getChapterContent());
        bookContent.setChapterId(newBookChapter.getId());
        bookContent.setCreateTime(LocalDateTime.now());
        bookContent.setUpdateTime(LocalDateTime.now());
        bookContentMapper.insert(bookContent);

        //更新小说表最新章节信息和小说总字数信息
        //更新小说表关于最新章节的信息
        BookInfo newBookInfo = new BookInfo();
        newBookInfo.setId(dto.getBookId());
        newBookInfo.setLastChapterId(newBookChapter.getId());
        newBookInfo.setLastChapterName(newBookChapter.getChapterName());
        newBookInfo.setLastChapterUpdateTime(LocalDateTime.now());
        newBookInfo.setWordCount(bookInfo.getWordCount() + newBookChapter.getWordCount());
        newBookChapter.setUpdateTime(LocalDateTime.now());
        bookInfoMapper.updateById(newBookInfo);
        //清除小说信息缓存
        bookInfoCacheManager.evictBookInfoCache(dto.getBookId());
        //清除小说更新榜缓存
        bookRankCacheManager.evictUpdateRankCache();
        //发送小说信息更新的 MQ 消息
//        amqpMsgManager.sendBookChangeMsg(dto.getBookId());
        return RestResp.ok();
    }

    /**
     * 小说章节发布列表查询接口
     *
     * @param bookId 小说ID
     * @param dto    分页查询参数
     * @return 小说章节发布列表
     */
    @Override
    public RestResp<PageRespDto<BookChapterRespDto>> listBookChapters(Long bookId, PageReqDto dto) {
        IPage<BookChapter> page = new Page<>();
        page.setCurrent(dto.getPageNum());
        page.setSize(dto.getPageSize());
        QueryWrapper<BookChapter> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(DatabaseConsts.BookChapterTable.COLUMN_BOOK_ID, bookId)
                .orderByDesc(DatabaseConsts.BookChapterTable.COLUMN_CHAPTER_NUM);
        IPage<BookChapter> bookChapterPage = bookChapterMapper.selectPage(page, queryWrapper);
        return RestResp.ok(PageRespDto.of(dto.getPageNum(), dto.getPageSize(), page.getTotal(),
                bookChapterPage.getRecords().stream().map(bookChapter -> BookChapterRespDto.builder()
                        .id(bookChapter.getId())
                        .chapterName(bookChapter.getChapterName())
                        .chapterUpdateTime(bookChapter.getUpdateTime())
                        .isVip(bookChapter.getIsVip())
                        .build()).toList()));
    }

    /**
     * 小说章节删除接口
     *
     * @param chapterId 章节ID
     * @return 无
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public RestResp<Void> deleteBookChapter(Long chapterId) {
        // 查询章节信息
        BookChapterRespDto chapter = bookChapterCacheManager.getChapter(chapterId);
        // 查询小说信息
        BookInfoRespDto bookInfo = bookInfoCacheManager.getBookInfo(chapter.getBookId());
        // 删除章节信息
        bookChapterMapper.deleteById(chapterId);
        // 删除章节内容
        QueryWrapper<BookContent> bookContentQueryWrapper = new QueryWrapper<>();
        bookContentQueryWrapper.eq(DatabaseConsts.BookContentTable.COLUMN_CHAPTER_ID, chapterId);
        bookContentMapper.delete(bookContentQueryWrapper);
        // 更新小说信息
        BookInfo newBookInfo = new BookInfo();
        newBookInfo.setId(chapter.getBookId());
        newBookInfo.setUpdateTime(LocalDateTime.now());
        newBookInfo.setWordCount(bookInfo.getWordCount() - chapter.getChapterWordCount());
        if (Objects.equals(bookInfo.getLastChapterId(), chapterId)) {
            // 设置最新章节信息
            QueryWrapper<BookChapter> bookChapterQueryWrapper = new QueryWrapper<>();
            bookChapterQueryWrapper.eq(DatabaseConsts.BookChapterTable.COLUMN_BOOK_ID, chapter.getBookId())
                    .orderByDesc(DatabaseConsts.BookChapterTable.COLUMN_CHAPTER_NUM)
                    .last(DatabaseConsts.SqlEnum.LIMIT_1.getSql());
            BookChapter bookChapter = bookChapterMapper.selectOne(bookChapterQueryWrapper);
            Long lastChapterId = 0L;
            String lastChapterName = "";
            LocalDateTime lastChapterUpdateTime = null;
            if (Objects.nonNull(bookChapter)) {
                lastChapterId = bookChapter.getId();
                lastChapterName = bookChapter.getChapterName();
                lastChapterUpdateTime = bookChapter.getUpdateTime();
            }
            newBookInfo.setLastChapterId(lastChapterId);
            newBookInfo.setLastChapterName(lastChapterName);
            newBookInfo.setLastChapterUpdateTime(lastChapterUpdateTime);
        }
        bookInfoMapper.updateById(newBookInfo);
        // 清理章节信息缓存
        bookChapterCacheManager.evictBookChapterCache(chapterId);
        // 清理章节内容缓存
        bookContentCacheManager.evictBookContentCache(chapterId);
        // 清理小说信息缓存
        bookInfoCacheManager.evictBookInfoCache(chapter.getBookId());
        //清理小说更新榜缓存
        bookRankCacheManager.evictUpdateRankCache();
        // 发送小说信息更新的 MQ 消息
//        amqpMsgManager.sendBookChangeMsg(chapter.getBookId());
        return RestResp.ok();
    }

    /**
     * 小说章节查询接口
     *
     * @param chapterId 章节ID
     * @return 小说章节内容
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public RestResp<ChapterContentRespDto> getBookChapter(Long chapterId) {
        BookChapterRespDto chapter = bookChapterCacheManager.getChapter(chapterId);
        String bookContent = bookContentCacheManager.getBookContent(chapterId);
        return RestResp.ok(
                ChapterContentRespDto.builder()
                        .chapterName(chapter.getChapterName())
                        .chapterContent(bookContent)
                        .isVip(chapter.getIsVip())
                        .build());
    }

    /**
     * 小说章节修改接口
     *
     * @param chapterId 章节ID
     * @param dto       小说章节修改请求参数
     * @return 无
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public RestResp<Void> updateBookChapter(Long chapterId, ChapterUpdateReqDto dto) {
        // 1.查询章节信息
        BookChapterRespDto chapter = bookChapterCacheManager.getChapter(chapterId);
        // 2.查询小说信息
        BookInfoRespDto bookInfo = bookInfoCacheManager.getBookInfo(chapter.getBookId());
        // 3.更新章节信息
        BookChapter newChapter = new BookChapter();
        newChapter.setId(chapterId);
        newChapter.setChapterName(dto.getChapterName());
        newChapter.setWordCount(dto.getChapterContent().length());
        newChapter.setIsVip(dto.getIsVip());
        newChapter.setUpdateTime(LocalDateTime.now());
        bookChapterMapper.updateById(newChapter);
        // 4.更新章节内容
        BookContent newContent = new BookContent();
        newContent.setContent(dto.getChapterContent());
        newContent.setUpdateTime(LocalDateTime.now());
        QueryWrapper<BookContent> bookContentQueryWrapper = new QueryWrapper<>();
        bookContentQueryWrapper.eq(DatabaseConsts.BookContentTable.COLUMN_CHAPTER_ID, chapterId);
        bookContentMapper.update(newContent, bookContentQueryWrapper);
        // 5.更新小说信息
        BookInfo newBookInfo = new BookInfo();
        newBookInfo.setId(chapter.getBookId());
        newBookInfo.setUpdateTime(LocalDateTime.now());
        newBookInfo.setWordCount(
                bookInfo.getWordCount() - chapter.getChapterWordCount() + dto.getChapterContent().length());
        if (Objects.equals(bookInfo.getLastChapterId(), chapterId)) {
            // 更新最新章节信息
            newBookInfo.setLastChapterName(dto.getChapterName());
            newBookInfo.setLastChapterUpdateTime(LocalDateTime.now());
        }
        bookInfoMapper.updateById(newBookInfo);
        // 6.清理章节信息缓存
        bookChapterCacheManager.evictBookChapterCache(chapterId);
        // 7.清理章节内容缓存
        bookContentCacheManager.evictBookContentCache(chapterId);
        // 8.清理小说信息缓存
        bookInfoCacheManager.evictBookInfoCache(chapter.getBookId());
        // 9.发送小说信息更新的 MQ 消息
//        amqpMsgManager.sendBookChangeMsg(chapter.getBookId());
        //清理小说更新榜缓存
        bookRankCacheManager.evictUpdateRankCache();
        return RestResp.ok();
    }

    /**
     * 小说删除接口
     *
     * @param bookId 小说ID
     * @return 无
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public RestResp<Void> deleteBook(Long bookId) {
        //权限验证 - 确保只有小说作者可以删除
        BookInfo bookInfo = bookInfoMapper.selectById(bookId);
        if (Objects.isNull(bookInfo)) {
            return RestResp.fail(ErrorCodeEnum.AUTHOR_BOOK_NOT_EXIST);
        }
        if (!Objects.equals(bookInfo.getAuthorId(), UserContext.getAuthorId())) {
            return RestResp.fail(ErrorCodeEnum.USER_UN_AUTH);
        }

        //删除小说评论
        QueryWrapper<BookComment> bookCommentQueryWrapper = new QueryWrapper<>();
        bookCommentQueryWrapper.eq(DatabaseConsts.BookCommentTable.COLUMN_BOOK_ID, bookId);
        bookCommentMapper.delete(bookCommentQueryWrapper);

        //删除所有章节内容和章节信息
        QueryWrapper<BookChapter> bookChapterQueryWrapper = new QueryWrapper<>();
        bookChapterQueryWrapper.eq(DatabaseConsts.BookChapterTable.COLUMN_BOOK_ID, bookId);
        List<BookChapter> bookChapters = bookChapterMapper.selectList(bookChapterQueryWrapper);

        if (!CollectionUtils.isEmpty(bookChapters)) {
            //删除章节内容
            List<Long> chapterIds = bookChapters.stream().map(BookChapter::getId).toList();
            QueryWrapper<BookContent> bookContentQueryWrapper = new QueryWrapper<>();
            bookContentQueryWrapper.in(DatabaseConsts.BookContentTable.COLUMN_CHAPTER_ID, chapterIds);
            bookContentMapper.delete(bookContentQueryWrapper);

            //删除章节缓存
            for (Long chapterId : chapterIds) {
                bookChapterCacheManager.evictBookChapterCache(chapterId);
                bookContentCacheManager.evictBookContentCache(chapterId);
            }
        }

        //删除章节信息
        bookChapterMapper.delete(bookChapterQueryWrapper);

        //删除小说信息
        bookInfoMapper.deleteById(bookId);

        //清理小说信息缓存
        bookInfoCacheManager.evictBookInfoCache(bookId);

        //清理排行榜缓存
        bookRankCacheManager.evictVisitRankCache();
        bookRankCacheManager.evictNewestRankCache();
        bookRankCacheManager.evictUpdateRankCache();

        //发送小说信息更新的 MQ 消息
//        amqpMsgManager.sendBookChangeMsg(bookId);

        return RestResp.ok();
    }

    /**
     * 获取用户评论接口
     *
     * @param userId     用户ID
     * @param pageReqDto 分页查询参数
     * @return 用户评论列表
     */
    @Override
    public RestResp<PageRespDto<UserCommentRespDto>> getUserComment(Long userId, PageReqDto pageReqDto) {
        //获取分页参数
        IPage<BookComment> page = new Page<>();
        page.setCurrent(pageReqDto.getPageNum());
        page.setSize(pageReqDto.getPageSize());

        //查询用户评论
        QueryWrapper<BookComment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(DatabaseConsts.BookCommentTable.COLUMN_USER_ID, userId)
                .orderByDesc(DatabaseConsts.CommonColumnEnum.UPDATE_TIME.getName());
        IPage<BookComment> bookCommentIPage = bookCommentMapper.selectPage(page, queryWrapper);
        List<BookComment> records = bookCommentIPage.getRecords();
        if (!CollUtils.isEmpty(records)) {
            List<Long> bookIdList = records.stream().map(BookComment::getBookId).toList();
            QueryWrapper<BookInfo> bookInfoQueryWrapper = new QueryWrapper<>();
            bookInfoQueryWrapper.in(DatabaseConsts.CommonColumnEnum.ID.getName(), bookIdList);
            Map<Long, BookInfo> bookInfoMap = bookInfoMapper.selectList(bookInfoQueryWrapper).stream()
                    .collect(Collectors.toMap(BookInfo::getId, Function.identity()));
            return RestResp.ok(PageRespDto.of(pageReqDto.getPageNum(), pageReqDto.getPageSize(), page.getTotal(),
                    records.stream().map(BookComment ->
                            UserCommentRespDto.builder()
                                    .commentContent(BookComment.getCommentContent())
                                    .commentBook(bookInfoMap.get(BookComment.getBookId()).getBookName())
                                    .commentBookPic(bookInfoMap.get(BookComment.getBookId()).getPicUrl())
                                    .commentTime(BookComment.getUpdateTime())
                                    .build()).toList()));
        }
        return RestResp.ok(PageRespDto.of(pageReqDto.getPageNum(), pageReqDto.getPageSize(), page.getTotal(), CollUtils.emptyList()));
    }

    /**
     * 获取用户书架列表接口
     *
     * @param userId     用户ID
     * @param pageReqDto 分页查询参数
     * @return 用户书架列表
     */
    @Override
    public RestResp<PageRespDto<UserBookShelfRespDto>> getUserBookShelf(Long userId, PageReqDto pageReqDto) {
        // 1. 构建分页对象
        IPage<UserBookShelf> page = new Page<>(pageReqDto.getPageNum(), pageReqDto.getPageSize());
        
        // 2. 分页查询用户书架
        QueryWrapper<UserBookShelf> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(DatabaseConsts.UserBookshelfTable.COLUMN_USER_ID, userId)
                .orderByDesc(DatabaseConsts.CommonColumnEnum.UPDATE_TIME.getName());
        
        IPage<UserBookShelf> userBookShelfPage = userBookshelfMapper.selectPage(page, queryWrapper);
        List<UserBookShelf> records = userBookShelfPage.getRecords();
        
        // 3. 如果书架为空，直接返回空列表
        if (CollUtils.isEmpty(records)) {
            return RestResp.ok(PageRespDto.of(
                pageReqDto.getPageNum(), 
                pageReqDto.getPageSize(), 
                0L, 
                Collections.emptyList()
            ));
        }
        
        // 4. 构建bookId到UserBookShelf的映射，避免重复查询
        Map<Long, UserBookShelf> bookShelfMap = records.stream()
                .collect(Collectors.toMap(UserBookShelf::getBookId, Function.identity()));
        
        // 5. 批量查询书籍信息并构建响应列表
        List<UserBookShelfRespDto> resultList = new ArrayList<>(records.size());
        
        for (UserBookShelf bookShelf : records) {
            Long bookId = bookShelf.getBookId();
            
            // 从缓存获取书籍信息
            BookInfoRespDto bookInfo = bookInfoCacheManager.getBookInfo(bookId);
            if (bookInfo == null) {
                // 如果书籍不存在，跳过该记录
                continue;
            }
            
            // 优化：优先从 Redis 获取阅读进度，提升查询性能
            Long preContentId = userBookshelfService.getReadProgress(userId, bookId);
            
            // 如果 Redis 和数据库都没有，使用默认值
            if (preContentId == null) {
                preContentId = bookShelf.getPreContentId();
            }
            
            // 构建响应对象
            UserBookShelfRespDto respDto = BeanUtils.copyBean(bookInfo, UserBookShelfRespDto.class);
            respDto.setId(bookShelf.getId());
            respDto.setBookId(bookId);
            respDto.setUserId(userId);
            respDto.setPreContentId(preContentId);
            resultList.add(respDto);
        }
        
        // 6. 返回分页结果
        return RestResp.ok(PageRespDto.of(
            pageReqDto.getPageNum(), 
            pageReqDto.getPageSize(), 
            userBookShelfPage.getTotal(), 
            resultList
        ));
    }

    /**
     * 检查书籍是否在书架中
     * @param userId 用户ID
     * @param bookId 书籍ID
     * @return 书籍是否在书架中
     */
    @Override
    public RestResp<Boolean> checkBookInShelf(Long userId, Long bookId) {
        QueryWrapper<UserBookShelf> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(DatabaseConsts.UserBookshelfTable.COLUMN_USER_ID, userId)
                .eq(DatabaseConsts.UserBookshelfTable.COLUMN_BOOK_ID, bookId);
        return RestResp.ok(userBookshelfMapper.selectOne(queryWrapper) != null);
    }

    /**
     * 添加书籍到书架
     * @param userId 用户ID
     * @param bookId 书籍ID
     * @return 无
     */
    @Override
    public RestResp<Void> addToBookShelf(Long userId, Long bookId) {
        // 1. 检查书籍是否存在
        BookInfoRespDto bookInfo = bookInfoCacheManager.getBookInfo(bookId);
        if (bookInfo == null) {
            return RestResp.fail(ErrorCodeEnum.AUTHOR_BOOK_NOT_EXIST);
        }

        // 2. 检查是否已在书架中
        QueryWrapper<UserBookShelf> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(DatabaseConsts.UserBookshelfTable.COLUMN_USER_ID, userId)
                .eq(DatabaseConsts.UserBookshelfTable.COLUMN_BOOK_ID, bookId);
        
        UserBookShelf existBookShelf = userBookshelfMapper.selectOne(queryWrapper);
        if (existBookShelf != null) {
            return RestResp.fail(ErrorCodeEnum.USER_BOOK_SHELF_EXIST);
        }

        // 3. 添加到书架
        UserBookShelf bookShelf = new UserBookShelf();
        bookShelf.setUserId(userId);
        bookShelf.setBookId(bookId);
        bookShelf.setPreContentId(null);
        bookShelf.setCreateTime(LocalDateTime.now());
        bookShelf.setUpdateTime(LocalDateTime.now());
        
        userBookshelfMapper.insert(bookShelf);
        return RestResp.ok();
    }

    /**
     * 从书架移除书籍
     * @param userId 用户ID
     * @param bookId 书籍ID
     * @return 无
     */
    @Override
    public RestResp<Void> removeFromBookShelf(Long userId, Long bookId) {
        // 1. 查询书架记录
        QueryWrapper<UserBookShelf> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(DatabaseConsts.UserBookshelfTable.COLUMN_USER_ID, userId)
                .eq(DatabaseConsts.UserBookshelfTable.COLUMN_BOOK_ID, bookId);
        
        UserBookShelf bookShelf = userBookshelfMapper.selectOne(queryWrapper);
        if (bookShelf == null) {
            return RestResp.fail(ErrorCodeEnum.USER_BOOK_SHELF_NOT_EXIST);
        }

        // 2. 删除书架记录
        userBookshelfMapper.deleteById(bookShelf.getId());
        
        // 3. 删除 Redis 中的阅读进度缓存
        readProgressManager.deleteProgress(userId, bookId);
        
        return RestResp.ok();
    }
}