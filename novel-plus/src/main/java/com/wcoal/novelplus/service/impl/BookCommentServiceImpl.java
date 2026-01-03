package com.wcoal.novelplus.service.impl;

import com.wcoal.novelplus.dao.entity.BookComment;
import com.wcoal.novelplus.dao.mapper.BookCommentMapper;
import com.wcoal.novelplus.service.IBookCommentService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 小说评论 服务实现类
 * </p>
 *
 * @author wcoal
 * @since 2025-10-03
 */
@Service
public class BookCommentServiceImpl extends ServiceImpl<BookCommentMapper, BookComment> implements IBookCommentService {

}
