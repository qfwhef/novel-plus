package com.wcoal.novelplus.service.impl;

import com.wcoal.novelplus.dao.entity.BookContent;
import com.wcoal.novelplus.dao.mapper.BookContentMapper;
import com.wcoal.novelplus.service.IBookContentService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 小说内容 服务实现类
 * </p>
 *
 * @author wcoal
 * @since 2025-10-02
 */
@Service
public class BookContentServiceImpl extends ServiceImpl<BookContentMapper, BookContent> implements IBookContentService {

}
