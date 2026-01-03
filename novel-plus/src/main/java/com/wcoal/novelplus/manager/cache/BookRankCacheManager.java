package com.wcoal.novelplus.manager.cache;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wcoal.novelplus.core.common.constant.CacheConsts;
import com.wcoal.novelplus.core.common.constant.DatabaseConsts;
import com.wcoal.novelplus.dao.entity.BookInfo;
import com.wcoal.novelplus.dao.mapper.BookInfoMapper;
import com.wcoal.novelplus.dto.resp.BookRankRespDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 小说排行榜缓存管理器
 * @author wcoal
 * @since  2025/10/2
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class BookRankCacheManager {

    private final BookInfoMapper bookInfoMapper;

    /**
     * 获取小说点击量排行榜
     * @return 小说点击量排行榜
     */
    @Cacheable(cacheManager = CacheConsts.REDIS_CACHE_MANAGER,
    value = CacheConsts.BOOK_VISIT_RANK_CACHE_NAME)
    public List<BookRankRespDto> listVisitRankBooks() {
        QueryWrapper<BookInfo> bookInfoQueryWrapper = new QueryWrapper<>();
        bookInfoQueryWrapper.orderByDesc(DatabaseConsts.BookTable.COLUMN_VISIT_COUNT);
        return listBookRanks(bookInfoQueryWrapper);
    }


    /**
     * 获取小说新书榜
     * @return 小说新书榜
     */
    @Cacheable(cacheManager = CacheConsts.CAFFEINE_CACHE_MANAGER,
            value = CacheConsts.BOOK_NEWEST_RANK_CACHE_NAME)
    public List<BookRankRespDto> listNewestRankBooks() {
        QueryWrapper<BookInfo> bookInfoQueryWrapper = new QueryWrapper<>();
        bookInfoQueryWrapper
                .ge(DatabaseConsts.BookTable.COLUMN_WORD_COUNT, 0)
                .orderByDesc(DatabaseConsts.CommonColumnEnum.CREATE_TIME.getName());
        return listBookRanks(bookInfoQueryWrapper);
    }

     /**
     * 获取小说更新榜
     * @return 小说更新榜
     */
    @Cacheable(cacheManager = CacheConsts.CAFFEINE_CACHE_MANAGER,
            value = CacheConsts.BOOK_UPDATE_RANK_CACHE_NAME)
    public List<BookRankRespDto> listUpdateRankBooks() {
        QueryWrapper<BookInfo> bookInfoQueryWrapper = new QueryWrapper<>();
        bookInfoQueryWrapper
                .gt(DatabaseConsts.BookTable.COLUMN_WORD_COUNT, 0)
                .orderByDesc(DatabaseConsts.CommonColumnEnum.UPDATE_TIME.getName());
        return listBookRanks(bookInfoQueryWrapper);
    }


     /**
      * 获取小说排行榜
      * @param bookInfoQueryWrapper 小说查询条件
      * @return 小说排行榜
      */
    private List<BookRankRespDto> listBookRanks(QueryWrapper<BookInfo> bookInfoQueryWrapper) {
        bookInfoQueryWrapper
                .gt(DatabaseConsts.BookTable.COLUMN_WORD_COUNT, 0)
                .last(DatabaseConsts.SqlEnum.LIMIT_30.getSql());
        return bookInfoMapper.selectList(bookInfoQueryWrapper).stream()
                .map(bookInfo -> {
                    BookRankRespDto bookRankRespDto = new BookRankRespDto();
                    bookRankRespDto.setId(bookInfo.getId());
                    bookRankRespDto.setCategoryId(bookInfo.getCategoryId());
                    bookRankRespDto.setCategoryName(bookInfo.getCategoryName());
                    bookRankRespDto.setPicUrl(bookInfo.getPicUrl());
                    bookRankRespDto.setBookName(bookInfo.getBookName());
                    bookRankRespDto.setAuthorName(bookInfo.getAuthorName());
                    bookRankRespDto.setBookDesc(bookInfo.getBookDesc());
                    bookRankRespDto.setWordCount(bookInfo.getWordCount());
                    bookRankRespDto.setLastChapterName(bookInfo.getLastChapterName());
                    bookRankRespDto.setLastChapterUpdateTime(bookInfo.getLastChapterUpdateTime());
                    return bookRankRespDto;
                }).collect(Collectors.toList());
    }

     /**
      * 清理小说点击量排行榜缓存
      */
     @CacheEvict(cacheManager = CacheConsts.REDIS_CACHE_MANAGER,
             value = CacheConsts.BOOK_VISIT_RANK_CACHE_NAME, allEntries = true)
    public void evictVisitRankCache() {
        log.info("清理小说点击量排行榜缓存");
    }

    /**
     * 清理小说新书榜缓存
     */
    @CacheEvict(cacheManager = CacheConsts.CAFFEINE_CACHE_MANAGER,
            value = CacheConsts.BOOK_NEWEST_RANK_CACHE_NAME, allEntries = true)
    public void evictNewestRankCache() {
        log.info("清理小说新书榜缓存");
        // 使用 allEntries = true 确保整个缓存被清除，而不仅仅是特定条目
        // 因为新书榜是基于创建时间排序的，删除任何一本书都可能影响整个排行榜
    }

    /**
     * 清理小说更新榜缓存
     */
    @CacheEvict(cacheManager = CacheConsts.CAFFEINE_CACHE_MANAGER,
            value = CacheConsts.BOOK_UPDATE_RANK_CACHE_NAME, allEntries = true)
    public void evictUpdateRankCache() {
        log.info("清理小说更新榜缓存");
        // 使用 allEntries = true 确保整个缓存被清除
        // 因为更新榜是基于更新时间排序的，删除任何一本书都可能影响整个排行榜
    }
}
