package com.mi.im.message.service;

import com.mi.im.common.model.Message;

import java.util.List;

public interface MessageService {
    boolean saveMessage(Message message);
    List<Message> getHistoryMessages(String userId, String targetId, Long startTime, Long endTime, Integer limit);
    List<Message> getOfflineMessages(String userId);
    boolean markMessageAsRead(String messageId);
    boolean saveMessageToSearch(Message message);
}