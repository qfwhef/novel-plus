package com.wcoal.novelplus.controller.front;

import com.wcoal.novelplus.core.common.constant.ApiRouterConsts;
import com.wcoal.novelplus.core.common.resp.RestResp;
import com.wcoal.novelplus.dto.resp.ImgVerifyCodeRespDto;
import com.wcoal.novelplus.service.ResourceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Tag(name = "ResourseController", description = "资源管理接口")
@RequestMapping(ApiRouterConsts.API_FRONT_RESOURCE_URL_PREFIX)
@RequiredArgsConstructor
@RestController
public class ResourseController {

    private final ResourceService resourceService;

    /**
     * 图片验证码接口
     */

    @Operation(summary = "获取图片验证码")
    @GetMapping("img_verify_code")
    public RestResp<ImgVerifyCodeRespDto> getImgVerifyCode() throws IOException {
        return resourceService.getImgVerifyCode();
    }

    /**
     * 图片上传接口
     */
    @Operation(summary = "图片上传接口")
    @PostMapping("/image")
    RestResp<String> uploadImage(
            @Parameter(description = "上传文件") @RequestParam("file") MultipartFile file) throws IOException {
        return resourceService.uploadImage(file);
    }

}
