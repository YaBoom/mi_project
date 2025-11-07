package com.mi.im.netty.handler;

import com.alibaba.fastjson.JSON;
import com.mi.im.common.model.Message;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WebSocketHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
    
    private static final Logger logger = LoggerFactory.getLogger(WebSocketHandler.class);
    
    // 存储用户ID与Channel的映射
    public static final Map<String, Channel> USER_CHANNEL_MAP = new ConcurrentHashMap<>();
    
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
        // 解析收到的消息
        String text = msg.text();
        Message message = JSON.parseObject(text, Message.class);
        
        logger.info("收到消息: {}", message);
        
        // 处理消息（根据消息类型和接收者进行分发）
        handleMessage(ctx, message);
    }
    
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.info("客户端连接成功: {}", ctx.channel().remoteAddress());
    }
    
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        // 客户端断开连接，清理资源
        String userId = getUserIdByChannel(ctx.channel());
        if (userId != null) {
            USER_CHANNEL_MAP.remove(userId);
            logger.info("用户[{}]断开连接", userId);
            // 更新用户在线状态
            updateUserStatus(userId, 2); // 2-离线
        }
        ctx.close();
    }
    
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        // 处理心跳检测
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.READER_IDLE) {
                // 读空闲，断开连接
                logger.info("心跳超时，断开连接: {}", ctx.channel().remoteAddress());
                ctx.close();
            }
        }
    }
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("发生异常: {}", cause.getMessage());
        ctx.close();
    }
    
    // 处理消息
    private void handleMessage(ChannelHandlerContext ctx, Message message) {
        // 1. 身份验证（如果是首次连接）
        if (message.getMessageType() == 0) { // 0-身份验证消息
            String userId = message.getSenderId();
            USER_CHANNEL_MAP.put(userId, ctx.channel());
            updateUserStatus(userId, 1); // 1-在线
            
            // 发送连接成功消息
            Message response = new Message();
            response.setMessageType(6); // 6-系统消息
            response.setContent("连接成功");
            sendMessage(ctx.channel(), response);
            return;
        }
        
        // 2. 检查黑名单
        if (isBlocked(message.getSenderId(), message.getReceiverId())) {
            message.setIsBlocked(true);
            // 发送拦截通知给发送者
            sendMessage(ctx.channel(), message);
            return;
        }
        
        // 3. 发送消息
        if (message.getIsGroupMessage()) {
            // 群组消息处理
            sendGroupMessage(message);
        } else {
            // 私聊消息处理
            sendPrivateMessage(message);
        }
    }
    
    // 发送私聊消息
    private void sendPrivateMessage(Message message) {
        Channel channel = USER_CHANNEL_MAP.get(message.getReceiverId());
        if (channel != null && channel.isActive()) {
            sendMessage(channel, message);
            message.setStatus(3); // 3-已送达
        } else {
            // 用户不在线，存储到数据库
            saveOfflineMessage(message);
        }
        
        // 更新发送者的消息状态
        message.setStatus(2); // 2-已发送
        sendMessage(USER_CHANNEL_MAP.get(message.getSenderId()), message);
        
        // 异步保存消息
        saveMessageAsync(message);
    }
    
    // 发送群组消息
    private void sendGroupMessage(Message message) {
        // TODO: 实现群组消息扩散机制
        // 1. 获取群组成员列表
        // 2. 过滤在线成员
        // 3. 发送消息给在线成员
        // 4. 存储离线消息
    }
    
    // 发送消息
    private void sendMessage(Channel channel, Message message) {
        if (channel != null && channel.isActive()) {
            channel.writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(message)));
        }
    }
    
    // 根据Channel获取用户ID
    private String getUserIdByChannel(Channel channel) {
        for (Map.Entry<String, Channel> entry : USER_CHANNEL_MAP.entrySet()) {
            if (entry.getValue() == channel) {
                return entry.getKey();
            }
        }
        return null;
    }
    
    // 更新用户状态
    private void updateUserStatus(String userId, Integer status) {
        // TODO: 调用用户服务更新状态
        // 可以通过OpenFeign调用用户服务
    }
    
    // 检查是否在黑名单中
    private boolean isBlocked(String senderId, String receiverId) {
        // TODO: 查询黑名单服务
        return false;
    }
    
    // 保存离线消息
    private void saveOfflineMessage(Message message) {
        // TODO: 保存到数据库或Redis
    }
    
    // 异步保存消息
    private void saveMessageAsync(Message message) {
        // TODO: 通过RabbitMQ异步保存消息
    }
}
