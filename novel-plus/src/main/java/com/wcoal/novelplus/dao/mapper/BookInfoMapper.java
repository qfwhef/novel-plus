package com.wcoal.novelplus.dao.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wcoal.novelplus.core.annotation.ValidateSortOrder;
import com.wcoal.novelplus.dao.entity.BookInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wcoal.novelplus.dto.resp.BookInfoRespDto;
import com.wcoal.novelplus.dto.resp.BookSearchReqDto;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * <p>
 * 小说信息 Mapper 接口
 * </p>
 *
 * @author wcoal
 * @since 2025-09-30
 */
public interface BookInfoMapper extends BaseMapper<BookInfo> {

    /**
     * 增加小说点击量
     * @param bookId 小说ID
     */
    @Update("update book_info set visit_count = visit_count + 1 where id = #{bookId}")
    void addVisitCount(Long bookId);
    /**
     * 小说搜索
     * @param page mybatis-plus 分页对象
     * @param condition 搜索条件
     * @return 返回结果
     * */
    List<BookInfo> searchBooks(IPage<BookInfoRespDto> page, @ValidateSortOrder BookSearchReqDto condition);
}
