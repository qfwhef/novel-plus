package com.wcoal.novelplus.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.wcoal.novelplus.core.common.resp.RestResp;
import com.wcoal.novelplus.dao.entity.AuthorInfo;
import com.wcoal.novelplus.dao.mapper.AuthorInfoMapper;
import com.wcoal.novelplus.dto.AuthorInfoDto;
import com.wcoal.novelplus.dto.req.AuthorRegisterReqDto;
import com.wcoal.novelplus.manager.cache.AuthorInfoCacheManager;
import com.wcoal.novelplus.service.IAuthorInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * <p>
 * 作者信息 服务实现类
 * </p>
 *
 * @author wcoal
 * @since 2025-10-05
 */
@Service
@RequiredArgsConstructor
public class AuthorInfoServiceImpl extends ServiceImpl<AuthorInfoMapper, AuthorInfo> implements IAuthorInfoService {

    private final AuthorInfoCacheManager authorInfoCacheManager;

    private final AuthorInfoMapper authorInfoMapper;

    /**
     * 作家注册
     * @param dto 作家注册请求参数
     * @return 作家注册结果
     */
    @Override
    public RestResp<Void> register(AuthorRegisterReqDto dto) {
        //校验该用户是否已经注册为作家
        AuthorInfo isAuthor = lambdaQuery().eq(AuthorInfo::getUserId, dto.getUserId()).one();
        if (isAuthor != null) {
            return RestResp.ok();
        }
        // 保存作家注册信息
        AuthorInfo authorInfo = new AuthorInfo();
        authorInfo.setUserId(dto.getUserId());
        authorInfo.setChatAccount(dto.getChatAccount());
        authorInfo.setEmail(dto.getEmail());
        authorInfo.setInviteCode("0");
        authorInfo.setTelPhone(dto.getTelPhone());
        authorInfo.setPenName(dto.getPenName());
        authorInfo.setWorkDirection(dto.getWorkDirection());
        authorInfo.setCreateTime(LocalDateTime.now());
        authorInfo.setUpdateTime(LocalDateTime.now());
        authorInfoMapper.insert(authorInfo);
        // 清除作家缓存
        authorInfoCacheManager.evictAuthorCache();

        return RestResp.ok();
    }

    /**
     * 查询作家状态接口
     * @param userId 作家用户id
     * @return 作家状态
     */
    @Override
    public RestResp<Integer> getStatus(Long userId) {
        AuthorInfoDto author = authorInfoCacheManager.getAuthor(userId);
        StpUtil.login(userId);
        return Objects.isNull(author) ? RestResp.ok(null) : RestResp.ok(author.getStatus());
    }
}
