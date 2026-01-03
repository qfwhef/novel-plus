package com.wcoal.novelplus.controller.front;


import com.wcoal.novelplus.core.common.constant.ApiRouterConsts;
import com.wcoal.novelplus.core.common.resp.RestResp;
import com.wcoal.novelplus.dto.resp.HomeBookRespDto;
import com.wcoal.novelplus.service.IHomeBookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>
 * 小说推荐 前端控制器
 * </p>
 *
 * @author wcoal
 * @since 2025-09-30
 */
@RestController
@RequestMapping(ApiRouterConsts.API_FRONT_HOME_URL_PREFIX)
@Slf4j
@Tag(name = "HomeBookController")
@RequiredArgsConstructor
public class HomeController {

    private final IHomeBookService homeBookService;

    @Operation(summary = "获取首页推荐小说")
    @GetMapping("/books")
    public RestResp<List<HomeBookRespDto>> getHomeBooks() {
        log.info("获取首页推荐小说");
        return homeBookService.listHomeBooks();
    }

}
