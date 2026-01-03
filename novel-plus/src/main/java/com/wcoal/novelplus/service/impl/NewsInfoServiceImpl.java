package com.wcoal.novelplus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wcoal.novelplus.core.common.constant.DatabaseConsts;
import com.wcoal.novelplus.core.common.resp.RestResp;
import com.wcoal.novelplus.dao.entity.NewsContent;
import com.wcoal.novelplus.dao.entity.NewsInfo;
import com.wcoal.novelplus.dao.mapper.NewsContentMapper;
import com.wcoal.novelplus.dao.mapper.NewsInfoMapper;
import com.wcoal.novelplus.dto.resp.NewsInfoRespDto;
import com.wcoal.novelplus.manager.cache.NewsCacheManager;
import com.wcoal.novelplus.service.INewsInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 新闻信息 服务实现类
 * </p>
 *
 * @author wcoal
 * @since 2025-10-03
 */
@Service
@RequiredArgsConstructor
public class NewsInfoServiceImpl extends ServiceImpl<NewsInfoMapper, NewsInfo> implements INewsInfoService {

    private final NewsCacheManager newsCacheManager;

    private final NewsInfoMapper newsInfoMapper;

    private final NewsContentMapper newsContentMapper;

    /**
     * 最新新闻列表查询接口
     */
    @Override
    public RestResp<List<NewsInfoRespDto>> listLatestNews() {
        return RestResp.ok(newsCacheManager.listLatestNews());
    }

    /**
     * 新闻信息查询接口
     */
    @Override
    public RestResp<NewsInfoRespDto> getNews(Long id) {
        NewsInfo newsInfo = newsInfoMapper.selectById(id);
        QueryWrapper<NewsContent> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(DatabaseConsts.NewsContentTable.COLUMN_NEWS_ID, id)
                .last(DatabaseConsts.SqlEnum.LIMIT_1.getSql());
        NewsContent newsContent = newsContentMapper.selectOne(queryWrapper);
        return RestResp.ok(NewsInfoRespDto.builder()
                .title(newsInfo.getTitle())
                .sourceName(newsInfo.getSourceName())
                .updateTime(newsInfo.getUpdateTime())
                .content(newsContent.getContent())
                .build());
    }
}
