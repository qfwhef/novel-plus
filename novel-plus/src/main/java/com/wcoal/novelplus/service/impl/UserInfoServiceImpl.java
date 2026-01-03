package com.wcoal.novelplus.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wcoal.novelplus.core.common.constant.DatabaseConsts;
import com.wcoal.novelplus.core.common.enums.ErrorCodeEnum;
import com.wcoal.novelplus.core.common.enums.UserStatusEnum;
import com.wcoal.novelplus.core.common.exception.BusinessException;
import com.wcoal.novelplus.core.common.resp.RestResp;
import com.wcoal.novelplus.core.utils.BeanUtils;
import com.wcoal.novelplus.dao.entity.UserInfo;
import com.wcoal.novelplus.dao.mapper.UserInfoMapper;
import com.wcoal.novelplus.dto.req.UserInfoUptReqDto;
import com.wcoal.novelplus.dto.req.UserLoginReqDto;
import com.wcoal.novelplus.dto.resp.UserInfoRespDto;
import com.wcoal.novelplus.dto.resp.UserLoginRespDto;
import com.wcoal.novelplus.dto.req.UserRegisterReqDto;
import com.wcoal.novelplus.manager.redis.VerifyCodeManager;
import com.wcoal.novelplus.service.IUserInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.User;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;


/**
 * <p>
 * 用户信息表 服务实现类
 * </p>
 *
 * @author wcoal
 * @since 2025-09-25
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements IUserInfoService {

    private final UserInfoMapper userInfoMapper;

    private final VerifyCodeManager verifyCodeManager;

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 用户注册
     * @param dto
     * @return
     */
    @Override
    public RestResp<Void> register(UserRegisterReqDto dto) {
        try {
            // 校验图形验证码是否正确
            if (!verifyCodeManager.imgVerifyCodeOk(dto.getSessionId(), dto.getVelCode())) {
                // 图形验证码校验失败
                throw new BusinessException(ErrorCodeEnum.USER_VERIFY_CODE_ERROR);
            }
            log.info("用户注册，参数：{}", dto);
            //1.校验用户名，邮箱，手机号是否存在
            UserInfo userName = lambdaQuery().eq(UserInfo::getUserName, dto.getUserName()).one();
            if (userName != null) {
                log.error("用户注册，用户名已存在，参数：{}", dto);
                throw new BusinessException(ErrorCodeEnum.USER_REGISTER_ERROR);
            }
            if (!dto.getPassword().equals(dto.getConfirmPassword())) {
                log.error("用户注册，两次密码不一致，参数：{}", dto);
                throw new BusinessException(ErrorCodeEnum.USER_REGISTER_ERROR);
            }

            //2.创建用户实体
            UserInfo userInfo = BeanUtils.copyBean(dto, UserInfo.class);
            userInfo.setPassword(BCrypt.hashpw(dto.getPassword(), BCrypt.gensalt()));
            // 手动设置时间字段（防止自动填充失效）
            LocalDateTime now = LocalDateTime.now();
            userInfo.setCreateTime(now);
            userInfo.setUpdateTime(now);
            int insertResult = userInfoMapper.insert(userInfo);
            if (insertResult <= 0) {
                log.error("用户注册，数据库插入失败，参数：{}", dto);
                throw new BusinessException(ErrorCodeEnum.USER_REGISTER_ERROR);
            }
        } finally {
            //清除redis中的临时session
            StpUtil.logout();
        }
        return RestResp.ok();
    }

    /**
     * 用户登录
     * @param dto
     * @return
     */
    @Override
    public RestResp<UserLoginRespDto> login(UserLoginReqDto dto) {
        log.info("用户登录，参数：{}", dto);
        //1.参数校验
        UserInfo user = lambdaQuery().eq(UserInfo::getUserName, dto.getUserName())
                .one();
        if (user == null || !BCrypt.checkpw(dto.getPassword(), user.getPassword())) {
            log.error("用户登录，用户名或密码错误，参数：{}", dto);
            throw new BusinessException(ErrorCodeEnum.USER_PASSWORD_ERROR);
        }
        if (user.getStatus() != UserStatusEnum.NORMAL) {
            log.error("用户登录，用户状态异常，参数：{}", dto);
            throw new BusinessException(ErrorCodeEnum.USER_STATUS_ERROR);
        }

        StpUtil.login(user.getId(), dto.getRememberMe());

        //3.更新最后登录时间
        LocalDateTime now = LocalDateTime.now();
        user.setLastLoginTime(now);
        updateById(user);

        UserLoginRespDto build = UserLoginRespDto.builder()
                .uid(user.getId())
                .token(StpUtil.getTokenValue())
                .userName(user.getUserName())
                .nickName(user.getNickName() == null ? "" : user.getNickName())
                .email(user.getEmail())
                .gender(user.getGender() == null ? 0: user.getGender())
                .userType(user.getUserType())
                .status(user.getStatus())
                .expireTime(StpUtil.getTokenTimeout())
                .userPhoto(user.getUserPhoto() == null ? "" : user.getUserPhoto())
                .build();

        return RestResp.ok(build);
    }

    /**
     * 获取用户信息
     * @param loginId
     * @return
     */
    @Override
    public RestResp<UserInfoRespDto> getUserInfo(Long loginId) {
        //校验参数
        if (loginId == null) {
            log.error("获取用户信息，参数loginId为空");
            throw new BusinessException(ErrorCodeEnum.USER_ERROR);
        }
        UserInfo one = lambdaQuery().eq(UserInfo::getId, loginId)
                .one();
        if (one == null) {
            log.error("获取用户信息，用户不存在，参数loginId：{}", loginId);
            throw new BusinessException(ErrorCodeEnum.USER_ERROR);
        }
        UserInfoRespDto userInfoRespDto = BeanUtils.copyBean(one, UserInfoRespDto.class);
        return RestResp.ok(userInfoRespDto);
    }

    /**
     * 更新用户信息
     * @param dto
     * @return
     */
    @Override
    public RestResp<Void> updateUserInfo(UserInfoUptReqDto dto) {
        UserInfo userInfo = new UserInfo();
        userInfo.setId(dto.getUserId());
        userInfo.setNickName(dto.getNickName());
        userInfo.setUserPhoto(dto.getUserPhoto());
        userInfo.setGender(dto.getUserSex());
        userInfoMapper.updateById(userInfo);
        return RestResp.ok();
    }

}
