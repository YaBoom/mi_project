package com.mi.im.message.service.mapper;

import com.mi.im.common.model.Message;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

/**
 * 消息数据访问接口
 */
@Mapper
public interface MessageMapper {

    /**
     * 插入消息
     * @param message 消息对象
     * @return 影响行数
     */
    int insert(Message message);

    /**
     * 获取历史消息
     * @param userId 发送者ID
     * @param targetId 接收者ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param limit 限制条数
     * @return 消息列表
     */
    List<Message> selectHistoryMessages(String userId, String targetId, Long startTime, Long endTime, Integer limit);

    /**
     * 获取用户的离线消息
     * @param userId 用户ID
     * @return 离线消息列表
     */
    List<Message> selectOfflineMessages(String userId);

    /**
     * 标记消息为已读
     * @param messageId 消息ID
     * @return 影响行数
     */
    int updateMessageStatus(String messageId);

    /**
     * 批量标记消息为已读
     * @param messageIds 消息ID列表
     * @return 影响行数
     */
    int batchUpdateMessageStatus(List<String> messageIds);

    /**
     * 根据消息ID获取消息
     * @param messageId 消息ID
     * @return 消息对象
     */
    Message selectByMessageId(String messageId);

    /**
     * 获取未读消息数量
     * @param userId 用户ID
     * @return 未读消息数量
     */
    int countUnreadMessages(String userId);
}