package com.mi.im.group.entity;

import lombok.Data;
import java.io.Serializable;
import java.util.Date;

/**
 * 群组成员实体类
 */
@Data
public class GroupMember implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;                // 成员ID
    private Long groupId;           // 群组ID
    private Long userId;            // 用户ID
    private String nickname;        // 在群内的昵称
    private Integer role;           // 角色：0-普通成员，1-管理员，2-群主
    private Integer joinType;       // 加入方式：0-邀请，1-申请，2-直接加入
    private Integer status;         // 状态：0-正常，1-已退出，2-已禁言
    private Date joinTime;          // 加入时间
    private Date updateTime;        // 更新时间
    private String extInfo;         // 扩展信息
}