package com.wcoal.novelplus.service;

import com.wcoal.novelplus.dao.entity.BookCommentReply;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wcoal.novelplus.core.common.resp.RestResp;
import com.wcoal.novelplus.dto.req.BookCommentReplyReqDto;
import com.wcoal.novelplus.dto.resp.BookCommentReplyRespDto;

import java.util.List;

/**
 * <p>
 * 小说评论回复 服务类
 * </p>
 *
 * @author wcoal
 * @since 2025-11-12
 */
public interface IBookCommentReplyService extends IService<BookCommentReply> {

    /**
     * 发表评论回复
     */
    RestResp<Void> saveReply(BookCommentReplyReqDto dto);

    /**
     * 删除评论回复
     */
    RestResp<Void> deleteReply(Long userId, Long replyId);

    /**
     * 查询评论的回复列表
     */
    RestResp<List<BookCommentReplyRespDto>> listReplies(Long commentId);
}
