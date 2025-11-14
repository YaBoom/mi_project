package com.mi.im.common.model;

import lombok.Data;
import org.elasticsearch.common.geo.GeoPoint;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.GeoPointField;

import java.io.Serializable;
import java.util.Date;


/**
 * 用户实体类
 */
@Data
@Document(indexName="im_users_zhu",type="person",shards=3,replicas=1,refreshInterval="-1")
public class User implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    // 用户ID
    private String userId;
    @Field(type = FieldType.Keyword)
    // 用户名
    private String username;
    @Field(type = FieldType.Keyword)
    // 密码
    private String password;
    @Field(type = FieldType.Keyword)
    // 昵称
    private String nickname;
    @Field(type = FieldType.Keyword)
    // 头像URL
    private String avatar;
    @Field(type = FieldType.Keyword)
    // 手机号
    private String phone;
    @Field(type = FieldType.Keyword)
    // 邮箱
    private String email;
    @Field
    // 在线状态: 0-离线, 1-在线
    private Integer onlineStatus;
    @Field
    private Date lastOnlineTime;
    @Field
    private String currentNodeId;

    @GeoPointField
    private GeoPoint location;
    @Field
    // 注册时间
    private Date createTime;
    @Field
    // 更新时间
    private Date updateTime;
    @Field
    private String currentServer;
    @Field
    private Integer status;
}