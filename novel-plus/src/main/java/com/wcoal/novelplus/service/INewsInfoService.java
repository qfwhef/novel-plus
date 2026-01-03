package com.wcoal.novelplus.service;

import com.wcoal.novelplus.core.common.resp.RestResp;
import com.wcoal.novelplus.dao.entity.NewsInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wcoal.novelplus.dto.resp.NewsInfoRespDto;

import java.util.List;

/**
 * <p>
 * 新闻信息 服务类
 * </p>
 *
 * @author wcoal
 * @since 2025-10-03
 */
public interface INewsInfoService extends IService<NewsInfo> {

    /**
     * 最新新闻列表查询接口
     */
    RestResp<List<NewsInfoRespDto>> listLatestNews();

    /**
     * 新闻信息查询接口
     */
    RestResp<NewsInfoRespDto> getNews(Long id);
}
