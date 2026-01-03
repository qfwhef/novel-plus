package com.wcoal.novelplus.service.impl;

import com.wcoal.novelplus.dao.entity.BookCategory;
import com.wcoal.novelplus.dao.mapper.BookCategoryMapper;
import com.wcoal.novelplus.service.IBookCategoryService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 小说类别 服务实现类
 * </p>
 *
 * @author wcoal
 * @since 2025-09-30
 */
@Service
public class BookCategoryServiceImpl extends ServiceImpl<BookCategoryMapper, BookCategory> implements IBookCategoryService {

}
