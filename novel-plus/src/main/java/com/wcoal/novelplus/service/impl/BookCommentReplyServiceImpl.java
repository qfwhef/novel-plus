package com.wcoal.novelplus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wcoal.novelplus.core.auth.UserContext;
import com.wcoal.novelplus.core.common.constant.DatabaseConsts;
import com.wcoal.novelplus.core.common.enums.ErrorCodeEnum;
import com.wcoal.novelplus.core.common.resp.RestResp;
import com.wcoal.novelplus.dao.entity.BookComment;
import com.wcoal.novelplus.dao.entity.BookCommentReply;
import com.wcoal.novelplus.dao.entity.UserInfo;
import com.wcoal.novelplus.dao.mapper.BookCommentMapper;
import com.wcoal.novelplus.dao.mapper.BookCommentReplyMapper;
import com.wcoal.novelplus.dto.req.BookCommentReplyReqDto;
import com.wcoal.novelplus.dto.resp.BookCommentReplyRespDto;
import com.wcoal.novelplus.manager.dao.UserDaoManager;
import com.wcoal.novelplus.service.IBookCommentReplyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>
 * 小说评论回复 服务实现类
 * </p>
 *
 * @author wcoal
 * @since 2025-11-12
 */
@Service
@RequiredArgsConstructor
public class BookCommentReplyServiceImpl extends ServiceImpl<BookCommentReplyMapper, BookCommentReply> 
        implements IBookCommentReplyService {

    private final BookCommentReplyMapper bookCommentReplyMapper;
    private final BookCommentMapper bookCommentMapper;
    private final UserDaoManager userDaoManager;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RestResp<Void> saveReply(BookCommentReplyReqDto dto) {
        // 校验评论是否存在
        BookComment comment = bookCommentMapper.selectById(dto.getCommentId());
        if (comment == null) {
            return RestResp.fail(ErrorCodeEnum.USER_COMMENT_NOT_EXIST);
        }

        // 校验回复内容长度
        if (dto.getReplyContent().length() < 2) {
            return RestResp.fail(ErrorCodeEnum.USER_COMMENT_CONTENT_TOO_SHORT);
        }
        if (dto.getReplyContent().length() > 512) {
            return RestResp.fail(ErrorCodeEnum.USER_COMMENT_CONTENT_TOO_LONG);
        }

        // 保存回复
        BookCommentReply reply = new BookCommentReply();
        reply.setCommentId(dto.getCommentId());
        reply.setUserId(UserContext.getUserId());
        reply.setReplyContent(dto.getReplyContent());
        reply.setAuditStatus(DatabaseConsts.BookCommentTable.AUDIT_PASS);
        reply.setCreateTime(LocalDateTime.now());
        reply.setUpdateTime(LocalDateTime.now());
        
        bookCommentReplyMapper.insert(reply);

        // 更新评论的回复数量
        comment.setReplyCount(comment.getReplyCount() + 1);
        bookCommentMapper.updateById(comment);

        return RestResp.ok();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RestResp<Void> deleteReply(Long userId, Long replyId) {
        QueryWrapper<BookCommentReply> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(DatabaseConsts.CommonColumnEnum.ID.getName(), replyId)
                .eq("user_id", userId);

        BookCommentReply reply = bookCommentReplyMapper.selectOne(queryWrapper);
        if (reply == null) {
            return RestResp.fail(ErrorCodeEnum.USER_COMMENT_NOT_EXIST);
        }

        // 删除回复
        bookCommentReplyMapper.delete(queryWrapper);

        // 更新评论的回复数量
        BookComment comment = bookCommentMapper.selectById(reply.getCommentId());
        if (comment != null && comment.getReplyCount() > 0) {
            comment.setReplyCount(comment.getReplyCount() - 1);
            bookCommentMapper.updateById(comment);
        }

        return RestResp.ok();
    }

    @Override
    public RestResp<List<BookCommentReplyRespDto>> listReplies(Long commentId) {
        // 查询回复列表
        QueryWrapper<BookCommentReply> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("comment_id", commentId)
                .eq("audit_status", DatabaseConsts.BookCommentTable.AUDIT_PASS)
                .orderByAsc(DatabaseConsts.CommonColumnEnum.CREATE_TIME.getName());

        List<BookCommentReply> replies = bookCommentReplyMapper.selectList(queryWrapper);

        if (replies.isEmpty()) {
            return RestResp.ok(List.of());
        }

        // 查询用户信息
        List<Long> userIds = replies.stream().map(BookCommentReply::getUserId).distinct().toList();
        List<UserInfo> userInfos = userDaoManager.listUserInfos(userIds);
        Map<Long, UserInfo> userInfoMap = userInfos.stream()
                .collect(Collectors.toMap(UserInfo::getId, Function.identity()));

        // 构建返回数据
        List<BookCommentReplyRespDto> result = replies.stream()
                .map(reply -> {
                    UserInfo userInfo = userInfoMap.get(reply.getUserId());
                    return BookCommentReplyRespDto.builder()
                            .id(reply.getId())
                            .commentId(reply.getCommentId())
                            .userId(reply.getUserId())
                            .username(userInfo != null ? userInfo.getNickName() : "未知用户")
                            .userPhoto(userInfo != null ? userInfo.getUserPhoto() : null)
                            .replyContent(reply.getReplyContent())
                            .createTime(reply.getCreateTime())
                            .build();
                })
                .toList();

        return RestResp.ok(result);
    }
}
