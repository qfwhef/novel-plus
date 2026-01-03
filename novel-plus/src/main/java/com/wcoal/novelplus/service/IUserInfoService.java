package com.wcoal.novelplus.service;

import com.wcoal.novelplus.core.common.resp.RestResp;
import com.wcoal.novelplus.dao.entity.UserInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wcoal.novelplus.dto.req.UserInfoUptReqDto;
import com.wcoal.novelplus.dto.req.UserLoginReqDto;
import com.wcoal.novelplus.dto.resp.UserInfoRespDto;
import com.wcoal.novelplus.dto.resp.UserLoginRespDto;
import com.wcoal.novelplus.dto.req.UserRegisterReqDto;
import jakarta.validation.Valid;

/**
 * <p>
 * 用户信息表 服务类
 * </p>
 *
 * @author wcoal
 * @since 2025-09-25
 */
public interface IUserInfoService extends IService<UserInfo> {

    /**
     * 用户注册
     * @param dto
     * @return
     */
    RestResp<Void> register(UserRegisterReqDto dto);

    /**
     * 用户登录
     * @param dto
     * @return
     */
    RestResp<UserLoginRespDto> login(UserLoginReqDto dto);

    /**
     * 获取用户信息
     * @param loginId
     * @return
     */
    RestResp<UserInfoRespDto> getUserInfo(Long loginId);

    /**
     * 更新用户信息
     * @param dto
     * @return
     */
    RestResp<Void> updateUserInfo(@Valid UserInfoUptReqDto dto);
}
