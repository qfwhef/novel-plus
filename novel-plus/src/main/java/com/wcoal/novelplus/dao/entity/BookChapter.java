package com.wcoal.novelplus.dao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 小说章节
 * </p>
 *
 * @author wcoal
 * @since 2025-10-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("book_chapter")
@Schema(name="BookChapter对象", description="小说章节")
public class BookChapter implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(name = "小说ID", description = "小说ID")
    private Long bookId;

    @Schema(name = "章节号", description = "章节号")
    private Integer chapterNum;

    @Schema(name = "章节名", description = "章节名")
    private String chapterName;

    @Schema(name = "章节字数", description = "章节字数")
    private Integer wordCount;

    @Schema(name = "是否收费", description = "是否收费;1-收费 0-免费")
    private Integer isVip;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;


}
