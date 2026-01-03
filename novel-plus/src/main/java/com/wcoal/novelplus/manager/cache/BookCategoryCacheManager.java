package com.wcoal.novelplus.manager.cache;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wcoal.novelplus.core.common.constant.CacheConsts;
import com.wcoal.novelplus.core.common.constant.DatabaseConsts;
import com.wcoal.novelplus.dao.entity.BookCategory;
import com.wcoal.novelplus.dao.mapper.BookCategoryMapper;
import com.wcoal.novelplus.dto.resp.BookCategoryRespDto;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 小说分类缓存管理器
 */
@RequiredArgsConstructor
@Component
public class BookCategoryCacheManager {

    private final BookCategoryMapper bookCategoryMapper;

    /**
     * 获取小说分类列表
     *
     * @param workDirection 作品方向
     * @return 小说分类列表
     */
    @Cacheable(cacheManager = CacheConsts.CAFFEINE_CACHE_MANAGER,
            value = CacheConsts.BOOK_CATEGORY_LIST_CACHE_NAME)
    public List<BookCategoryRespDto> list(Integer workDirection) {
        QueryWrapper<BookCategory> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(DatabaseConsts.BookCategoryTable.COLUMN_WORK_DIRECTION, workDirection);
        return bookCategoryMapper.selectList(queryWrapper).stream().map(v ->
            BookCategoryRespDto.builder()
                    .id(v.getId())
                    .name(v.getName())
                    .build()
        ).toList();
    }
}
