package com.wcoal.novelplus.controller.front;


import cn.dev33.satoken.stp.StpUtil;
import com.wcoal.novelplus.core.common.constant.ApiRouterConsts;
import com.wcoal.novelplus.core.auth.UserContext;
import com.wcoal.novelplus.core.common.req.PageReqDto;
import com.wcoal.novelplus.core.common.resp.PageRespDto;
import com.wcoal.novelplus.core.common.resp.RestResp;
import com.wcoal.novelplus.dto.req.BookCommentReplyReqDto;
import com.wcoal.novelplus.dto.req.UserBookShelfReqDto;
import com.wcoal.novelplus.dto.req.UserCommentReqDto;
import com.wcoal.novelplus.dto.req.UserInfoUptReqDto;
import com.wcoal.novelplus.dto.req.UserLoginReqDto;
import com.wcoal.novelplus.dto.resp.BookCommentReplyRespDto;
import com.wcoal.novelplus.dto.resp.UserBookShelfRespDto;
import com.wcoal.novelplus.dto.resp.UserCommentRespDto;
import com.wcoal.novelplus.dto.resp.UserInfoRespDto;
import com.wcoal.novelplus.dto.resp.UserLoginRespDto;
import com.wcoal.novelplus.dto.req.UserRegisterReqDto;
import com.wcoal.novelplus.service.IBookCommentReplyService;
import com.wcoal.novelplus.service.IBookInfoService;
import com.wcoal.novelplus.service.IUserInfoService;

import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 用户信息表 前端控制器
 * </p>
 *
 * @author wcoal
 * @since 2025-09-25
 */
@RestController
@RequestMapping(ApiRouterConsts.API_FRONT_USER_URL_PREFIX)
@Slf4j
@Tag(name = "UserController", description = "用户模块")
@RequiredArgsConstructor
public class UserController {

    private final IUserInfoService userService;

    private final IBookInfoService bookService;

    private final IBookCommentReplyService bookCommentReplyService;

    @PostMapping("/register")
    @Operation(summary = "用户注册接口(仅注册)")
    public RestResp<Void> register(@Valid @RequestBody UserRegisterReqDto dto) {
        log.info("用户注册接口,用户名:{}", dto.getUserName());
        return userService.register(dto);
    }

    @PostMapping("/login")
    @Operation(summary = "用户登录接口")
    public RestResp<UserLoginRespDto> login(@Valid @RequestBody UserLoginReqDto dto) {
        log.info("用户登录接口,用户名:{}", dto.getUserName());
        return userService.login(dto);
    }

    @PostMapping("/logout")
    @Operation(summary = "用户登出接口")
    public RestResp<Void> logout() {
        log.info("用户登出接口,用户ID:{}", UserContext.getUserId());
        StpUtil.logout();
        return RestResp.ok();
    }

    @GetMapping
    @Operation(summary = "获取当前登录用户信息接口")
    public RestResp<UserInfoRespDto> getUserInfo() {
        log.info("当前线程userId:{}", UserContext.getUserId());
        return userService.getUserInfo(UserContext.getUserId());
    }

    @PutMapping
    @Operation(summary = "用户信息修改接口")
    public RestResp<Void> updateUserInfo(@Valid @RequestBody UserInfoUptReqDto dto) {
        log.info("用户信息修改接口,用户ID:{}", UserContext.getUserId());
        dto.setUserId(UserContext.getUserId());
        return userService.updateUserInfo(dto);
    }

    @PostMapping("/comment")
    @Operation(summary = "用户发表评论接口")
    public RestResp<Void> userComment(@Valid @RequestBody UserCommentReqDto dto) {
        dto.setUserId(UserContext.getUserId());
        log.info("用户发表评论接口,用户ID:{},小说ID:{},评论内容:{}", dto.getUserId(), dto.getBookId(), dto.getCommentContent());
        return bookService.userComment(dto);
    }

    @Operation(summary = "修改评论接口")
    @PutMapping("comment/{id}")
    public RestResp<Void> updateComment(@Parameter(description = "评论ID") @PathVariable Long id,
                                        @RequestParam String content) {
        log.info("修改评论接口,评论ID:{},评论内容:{}", id, content);
        return bookService.updateComment(UserContext.getUserId(), id, content);
    }

    @Operation(summary = "删除评论接口")
    @DeleteMapping("comment/{id}")
    public RestResp<Void> deleteComment(@Parameter(description = "评论ID") @PathVariable Long id) {
        log.info("删除评论接口,评论ID:{}", id);
        return bookService.deleteComment(UserContext.getUserId(), id);
    }

    @Operation(summary = "获取用户评论接口")
    @GetMapping("/comments")
    public RestResp<PageRespDto<UserCommentRespDto>> getUserComment(PageReqDto pageReqDto) {
        log.info("获取用户评论接口,用户ID:{},分页参数:{}", UserContext.getUserId(), pageReqDto);
        return bookService.getUserComment(UserContext.getUserId(), pageReqDto);
    }

    @Operation(summary = "获取用户书架列表接口")
    @GetMapping("/bookshelf")
    public RestResp<PageRespDto<UserBookShelfRespDto>> getUserBookShelf(PageReqDto pageReqDto) {
        log.info("获取用户书架列表接口,用户ID:{},分页参数:{}", UserContext.getUserId(), pageReqDto);
        return bookService.getUserBookShelf(UserContext.getUserId(), pageReqDto);
    }

    @Operation(summary = "检查书籍是否在书架中")
    @GetMapping("/bookshelf/check/{bookId}")
    public RestResp<Boolean> checkBookInShelf(@Parameter(description = "书籍ID") @PathVariable Long bookId) {
        log.info("检查书籍是否在书架接口,用户ID:{},书籍ID:{}", UserContext.getUserId(), bookId);
        return bookService.checkBookInShelf(UserContext.getUserId(), bookId);
    }

    @Operation(summary = "添加书籍到书架")
    @PostMapping("/bookshelf")
    public RestResp<Void> addToBookShelf(@Valid @RequestBody UserBookShelfReqDto dto) {
        log.info("添加书籍到书架接口,用户ID:{},书籍ID:{}", UserContext.getUserId(), dto.getBookId());
        return bookService.addToBookShelf(UserContext.getUserId(), dto.getBookId());
    }

    @Operation(summary = "从书架移除书籍")
    @DeleteMapping("/bookshelf/{bookId}")
    public RestResp<Void> removeFromBookShelf(@Parameter(description = "书籍ID") @PathVariable Long bookId) {
        log.info("从书架移除书籍接口,用户ID:{},书籍ID:{}", UserContext.getUserId(), bookId);
        return bookService.removeFromBookShelf(UserContext.getUserId(), bookId);
    }

    @Operation(summary = "发表评论回复接口")
    @PostMapping("/comment/reply")
    public RestResp<Void> saveCommentReply(@Valid @RequestBody BookCommentReplyReqDto dto) {
        log.info("发表评论回复接口,用户ID:{},评论ID:{},回复内容:{}", UserContext.getUserId(), dto.getCommentId(), dto.getReplyContent());
        return bookCommentReplyService.saveReply(dto);
    }

    @Operation(summary = "删除评论回复接口")
    @DeleteMapping("/comment/reply/{replyId}")
    public RestResp<Void> deleteCommentReply(@Parameter(description = "回复ID") @PathVariable Long replyId) {
        log.info("删除评论回复接口,用户ID:{},回复ID:{}", UserContext.getUserId(), replyId);
        return bookCommentReplyService.deleteReply(UserContext.getUserId(), replyId);
    }

    @Operation(summary = "查询评论回复列表接口")
    @GetMapping("/comment/{commentId}/replies")
    public RestResp<List<BookCommentReplyRespDto>> listCommentReplies(@Parameter(description = "评论ID") @PathVariable Long commentId) {
        log.info("查询评论回复列表接口,评论ID:{}", commentId);
        return bookCommentReplyService.listReplies(commentId);
    }

}
