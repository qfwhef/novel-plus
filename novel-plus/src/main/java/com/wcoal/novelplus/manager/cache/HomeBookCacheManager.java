package com.wcoal.novelplus.manager.cache;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wcoal.novelplus.core.common.constant.DatabaseConsts;
import com.wcoal.novelplus.core.utils.CollUtils;
import com.wcoal.novelplus.dao.entity.BookInfo;
import com.wcoal.novelplus.dao.entity.HomeBook;
import com.wcoal.novelplus.dao.mapper.BookInfoMapper;
import com.wcoal.novelplus.dao.mapper.HomeBookMapper;
import com.wcoal.novelplus.dto.resp.HomeBookRespDto;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
@RequiredArgsConstructor
public class HomeBookCacheManager {

    private final HomeBookMapper homeBookMapper;

    private final BookInfoMapper bookInfoMapper;

    /**
     * 获取首页推荐小说
     * @return 首页推荐小说列表
     */
/*    @Cacheable(cacheManager = CacheConsts.CAFFEINE_CACHE_MANAGER,
    value = CacheConsts.HOME_BOOK_CACHE_NAME)*/
    public List<HomeBookRespDto> listHomeBooks() {
        //从首页推荐小说表中查询出推荐的小说
        QueryWrapper<HomeBook> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByAsc(DatabaseConsts.CommonColumnEnum.SORT.getName());
        List<HomeBook> homeBooks = homeBookMapper.selectList(queryWrapper);

        //获取推荐小数id列表
        if (!CollUtils.isEmpty(homeBooks)) {
            List<Long> bookIds = homeBooks.stream().map(HomeBook::getBookId).toList();

            //根据小说id查询小说信息列表
            List<BookInfo> bookInfos = bookInfoMapper.selectBatchIds(bookIds);

            //组装响应dto返回
            if (!CollUtils.isEmpty(bookInfos)){
                Map<Long, BookInfo> bookInfoMap = bookInfos.stream().collect(Collectors.toMap(BookInfo::getId, c -> c));
                return homeBooks.stream().map(v -> {
                    BookInfo bookInfo = bookInfoMap.get(v.getBookId());
                    HomeBookRespDto bookRespDto = new HomeBookRespDto();
                    bookRespDto.setType(v.getType());
                    bookRespDto.setBookId(v.getBookId());
                    bookRespDto.setBookName(bookInfo.getBookName());
                    bookRespDto.setPicUrl(bookInfo.getPicUrl());
                    bookRespDto.setAuthorName(bookInfo.getAuthorName());
                    bookRespDto.setBookDesc(bookInfo.getBookDesc());
                    return bookRespDto;
                }).toList();
            }
        }
        return CollUtils.emptyList();
    }
}
