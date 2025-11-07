package com.mi.im.file.controller;

import com.mi.im.file.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * 文件控制器
 */
@RestController
@RequestMapping("/api/file")
public class FileController {

    @Autowired
    private FileService fileService;

    /**
     * 上传文件
     */
    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "fileType", defaultValue = "general") String fileType) {
        Map<String, String> result = fileService.uploadFile(file, fileType);
        return ResponseEntity.ok(result);
    }

    /**
     * 下载文件
     */
    @GetMapping("/download/{fileId}")
    public ResponseEntity<byte[]> downloadFile(@PathVariable String fileId) throws IOException {
        InputStream inputStream = fileService.downloadFile(fileId);
//        byte[] bytes = inputStream.readAllBytes();
        int bytesRead; // 存储读取的字节数
        byte[] buffer = new byte[1024]; // 设置缓冲区，大小可以根据需要调整
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        // 读取数据
        try {
            while ((bytesRead = inputStream.read(buffer)) != -1) { // 读取字节
                byteArrayOutputStream.write(buffer, 0, bytesRead); // 写入 ByteArrayOutputStream
            }
        } catch (IOException e) {
            e.printStackTrace(); // 处理读取异常
        }

        inputStream.close();
        byte[] bytes =  byteArrayOutputStream.toByteArray();
        // 获取文件信息以设置响应头
        Map<String, Object> fileInfo = fileService.getFileInfo(fileId);
        String contentType = (String) fileInfo.get("contentType");
        String fileName = fileId;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(contentType));
        headers.setContentDispositionFormData("attachment", fileName);
        headers.setContentLength(byteArrayOutputStream.size());

        return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
    }

    /**
     * 获取文件信息
     */
    @GetMapping("/info/{fileId}")
    public ResponseEntity<Map<String, Object>> getFileInfo(@PathVariable String fileId) {
        Map<String, Object> fileInfo = fileService.getFileInfo(fileId);
        return ResponseEntity.ok(fileInfo);
    }

    /**
     * 删除文件
     */
    @DeleteMapping("/delete/{fileId}")
    public ResponseEntity<Map<String, Boolean>> deleteFile(@PathVariable String fileId) {
        boolean deleted = fileService.deleteFile(fileId);
        Map<String, Boolean> valueMap = new HashMap<>();
        valueMap.put("success", deleted);
        return ResponseEntity.ok(valueMap);
    }

    /**
     * 获取文件临时访问URL
     */
    @GetMapping("/presigned-url/{fileId}")
    public ResponseEntity<Map<String, String>> getPresignedUrl(
            @PathVariable String fileId,
            @RequestParam(value = "expiresInSeconds", defaultValue = "3600") int expiresInSeconds) {
        String url = fileService.getPresignedUrl(fileId, expiresInSeconds);
        Map<String, String> valueMap = new HashMap<>();
        valueMap.put("url", url);
        valueMap.put("expiresInSeconds", String.valueOf(expiresInSeconds));
        return ResponseEntity.ok(valueMap);
    }

    /**
     * 获取文件直接访问
     */
    @GetMapping("/view/{fileId}")
    public ResponseEntity<byte[]> viewFile(@PathVariable String fileId) throws IOException {
        InputStream inputStream = fileService.downloadFile(fileId);
        //byte[] bytes = inputStream.readAllBytes();
        int bytesRead; // 存储读取的字节数
        byte[] buffer = new byte[1024]; // 设置缓冲区，大小可以根据需要调整
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        // 读取数据
        try {
            while ((bytesRead = inputStream.read(buffer)) != -1) { // 读取字节
                byteArrayOutputStream.write(buffer, 0, bytesRead); // 写入 ByteArrayOutputStream
            }
        } catch (IOException e) {
            e.printStackTrace(); // 处理读取异常
        }
        inputStream.close();
        byte[] bytes =  byteArrayOutputStream.toByteArray();
        // 获取文件信息以设置响应头
        Map<String, Object> fileInfo = fileService.getFileInfo(fileId);
        String contentType = (String) fileInfo.get("contentType");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(contentType));
        headers.setContentLength(bytes.length);

        return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
    }
}