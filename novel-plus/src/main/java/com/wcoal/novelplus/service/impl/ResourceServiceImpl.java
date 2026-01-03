package com.wcoal.novelplus.service.impl;

import com.wcoal.novelplus.core.common.resp.RestResp;
import com.wcoal.novelplus.core.utils.R2Utils;
import com.wcoal.novelplus.dto.resp.ImgVerifyCodeRespDto;
import com.wcoal.novelplus.manager.redis.VerifyCodeManager;
import com.wcoal.novelplus.service.ResourceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResourceServiceImpl implements ResourceService {

    private final VerifyCodeManager verifyCodeManager;

    private final R2Utils r2Utils;

    /**
     * 获取图片验证码
     */
    @Override
    public RestResp<ImgVerifyCodeRespDto> getImgVerifyCode() throws IOException {
        // 使用UUID生成sessionId，避免创建Sa-Token匿名会话
        String sessionId = UUID.randomUUID().toString().replace("-", "");
        return RestResp.ok(ImgVerifyCodeRespDto.builder()
                .sessionId(sessionId)
                .img(verifyCodeManager.genImgVerifyCode(sessionId))
                .build());
    }

    /**
     * 图片上传接口
     */
    @Override
    public RestResp<String> uploadImage(MultipartFile file) throws IOException {
        String url = null;
        try {
            log.info("开始上传文件到r2,图片大小：{}KB", file.getSize()/1024);
            url = r2Utils.uploadFile(file);
            log.info("文件上传到r2成功,图片url：{}", url);
        } catch (IOException e) {
            log.error("文件上传到r2失败,异常信息：{}", e.getMessage());
        }
        return RestResp.ok(url);
    }
}
