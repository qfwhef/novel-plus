package com.wcoal.novelplus.service.impl;

import com.wcoal.novelplus.dao.entity.NewsContent;
import com.wcoal.novelplus.dao.mapper.NewsContentMapper;
import com.wcoal.novelplus.service.INewsContentService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 新闻内容 服务实现类
 * </p>
 *
 * @author wcoal
 * @since 2025-10-03
 */
@Service
public class NewsContentServiceImpl extends ServiceImpl<NewsContentMapper, NewsContent> implements INewsContentService {

}
