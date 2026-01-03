package com.wcoal.novelplus.dto.resp;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
/**
 * <p>
 * 用户书架列表响应DTO
 * </p>
 *
 * @author wcoal
 * @since 2025-10-21
 */

@Data
@Builder
public class UserBookShelfRespDto {
    /**
     * 主键ID
     */
    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "书籍ID")
    private Long bookId;

    /**
     * 书籍名称
     */
    @Schema(description = "书籍名称")
    private String bookName;

     /**
      * 作者名称
      */
    @Schema(description = "作者名称")
    private String authorName;

     /**
      * 封面图片URL
      */
    @Schema(description = "封面图片URL")
    private String picUrl;

     /**
      * 书籍描述
      */
    @Schema(description = "书籍描述")
    private String bookDesc;

     /**
      * 分类名称
      */
    @Schema(description = "分类名称")
    private String categoryName;

     /**
      * 总字数
      */
    @Schema(description = "总字数")
    private Integer wordCount;

     /**
      * 上次阅读的章节ID
      */
    @Schema(description = "上次阅读的章节ID")
    private Long preContentId;

     /**
      * 第一章节ID
      */
    @Schema(description = "第一章节ID")
    private Long firstChapterId;

     /**
      * 创建时间
      */
    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

     /**
      * 更新时间
      */
    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
