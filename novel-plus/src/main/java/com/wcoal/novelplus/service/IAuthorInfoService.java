package com.wcoal.novelplus.service;

import com.wcoal.novelplus.core.common.resp.RestResp;
import com.wcoal.novelplus.dao.entity.AuthorInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wcoal.novelplus.dto.req.AuthorRegisterReqDto;
import jakarta.validation.Valid;

/**
 * <p>
 * 作者信息 服务类
 * </p>
 *
 * @author wcoal
 * @since 2025-10-05
 */
public interface IAuthorInfoService extends IService<AuthorInfo> {

    /**
     * 作家注册
     * @param dto 作家注册请求参数
     * @return 作家注册结果
     */
    RestResp<Void> register(@Valid AuthorRegisterReqDto dto);

    /**
     * 查询作家状态接口
     * @param userId 作家用户id
     * @return 作家状态
     */
    RestResp<Integer> getStatus(Long userId);
}
