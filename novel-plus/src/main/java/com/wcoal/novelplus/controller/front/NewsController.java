package com.wcoal.novelplus.controller.front;


import com.wcoal.novelplus.core.common.constant.ApiRouterConsts;
import com.wcoal.novelplus.core.common.resp.RestResp;
import com.wcoal.novelplus.dto.resp.NewsInfoRespDto;
import com.wcoal.novelplus.service.INewsInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>
 * 新闻信息 前端控制器
 * </p>
 *
 * @author wcoal
 * @since 2025-10-03
 */
@Tag(name = "NewsController", description = "前台门户-新闻模块")
@RestController
@RequestMapping(ApiRouterConsts.API_FRONT_NEWS_URL_PREFIX)
@RequiredArgsConstructor
public class NewsController {

    private final INewsInfoService newsService;

    /**
     * 最新新闻列表查询接口
     */
    @Operation(summary = "最新新闻列表查询接口")
    @GetMapping("latest_list")
    public RestResp<List<NewsInfoRespDto>> listLatestNews() {
        return newsService.listLatestNews();
    }

    /**
     * 新闻信息查询接口
     */
    @Operation(summary = "新闻信息查询接口")
    @GetMapping("{id}")
    public RestResp<NewsInfoRespDto> getNews(
            @Parameter(description = "新闻ID") @PathVariable Long id) {
        return newsService.getNews(id);
    }

}
