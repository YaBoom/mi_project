package com.mi.im.file.service.impl;

import com.mi.im.file.service.FileService;
import io.minio.*;
import io.minio.http.Method;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 文件服务实现类
 */
@Service
public class FileServiceImpl implements FileService {

    private static final Logger logger = LoggerFactory.getLogger(FileServiceImpl.class);

    @Value("${minio.url}")
    private String minioUrl;

    @Value("${minio.accessKey}")
    private String accessKey;

    @Value("${minio.secretKey}")
    private String secretKey;

    @Value("${minio.bucketName}")
    private String bucketName;

    @Value("${minio.secure}")
    private boolean secure;

    @Value("${file.maxSize}")
    private long maxFileSize;

    @Value("${file.allowedTypes}")
    private String allowedTypes;

    private MinioClient minioClient;

    @PostConstruct
    public void init() {
        // 初始化MinIO客户端
        minioClient = MinioClient.builder()
                .endpoint(minioUrl)
                .credentials(accessKey, secretKey)
                .build();
        
        // 检查并创建存储桶
        try {
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                logger.info("创建存储桶: {}", bucketName);
            }
        } catch (Exception e) {
            logger.error("初始化MinIO存储桶失败", e);
            throw new RuntimeException("初始化MinIO存储桶失败", e);
        }
    }

    @Override
    public Map<String, String> uploadFile(MultipartFile file, String fileType) {
        try {
            // 验证文件大小
            if (file.getSize() > maxFileSize) {
                throw new RuntimeException("文件大小超过限制");
            }

            // 验证文件类型
            String contentType = file.getContentType();
            if (contentType == null || !Arrays.asList(allowedTypes.split(",")).contains(contentType)) {
                throw new RuntimeException("不支持的文件类型: " + contentType);
            }

            // 生成唯一文件名
            String originalFileName = file.getOriginalFilename();
            String extension = FilenameUtils.getExtension(originalFileName);
            String newFileName = UUID.randomUUID().toString() + "." + extension;
            String objectName = fileType + "/" + newFileName;

            // 上传文件到MinIO
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(contentType)
                            .build());

            logger.info("文件上传成功: {}", objectName);

            // 返回文件信息
            Map<String, String> result = new HashMap<>();
            result.put("fileId", newFileName);
            result.put("fileName", originalFileName);
            result.put("fileType", fileType);
            result.put("contentType", contentType);
            result.put("fileSize", String.valueOf(file.getSize()));
            result.put("uploadTime", new Date().toString());

            return result;
        } catch (Exception e) {
            logger.error("文件上传失败", e);
            throw new RuntimeException("文件上传失败", e);
        }
    }

    @Override
    public InputStream downloadFile(String fileId) {
        try {
            // 查找文件所在目录（这里简化处理，实际应该从数据库查询）
            String objectName = "general/" + fileId;
            
            // 检查文件是否存在
            try {
                minioClient.statObject(
                        StatObjectArgs.builder()
                                .bucket(bucketName)
                                .object(objectName)
                                .build());
            } catch (Exception e) {
                // 文件不存在，尝试在avatar目录查找
                objectName = "avatar/" + fileId;
                try {
                    minioClient.statObject(
                            StatObjectArgs.builder()
                                    .bucket(bucketName)
                                    .object(objectName)
                                    .build());
                } catch (Exception ex) {
                    throw new RuntimeException("文件不存在: " + fileId);
                }
            }

            // 获取文件输入流
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build());
        } catch (Exception e) {
            logger.error("文件下载失败: {}", fileId, e);
            throw new RuntimeException("文件下载失败", e);
        }
    }

    @Override
    public Map<String, Object> getFileInfo(String fileId) {
        try {
            // 查找文件所在目录
            String objectName = "general/" + fileId;
            StatObjectResponse stat = null;
            
            try {
                stat = minioClient.statObject(
                        StatObjectArgs.builder()
                                .bucket(bucketName)
                                .object(objectName)
                                .build());
            } catch (Exception e) {
                objectName = "avatar/" + fileId;
                stat = minioClient.statObject(
                        StatObjectArgs.builder()
                                .bucket(bucketName)
                                .object(objectName)
                                .build());
            }

            if (stat == null) {
                throw new RuntimeException("文件不存在: " + fileId);
            }

            // 构建文件信息
            Map<String, Object> fileInfo = new HashMap<>();
            fileInfo.put("fileId", fileId);
            fileInfo.put("contentType", stat.contentType());
            fileInfo.put("fileSize", stat.size());
            fileInfo.put("lastModified", stat.lastModified());
            fileInfo.put("etag", stat.etag());

            return fileInfo;
        } catch (Exception e) {
            logger.error("获取文件信息失败: {}", fileId, e);
            throw new RuntimeException("获取文件信息失败", e);
        }
    }

    @Override
    public boolean deleteFile(String fileId) {
        try {
            // 查找文件所在目录
            String objectName = "general/" + fileId;
            boolean deleted = false;
            
            try {
                minioClient.removeObject(
                        RemoveObjectArgs.builder()
                                .bucket(bucketName)
                                .object(objectName)
                                .build());
                deleted = true;
            } catch (Exception e) {
                // 尝试在avatar目录查找
                objectName = "avatar/" + fileId;
                minioClient.removeObject(
                        RemoveObjectArgs.builder()
                                .bucket(bucketName)
                                .object(objectName)
                                .build());
                deleted = true;
            }

            logger.info("文件删除成功: {}", fileId);
            return deleted;
        } catch (Exception e) {
            logger.error("文件删除失败: {}", fileId, e);
            throw new RuntimeException("文件删除失败", e);
        }
    }

    @Override
    public String getPresignedUrl(String fileId, int expiresInSeconds) {
        try {
            // 查找文件所在目录
            String objectName = "general/" + fileId;
            boolean exists = false;
            
            try {
                minioClient.statObject(
                        StatObjectArgs.builder()
                                .bucket(bucketName)
                                .object(objectName)
                                .build());
                exists = true;
            } catch (Exception e) {
                objectName = "avatar/" + fileId;
                minioClient.statObject(
                        StatObjectArgs.builder()
                                .bucket(bucketName)
                                .object(objectName)
                                .build());
                exists = true;
            }

            if (!exists) {
                throw new RuntimeException("文件不存在: " + fileId);
            }

            // 生成预签名URL
            String url = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(objectName)
                            .expiry(expiresInSeconds, TimeUnit.SECONDS)
                            .build());

            return url;
        } catch (Exception e) {
            logger.error("生成预签名URL失败: {}", fileId, e);
            throw new RuntimeException("生成预签名URL失败", e);
        }
    }
}