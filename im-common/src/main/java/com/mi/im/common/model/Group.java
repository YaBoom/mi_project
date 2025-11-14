package com.mi.im.common.model;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 群组实体类
 */
@Data
public class Group implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;                // 群组ID
    private String groupName;       // 群组名称
    private String groupAvatar;     // 群组头像
    private String description;     // 群组描述
    private Long ownerId;           // 群主ID
    private Integer memberCount;    // 成员数量
    private Integer maxMembers;     // 最大成员数
    private Integer status;         // 状态：0-正常，1-解散
    private Date createTime;        // 创建时间
    private Date updateTime;        // 更新时间
    private String extInfo;         // 扩展信息
}