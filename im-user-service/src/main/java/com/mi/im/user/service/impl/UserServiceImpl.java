package com.mi.im.user.service.impl;

import com.mi.im.api.user.UserService;
import com.mi.im.common.model.User;
import com.mi.im.user.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl implements UserService {
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    private static final String USER_CACHE_PREFIX = "user:";
    private static final long USER_CACHE_TTL = 3600; // 1小时


    @Override
    public User getUserById(Long userId) {
        // 先从缓存获取
        String cacheKey = USER_CACHE_PREFIX + userId;
        User user = (User) redisTemplate.opsForValue().get(cacheKey);
        if (user != null) {
            return user;
        }

        // 缓存未命中，从数据库查询
        user = userMapper.selectById(userId);
        if (user != null) {
            // 存入缓存
            redisTemplate.opsForValue().set(cacheKey, user, USER_CACHE_TTL, TimeUnit.SECONDS);
        }

        return user;
    }

    @Override
    public User getUserByPhone(String phone) {
        return userMapper.selectByPhone(phone);
    }

    @Override
    public boolean updateUserStatus(Long userId, Integer status) {
        // 更新数据库
        int result = userMapper.updateStatus(userId.toString(), status);

        // 更新缓存
        if (result > 0) {
            String cacheKey = USER_CACHE_PREFIX + userId;
            User user = (User) redisTemplate.opsForValue().get(cacheKey);
            if (user != null) {
                user.setStatus(status);
                redisTemplate.opsForValue().set(cacheKey, user, USER_CACHE_TTL, TimeUnit.SECONDS);
            }
        }

        return result > 0;
    }

    @Override
    public boolean updateUserServerAddress(Long userId, String serverAddress) {
        int result = userMapper.updateServerAddress(userId.toString(), serverAddress);

        // 更新缓存
        if (result > 0) {
            String cacheKey = USER_CACHE_PREFIX + userId;
            User user = (User) redisTemplate.opsForValue().get(cacheKey);
            if (user != null) {
                user.setCurrentServer(serverAddress);
                redisTemplate.opsForValue().set(cacheKey, user, USER_CACHE_TTL, TimeUnit.SECONDS);
            }
        }

        return result > 0;
    }

    @Override
    public boolean checkBlacklist(Long userId, Long blackUserId) {
        // 从Redis或数据库查询黑名单
        String blacklistKey = "blacklist:" + userId;
        return redisTemplate.opsForSet().isMember(blacklistKey, blackUserId);
    }

    @Override
    public boolean addBlacklist(Long userId, Long blackUserId) {
        String blacklistKey = "blacklist:" + userId;
        redisTemplate.opsForSet().add(blacklistKey, blackUserId);
        // 设置永久过期时间
        redisTemplate.persist(blacklistKey);
        return true;
    }
}

