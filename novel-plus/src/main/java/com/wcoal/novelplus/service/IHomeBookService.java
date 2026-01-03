package com.wcoal.novelplus.service;

import com.wcoal.novelplus.core.common.resp.RestResp;
import com.wcoal.novelplus.dao.entity.HomeBook;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wcoal.novelplus.dto.resp.HomeBookRespDto;

import java.util.List;

/**
 * <p>
 * 小说推荐 服务类
 * </p>
 *
 * @author wcoal
 * @since 2025-09-30
 */
public interface IHomeBookService extends IService<HomeBook> {

    /**
     * 获取首页推荐小说
     * @return 首页推荐小说列表
     */
    RestResp<List<HomeBookRespDto>> listHomeBooks();

}
