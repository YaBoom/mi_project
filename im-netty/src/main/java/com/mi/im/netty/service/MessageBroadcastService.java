package com.mi.im.netty.service;

import com.mi.im.common.model.Message;
import com.mi.im.netty.handler.WebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MessageBroadcastService {
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    /**
     * 广播群组消息
     */
    public void broadcastGroupMessage(Message message) {
        // 从Redis获取群组成员列表
        String groupMemberKey = "group:member:" + message.getReceiverId();
        List<Object> objects = redisTemplate.opsForList().range(groupMemberKey, 0, -1);

        List<String> memberIds = objects.stream()
                .filter(obj -> obj instanceof String)
                .map(obj -> (String) obj)
                .collect(Collectors.toList());
        
        if (memberIds != null) {
            for (String memberId : memberIds) {
                // 跳过发送者自己
                if (!memberId.equals(message.getSenderId())) {
                    sendToUser(memberId, message);
                }
            }
        }
    }
    
    /**
     * 发送消息给指定用户
     */
    public boolean sendToUser(String userId, Message message) {
        return WebSocketHandler.USER_CHANNEL_MAP.containsKey(userId) &&
               WebSocketHandler.USER_CHANNEL_MAP.get(userId).isActive();
    }
}