package com.mi.im.common.constant;

public class MessageConstants {
    // 消息类型
    public static final Integer MESSAGE_TYPE_TEXT = 1;
    public static final Integer MESSAGE_TYPE_IMAGE = 2;
    public static final Integer MESSAGE_TYPE_VOICE = 3;
    public static final Integer MESSAGE_TYPE_VIDEO = 4;
    public static final Integer MESSAGE_TYPE_FILE = 5;
    public static final Integer MESSAGE_TYPE_SYSTEM = 6;
    public static final Integer MESSAGE_TYPE_AUTH = 0;
    
    // 消息状态
    public static final Integer MESSAGE_STATUS_SENDING = 1;
    public static final Integer MESSAGE_STATUS_SENT = 2;
    public static final Integer MESSAGE_STATUS_DELIVERED = 3;
    public static final Integer MESSAGE_STATUS_READ = 4;
    public static final Integer MESSAGE_STATUS_FAILED = 5;
    
    // 用户状态
    public static final Integer USER_STATUS_ONLINE = 1;
    public static final Integer USER_STATUS_OFFLINE = 2;
    public static final Integer USER_STATUS_HIDDEN = 3;
}