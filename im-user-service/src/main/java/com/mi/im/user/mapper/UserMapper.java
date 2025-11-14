package com.mi.im.user.mapper;

import com.mi.im.common.model.User;
import com.mi.im.common.model.dto.UserDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

@Mapper
public interface UserMapper {
    UserDTO selectById(Long userId);
    UserDTO selectByPhone(String phone);
    int updateStatus(@Param("userId") Long userId, @Param("status") Integer status);
    int updateServerAddress(@Param("userId") String userId, @Param("serverAddress") String serverAddress);
}