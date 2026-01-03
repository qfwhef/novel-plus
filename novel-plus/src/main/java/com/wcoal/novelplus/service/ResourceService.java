package com.wcoal.novelplus.service;

import com.wcoal.novelplus.core.common.resp.RestResp;
import com.wcoal.novelplus.dto.resp.ImgVerifyCodeRespDto;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ResourceService {

    /**
     * 获取图片验证码
     */
    RestResp<ImgVerifyCodeRespDto> getImgVerifyCode() throws IOException;

    /**
     * 图片上传接口
     */
    RestResp<String> uploadImage(MultipartFile file) throws IOException;
}
