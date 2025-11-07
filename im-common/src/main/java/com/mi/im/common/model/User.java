package com.mi.im.common.model;

import lombok.Data;
import java.io.Serializable;
import java.util.Date;


/**
 * 用户实体类
 */
@Data
public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    // 用户ID
    private String userId;
    
    // 用户名
    private String username;
    
    // 密码
    private String password;
    
    // 昵称
    private String nickname;
    
    // 头像URL
    private String avatar;
    
    // 手机号
    private String phone;
    
    // 邮箱
    private String email;
    
    // 在线状态: 0-离线, 1-在线
    private Integer onlineStatus;
    
    // 最后在线时间
    private Date lastOnlineTime;
    
    // 当前连接的Netty节点ID
    private String currentNodeId;
    
    // 地理位置纬度
    private Double latitude;
    
    // 地理位置经度
    private Double longitude;
    
    // 注册时间
    private Date createTime;
    
    // 更新时间
    private Date updateTime;

    private String currentServer;

    private Integer status;
}