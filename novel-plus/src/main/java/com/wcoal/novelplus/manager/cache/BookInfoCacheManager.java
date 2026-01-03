package com.wcoal.novelplus.manager.cache;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wcoal.novelplus.core.common.constant.CacheConsts;
import com.wcoal.novelplus.core.common.constant.DatabaseConsts;
import com.wcoal.novelplus.core.utils.BeanUtils;
import com.wcoal.novelplus.dao.entity.BookChapter;
import com.wcoal.novelplus.dao.entity.BookInfo;
import com.wcoal.novelplus.dao.mapper.BookChapterMapper;
import com.wcoal.novelplus.dao.mapper.BookInfoMapper;
import com.wcoal.novelplus.dto.resp.BookInfoRespDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 小说信息缓存管理
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class BookInfoCacheManager {

    private final BookInfoMapper bookInfoMapper;

    private final BookChapterMapper bookChapterMapper;

    //从缓存中获取小说信息，如果缓存中没有，则执行cacheput把小说信息放入缓存
    @Cacheable(cacheManager = CacheConsts.CAFFEINE_CACHE_MANAGER,
    value = CacheConsts.BOOK_INFO_CACHE_NAME)
    public BookInfoRespDto getBookInfo(Long id) {
        return cachePutBookInfo(id);
    }

    @CachePut(cacheManager = CacheConsts.CAFFEINE_CACHE_MANAGER,
    value = CacheConsts.BOOK_INFO_CACHE_NAME)
    public BookInfoRespDto cachePutBookInfo(Long id) {
        //查询基础信息
        BookInfo bookInfo = bookInfoMapper.selectById(id);
        // 检查bookInfo是否为null，避免空指针异常
        if (bookInfo == null) {
            log.warn("未查询到小说信息，id: {}", id);
            return null;
        }
        //查询首章id用于展示基础信息
        QueryWrapper<BookChapter> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(DatabaseConsts.BookChapterTable.COLUMN_BOOK_ID, id)
                .orderByAsc(DatabaseConsts.BookChapterTable.COLUMN_CHAPTER_NUM)
                .last(DatabaseConsts.SqlEnum.LIMIT_1.getSql());
        BookChapter firstBookChapter = bookChapterMapper.selectOne(queryWrapper);
        //封装查询结果为dto
        BookInfoRespDto bookInfoRespDto = BeanUtils.copyBean(bookInfo, BookInfoRespDto.class);
        bookInfoRespDto.setFirstChapterId(firstBookChapter != null ? firstBookChapter.getId() : null);
        bookInfoRespDto.setUpdateTime(bookInfo.getLastChapterUpdateTime());
        return bookInfoRespDto;
    }

    /**
     * 更新小说信息缓存中的点击量
     * @param bookId 小说ID
     * @param visitCount 点击量
     * @return 更新后的小说信息
     */
    @CachePut(cacheManager = CacheConsts.CAFFEINE_CACHE_MANAGER,
            value = CacheConsts.BOOK_INFO_CACHE_NAME, key = "#bookId")
    public BookInfoRespDto updateBookInfoVisitCount(Long bookId, Long visitCount) {
        // 从缓存中获取原有的小说信息
        BookInfoRespDto bookInfoRespDto = getBookInfo(bookId);
        if (bookInfoRespDto != null) {
            // 更新点击量
            bookInfoRespDto.setVisitCount(visitCount);
        }
        return bookInfoRespDto;
    }

    /**
     * 获取分类下最新更新的小说id,并放入缓存中1小时
     * @param categoryId 分类ID
     * @return 最新更新的小说id列表
     */
    @Cacheable(cacheManager = CacheConsts.CAFFEINE_CACHE_MANAGER,
            value = CacheConsts.LAST_UPDATE_BOOK_ID_LIST_CACHE_NAME)
    public List<Long> getLastUpdateBookIds(Long categoryId) {
        QueryWrapper<BookInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(DatabaseConsts.BookTable.COLUMN_CATEGORY_ID, categoryId)
                .gt(DatabaseConsts.BookTable.COLUMN_WORD_COUNT, 0)
                .orderByDesc(DatabaseConsts.BookTable.COLUMN_LAST_CHAPTER_UPDATE_TIME)
                .last(DatabaseConsts.SqlEnum.LIMIT_500.getSql());
        return bookInfoMapper.selectList(queryWrapper).stream()
                .map(BookInfo::getId)
                .collect(Collectors.toList());
    }

    @CacheEvict(cacheManager = CacheConsts.CAFFEINE_CACHE_MANAGER,
            value = CacheConsts.BOOK_INFO_CACHE_NAME)
    public void evictBookInfoCache(Long bookId) {
        // 调用此方法自动清除小说信息的缓存
    }
}
