package com.wcoal.novelplus.service.impl;

import com.wcoal.novelplus.core.common.resp.RestResp;
import com.wcoal.novelplus.dao.entity.HomeBook;
import com.wcoal.novelplus.dao.mapper.HomeBookMapper;
import com.wcoal.novelplus.dto.resp.HomeBookRespDto;
import com.wcoal.novelplus.manager.cache.HomeBookCacheManager;
import com.wcoal.novelplus.service.IHomeBookService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 小说推荐 服务实现类
 * </p>
 *
 * @author wcoal
 * @since 2025-09-30
 */
@Service
@RequiredArgsConstructor
public class HomeServiceImpl extends ServiceImpl<HomeBookMapper, HomeBook> implements IHomeBookService {

    private final HomeBookCacheManager homeBookCacheManager;

    /**
     * 获取首页推荐小说
     * @return 首页推荐小说列表
     */
    @Override
    public RestResp<List<HomeBookRespDto>> listHomeBooks() {
        return RestResp.ok(homeBookCacheManager.listHomeBooks());
    }

}
