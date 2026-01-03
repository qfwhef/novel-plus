package com.wcoal.novelplus.core.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.UUID;

@Slf4j
@Data
@AllArgsConstructor
public class R2Utils {

    // 加载配置
    private String accessKeyId;
    private String accessKeySecret;
    private String endpoint;
    private String bucketName;
    private String domain;

    public String uploadFile(MultipartFile file) throws IOException {

        String originalFilename = file.getOriginalFilename();
        //截取文件后缀
        String substring = originalFilename.substring(originalFilename.lastIndexOf("."));
        //使用UUID重新生成文件名，防止文件名称重复造成文件覆盖
        String fileName = UUID.randomUUID().toString() + substring;
        log.info("生成的文件名：{}", fileName);

        // 创建S3客户端
        S3Client s3 = S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .region(Region.of("auto"))  // Cloudflare R2区域（选择合适的区域）
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKeyId, accessKeySecret)))
                .build();

        // 上传文件
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .build();

        // 使用InputStream方式上传文件，避免大文件加载到内存导致OOM
        try (InputStream inputStream = file.getInputStream()) {
            PutObjectResponse response = s3.putObject(
                    putObjectRequest,
                    RequestBody.fromInputStream(inputStream, file.getSize())
            );
        }

        // 关闭S3客户端
        s3.close();
        // 拼接URL
        String url = domain + fileName;
        log.info("文件上传成功，访问URL: {}", url);
        return url;
    }
}