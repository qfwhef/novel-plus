package com.wcoal.novelplus.dao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 小说信息
 * </p>
 *
 * @author wcoal
 * @since 2025-09-30
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("book_info")
@Schema(name="BookInfo对象", description="小说信息")
public class BookInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "作品方向;0-男频 1-女频")
    private Integer workDirection;

    @Schema(description = "类别ID")
    private Long categoryId;

    @Schema(description = "类别名")
    private String categoryName;

    @Schema(description = "小说封面地址")
    private String picUrl;

    @Schema(description = "小说名")
    private String bookName;

    @Schema(description = "作家id")
    private Long authorId;

    @Schema(description = "作家名")
    private String authorName;

    @Schema(description = "书籍描述")
    private String bookDesc;

    @Schema(description = "评分;总分:10 ，真实评分 = score/10")
    private Integer score;

    @Schema(description = "书籍状态;0-连载中 1-已完结")
    private Integer bookStatus;

    @Schema(description = "点击量")
    private Long visitCount;

    @Schema(description = "总字数")
    private Integer wordCount;

    @Schema(description = "评论数")
    private Integer commentCount;

    @Schema(description = "最新章节ID")
    private Long lastChapterId;

    @Schema(description = "最新章节名")
    private String lastChapterName;

    @Schema(description = "最新章节更新时间")
    private LocalDateTime lastChapterUpdateTime;

    @Schema(description = "是否收费;1-收费 0-免费")
    private Integer isVip;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;


}
