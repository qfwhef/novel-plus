package com.wcoal.novelplus.controller.author;


import com.wcoal.novelplus.core.auth.UserContext;
import com.wcoal.novelplus.core.common.constant.ApiRouterConsts;
import com.wcoal.novelplus.core.common.constant.SystemConfigConsts;
import com.wcoal.novelplus.core.common.req.PageReqDto;
import com.wcoal.novelplus.core.common.resp.PageRespDto;
import com.wcoal.novelplus.core.common.resp.RestResp;
import com.wcoal.novelplus.dto.req.AuthorRegisterReqDto;
import com.wcoal.novelplus.dto.req.BookAddReqDto;
import com.wcoal.novelplus.dto.req.ChapterAddReqDto;
import com.wcoal.novelplus.dto.req.ChapterUpdateReqDto;
import com.wcoal.novelplus.dto.resp.BookChapterRespDto;
import com.wcoal.novelplus.dto.resp.BookInfoRespDto;
import com.wcoal.novelplus.dto.resp.ChapterContentRespDto;
import com.wcoal.novelplus.service.IAuthorInfoService;
import com.wcoal.novelplus.service.IBookInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 作家信息 前端控制器
 * </p>
 *
 * @author wcoal
 * @since 2025-10-05
 */
@Slf4j
@Tag(name = "AuthorController", description = "作家后台-作者模块")
@SecurityRequirement(name = SystemConfigConsts.HTTP_AUTH_HEADER_NAME)
@RestController
@RequestMapping(ApiRouterConsts.API_AUTHOR_URL_PREFIX)
@RequiredArgsConstructor
public class AuthorController {

    private final IAuthorInfoService authorService;

    private final IBookInfoService bookService;

    @PostMapping("/register")
    @Operation(summary = "作家注册接口")
    public RestResp<Void> register(@Valid @RequestBody AuthorRegisterReqDto dto) {
        dto.setUserId(UserContext.getUserId());
        log.info("注册作家用户id:{}", dto.getUserId());
        return authorService.register(dto);
    }

    @Operation(summary = "作家状态查询接口")
    @GetMapping("status")
    public RestResp<Integer> getStatus() {
        return authorService.getStatus(UserContext.getUserId());
    }

    @Operation(summary = "小说发布接口")
    @PostMapping("book")
    public RestResp<Void> publishBook(@Valid @RequestBody BookAddReqDto dto) {
        return bookService.saveBook(dto);
    }

    @Operation(summary = "小说发布列表查询接口")
    @GetMapping("books")
    public RestResp<PageRespDto<BookInfoRespDto>> listBooks(@ParameterObject PageReqDto dto) {
        return bookService.listAuthorBooks(dto);
    }

    @Operation(summary = "小说章节发布接口")
    @PostMapping("book/chapter/{bookId}")
    public RestResp<Void> publishBookChapter(
            @Parameter(description = "小说ID") @PathVariable("bookId") Long bookId,
            @Valid @RequestBody ChapterAddReqDto dto) {
        dto.setBookId(bookId);
        return bookService.saveBookChapter(dto);
    }

    @Operation(summary = "小说章节发布列表查询接口")
    @GetMapping("book/chapters/{bookId}")
    public RestResp<PageRespDto<BookChapterRespDto>> listBookChapters(
            @Parameter(description = "小说ID") @PathVariable("bookId") Long bookId,
            @ParameterObject PageReqDto dto) {
        return bookService.listBookChapters(bookId, dto);
    }

    @Operation(summary = "小说章节删除接口")
    @DeleteMapping("book/chapter/{chapterId}")
    public RestResp<Void> deleteBookChapter(
            @Parameter(description = "章节ID") @PathVariable("chapterId") Long chapterId) {
        return bookService.deleteBookChapter(chapterId);
    }

    @Operation(summary = "小说章节查询接口")
    @GetMapping("book/chapter/{chapterId}")
    public RestResp<ChapterContentRespDto> getBookChapter(
            @Parameter(description = "章节ID") @PathVariable("chapterId") Long chapterId) {
        return bookService.getBookChapter(chapterId);
    }

    @Operation(summary = "小说章节更新接口")
    @PutMapping("book/chapter/{chapterId}")
    public RestResp<Void> updateBookChapter(
            @Parameter(description = "章节ID") @PathVariable("chapterId") Long chapterId,
            @Valid @RequestBody ChapterUpdateReqDto dto) {
        return bookService.updateBookChapter(chapterId, dto);
    }

    @Operation(summary = "小说删除接口")
    @DeleteMapping("book/{bookId}")
    public RestResp<Void> deleteBook(
            @Parameter(description = "小说ID") @PathVariable("bookId") Long bookId) {
        return bookService.deleteBook(bookId);
    }

}
