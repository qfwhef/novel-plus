package com.wcoal.novelplus.manager.redis;

import com.wcoal.novelplus.core.common.constant.CacheConsts;
import com.wcoal.novelplus.core.common.utils.ImgVerifyCodeUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.Objects;

@Component
@RequiredArgsConstructor
@Slf4j
public class VerifyCodeManager {

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 生成图形验证码，存到redis中
     */
    public String genImgVerifyCode(String sessionId) throws IOException {

        String verifyCode = ImgVerifyCodeUtils.getRandomVerifyCode(4);
        String img = ImgVerifyCodeUtils.genVerifyCodeImg(verifyCode);
        stringRedisTemplate.opsForValue().set(CacheConsts.IMG_VERIFY_CODE_CACHE_KEY + sessionId,
                verifyCode, Duration.ofMinutes(5));
        return img;
    }

    /**
     * 校验图片验证码是否正确
     */
    public boolean imgVerifyCodeOk(String sessionId, String velCode) {
        String cacheKey = CacheConsts.IMG_VERIFY_CODE_CACHE_KEY + sessionId;
        String cachedCode = stringRedisTemplate.opsForValue().get(cacheKey);
        
        // 验证码校验成功后立即删除，防止重复使用
        if (Objects.equals(cachedCode, velCode)) {
            stringRedisTemplate.delete(cacheKey);
            return true;
        }
        return false;
    }
}
