package com.mi.im.file.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Map;

/**
 * 文件服务接口
 */
public interface FileService {

    /**
     * 上传文件
     * @param file 上传的文件
     * @param fileType 文件类型标识
     * @return 包含文件信息的Map
     */
    Map<String, String> uploadFile(MultipartFile file, String fileType);

    /**
     * 下载文件
     * @param fileId 文件ID
     * @return 文件输入流
     */
    InputStream downloadFile(String fileId);

    /**
     * 获取文件信息
     * @param fileId 文件ID
     * @return 包含文件信息的Map
     */
    Map<String, Object> getFileInfo(String fileId);

    /**
     * 删除文件
     * @param fileId 文件ID
     * @return 是否删除成功
     */
    boolean deleteFile(String fileId);

    /**
     * 获取文件临时访问URL
     * @param fileId 文件ID
     * @param expiresInSeconds 过期时间（秒）
     * @return 临时访问URL
     */
    String getPresignedUrl(String fileId, int expiresInSeconds);
}