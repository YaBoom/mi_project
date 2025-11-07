package com.mi.im.common.model;

import lombok.Data;
import java.io.Serializable;
import java.util.Date;

/**
 * 消息实体类
 */
@Data
public class Message implements Serializable {
    private static final long serialVersionUID = 1L;

    // 消息ID
    private String messageId;
    
    // 发送者ID
    private String senderId;
    
    // 接收者ID
    private String receiverId;
    
    // 消息类型: 1-文本, 2-图片, 3-语音, 4-视频, 5-表情, 6-文件
    private Integer messageType;
    
    // 消息内容
    private String content;
    
    // 文件URL（用于图片、语音、视频、文件类型消息）
    private String fileUrl;
    
    // 文件大小
    private Long fileSize;
    
    // 文件类型
    private String fileType;
    
    // 消息状态: 0-发送中, 1-已发送, 2-已送达, 3-已读
    private Integer status;
    
    // 发送时间
    private Date sendTime;
    
    // 是否为群组消息
    private Boolean isGroupMessage;
    
    // 群组ID
    private String groupId;
    
    // 是否离线消息
    private Boolean isOffline;

    private Boolean isBlocked;
}