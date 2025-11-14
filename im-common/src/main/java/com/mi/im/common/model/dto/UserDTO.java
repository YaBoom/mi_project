package com.mi.im.common.model.dto;

import com.fasterxml.jackson.annotation.JsonView;
import com.mi.im.common.model.views.Views;

import java.util.Date;

/**
 * @className: UserDTO
 * @Description: TODO
 * @author: zhuyt
 * @date: 25/11/10 9:02
 */
public class UserDTO {

    private static final long serialVersionUID = 1L;
    @JsonView(Views.Public.class)
    // 用户ID
    private String userId;


    @JsonView(Views.Summary.class)
    // 用户名
    private String username;


    @JsonView(Views.Admin.class)
    // 密码
    private String password;
    @JsonView(Views.Public.class)
    // 昵称
    private String nickname;
    @JsonView(Views.Public.class)
    // 头像URL
    private String avatar;
    @JsonView(Views.Admin.class)
    // 手机号
    private String phone;
    @JsonView(Views.Public.class)
    // 邮箱
    private String email;
    @JsonView(Views.Public.class)
    // 在线状态: 0-离线, 1-在线
    private Integer onlineStatus;
    @JsonView(Views.Admin.class)
    // 最后在线时间
    private Date lastOnlineTime;
    @JsonView(Views.Admin.class)
    // 当前连接的Netty节点ID
    private String currentNodeId;
    @JsonView(Views.Admin.class)
    // 地理位置纬度
    private Double latitude;
    @JsonView(Views.Admin.class)
    // 地理位置经度
    private Double longitude;
    @JsonView(Views.Public.class)
    // 注册时间
    private Date createTime;
    @JsonView(Views.Public.class)
    // 更新时间
    private Date updateTime;
    @JsonView(Views.Public.class)
    private String currentServer;
    @JsonView(Views.Public.class)
    private Integer status;
}
