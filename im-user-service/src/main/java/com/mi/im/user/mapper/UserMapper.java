package com.mi.im.user.mapper;

import com.mi.im.common.model.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {
    User selectById(Long userId);
    User selectByPhone(String phone);
    int updateStatus(@Param("userId") String userId, @Param("status") Integer status);
    int updateServerAddress(@Param("userId") String userId, @Param("serverAddress") String serverAddress);
}