package com.wcoal.novelplus.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wcoal.novelplus.core.common.req.PageReqDto;
import com.wcoal.novelplus.core.common.resp.PageRespDto;
import com.wcoal.novelplus.core.common.resp.RestResp;
import com.wcoal.novelplus.core.utils.BeanUtils;
import com.wcoal.novelplus.dao.entity.BookInfo;
import com.wcoal.novelplus.dao.mapper.BookInfoMapper;
import com.wcoal.novelplus.dto.resp.BookInfoRespDto;
import com.wcoal.novelplus.dto.resp.BookSearchReqDto;
import com.wcoal.novelplus.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 数据库搜索服务实现类
 *
 * @author wcoal
 * @since 2025/10/3
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DbSearchServiceImpl implements SearchService {

    private final BookInfoMapper bookInfoMapper;

    @Override
    public RestResp<PageRespDto<BookInfoRespDto>> searchBooks(BookSearchReqDto condition) {
        Page<BookInfoRespDto> page = new Page<>();
        page.setCurrent(condition.getPageNum());//设置当前页码
        page.setSize(condition.getPageSize());//设置每页显示数量
        List<BookInfo> bookInfos = bookInfoMapper.searchBooks(page, condition);
        return RestResp.ok(
                PageRespDto.of(condition.getPageNum(), condition.getPageSize(), page.getTotal(),
                        BeanUtils.copyList(bookInfos, BookInfoRespDto.class)));
    }
}
