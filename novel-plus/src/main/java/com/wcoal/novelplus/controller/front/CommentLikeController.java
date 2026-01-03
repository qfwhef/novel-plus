package com.wcoal.novelplus.controller.front;

import com.wcoal.novelplus.core.auth.UserContext;
import com.wcoal.novelplus.core.common.constant.ApiRouterConsts;
import com.wcoal.novelplus.core.common.resp.RestResp;
import com.wcoal.novelplus.dto.req.CommentLikeReqDto;
import com.wcoal.novelplus.service.IBookCommentLikeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 评论点赞 前端控制器
 * </p>
 *
 * @author wcoal
 * @since 2025-11-12
 */
@RestController
@RequestMapping(ApiRouterConsts.API_FRONT_COMMENT_URL_PREFIX)
@Tag(name = "CommentLikeController", description = "前端评论点赞模块")
@Slf4j
@RequiredArgsConstructor
public class CommentLikeController {

    private final IBookCommentLikeService commentLikeService;

    @Operation(summary = "切换点赞状态接口")
    @PostMapping("/like")
    public RestResp<Void> toggleLike(@Valid @RequestBody CommentLikeReqDto reqDto) {
        Long userId = UserContext.getUserId();
        log.info("切换点赞状态接口,用户ID:{},评论ID:{},点赞状态:{}", 
                userId, reqDto.getCommentId(), reqDto.getLiked());
        return commentLikeService.toggleLike(reqDto.getCommentId(), userId, reqDto.getLiked());
    }

    @Operation(summary = "查询点赞数量接口")
    @GetMapping("/{commentId}/like/count")
    public RestResp<Integer> getLikeCount(@Parameter(description = "评论ID") @PathVariable Long commentId) {
        return commentLikeService.getLikeCount(commentId);
    }

    @Operation(summary = "查询点赞状态接口")
    @GetMapping("/{commentId}/like/status")
    public RestResp<Boolean> getLikeStatus(@Parameter(description = "评论ID") @PathVariable Long commentId) {
        return commentLikeService.isLiked(commentId, UserContext.getUserId());
    }

    @Operation(summary = "批量查询点赞状态接口")
    @PostMapping("/like/batch-status")
    public RestResp<Map<Long, Boolean>> batchGetLikeStatus(@RequestBody List<Long> commentIds) {
        log.info("批量查询点赞状态接口,用户ID:{},评论数量:{}", UserContext.getUserId(), commentIds.size());
        return commentLikeService.batchIsLiked(commentIds, UserContext.getUserId());
    }
}
