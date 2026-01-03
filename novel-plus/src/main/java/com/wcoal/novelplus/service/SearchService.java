package com.wcoal.novelplus.service;


import com.wcoal.novelplus.core.common.resp.PageRespDto;
import com.wcoal.novelplus.core.common.resp.RestResp;
import com.wcoal.novelplus.dto.resp.BookInfoRespDto;
import com.wcoal.novelplus.dto.resp.BookSearchReqDto;

/**
 * 搜索 服务类
 *
 * @author wcoal
 * @since 2025/10/3
 */
public interface SearchService {

    /**
     * 小说搜索
     *
     * @param condition 搜索条件
     * @return 搜索结果
     */
    RestResp<PageRespDto<BookInfoRespDto>> searchBooks(BookSearchReqDto condition);

}
