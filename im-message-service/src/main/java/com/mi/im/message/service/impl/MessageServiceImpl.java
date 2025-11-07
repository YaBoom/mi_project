package com.mi.im.message.service.impl;

import com.alibaba.fastjson.JSON;
import com.mi.im.common.model.Message;
import com.mi.im.message.service.MessageService;
import com.mi.im.message.service.mapper.MessageMapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * 消息服务实现类
 */
@Service
public class MessageServiceImpl implements MessageService {

    @Autowired
    private MessageMapper messageMapper;
    
    @Autowired
    private RabbitTemplate rabbitTemplate;
    
    @Value("${rabbitmq.queue.offline-message}")
    private String offlineMessageQueue;
    
    @Value("${rabbitmq.exchange.message-broadcast}")
    private String messageBroadcastExchange;

    @Override
    @Transactional
    public boolean saveMessage(Message message) {
        // 设置消息ID和发送时间
        message.setMessageId(UUID.randomUUID().toString());
        message.setSendTime(new Date());
        message.setStatus(1); // 已发送
        
        // 保存消息
        int result = messageMapper.insert(message);
        return result > 0;
    }

    @Override
    public List<Message> getHistoryMessages(String userId, String targetId, Long startTime, Long endTime, Integer limit) {
        return messageMapper.selectHistoryMessages(userId, targetId, startTime, endTime, limit);
    }

    @Override
    public List<Message> getOfflineMessages(String userId) {
        return messageMapper.selectOfflineMessages(userId);
    }

    @Override
    public boolean markMessageAsRead(String messageId) {
        int result = messageMapper.updateMessageStatus(messageId);
        return result > 0;
    }

    @Override
    public boolean saveMessageToSearch(Message message) {
        // TODO: 实现消息搜索保存逻辑，可以集成Elasticsearch
        // 这里暂时返回true表示成功
        try {
            // 将消息发送到RabbitMQ，由专门的消费者处理消息索引
            rabbitTemplate.convertAndSend(messageBroadcastExchange, "message.search", JSON.toJSONString(message));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


}