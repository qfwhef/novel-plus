package com.wcoal.novelplus.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wcoal.novelplus.core.common.resp.RestResp;
import com.wcoal.novelplus.dao.entity.CrawlSingleTask;
import com.wcoal.novelplus.dao.entity.CrawlSource;
import com.wcoal.novelplus.service.CrawlService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 爬虫管理 REST API
 *
 * @author wcoal
 * @since 2025-11-23
 */
@Tag(name = "爬虫管理", description = "爬虫源和采集任务管理接口")
@RestController
@RequestMapping("/api/admin/crawl")
@RequiredArgsConstructor
public class CrawlController {

    private final CrawlService crawlService;

    @Operation(summary = "添加爬虫源")
    @PostMapping("/source")
    public RestResp<Void> addCrawlSource(@RequestBody CrawlSource source) {
        crawlService.addCrawlSource(source);
        return RestResp.ok();
    }

    @Operation(summary = "更新爬虫源")
    @PutMapping("/source")
    public RestResp<Void> updateCrawlSource(@RequestBody CrawlSource source) {
        crawlService.updateCrawlSource(source);
        return RestResp.ok();
    }

    @Operation(summary = "爬虫源分页列表")
    @GetMapping("/source/list")
    public RestResp<Page<CrawlSource>> listCrawlSource(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return RestResp.ok(crawlService.listCrawlByPage(page, pageSize));
    }

    @Operation(summary = "开启/关闭爬虫")
    @PutMapping("/source/{sourceId}/status")
    public RestResp<Void> openOrCloseCrawl(
            @PathVariable Integer sourceId,
            @RequestParam Byte sourceStatus) {
        crawlService.openOrCloseCrawl(sourceId, sourceStatus);
        return RestResp.ok();
    }

    @Operation(summary = "获取爬虫源详情")
    @GetMapping("/source/{id}")
    public RestResp<CrawlSource> getCrawlSource(@PathVariable Integer id) {
        return RestResp.ok(crawlService.getCrawlSource(id));
    }

    @Operation(summary = "添加单本采集任务")
    @PostMapping("/task")
    public RestResp<Void> addCrawlSingleTask(@RequestBody CrawlSingleTask task) {
        crawlService.addCrawlSingleTask(task);
        return RestResp.ok();
    }

    @Operation(summary = "单本采集任务分页列表")
    @GetMapping("/task/list")
    public RestResp<Page<CrawlSingleTask>> listCrawlSingleTask(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return RestResp.ok(crawlService.listCrawlSingleTaskByPage(page, pageSize));
    }

    @Operation(summary = "删除采集任务")
    @DeleteMapping("/task/{id}")
    public RestResp<Void> deleteCrawlSingleTask(@PathVariable Long id) {
        crawlService.delCrawlSingleTask(id);
        return RestResp.ok();
    }

    @Operation(summary = "查询任务进度")
    @GetMapping("/task/{taskId}/progress")
    public RestResp<Integer> getTaskProgress(@PathVariable Long taskId) {
        return RestResp.ok(crawlService.getTaskProgress(taskId));
    }
}
