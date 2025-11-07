package com.mi.im.netty.config;
import com.mi.im.netty.handler.WebSocketHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.net.InetAddress;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Netty配置类
 */
@Configuration
public class NettyConfig {
    private static final Logger logger = LoggerFactory.getLogger(NettyConfig.class);

    @Value("${netty.port:8080}")
    private int port;

    @Value("${netty.websocket.path:/ws}")
    private String webSocketPath;

    @Value("${netty.idle.read-time:60}")
    private int readIdleTime;

    @Value("${netty.idle.write-time:30}")
    private int writeIdleTime;

    @Value("${netty.idle.all-time:0}")
    private int allIdleTime;

    @Value("${zookeeper.address:localhost:2181}")
    private String zookeeperAddress;

    @Value("${zookeeper.base-path:/im-netty}")
    private String zookeeperBasePath;

    @Value("${redis.host:localhost}")
    private String redisHost;

    @Value("${redis.port:6379}")
    private int redisPort;

    private CuratorFramework curatorFramework;
    private String nodePath;
    private String serverAddress;
    private AtomicInteger onlineCount = new AtomicInteger(0);

    /**
     * 初始化Zookeeper客户端
     */
    @PostConstruct
    public void init() {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        curatorFramework = CuratorFrameworkFactory.newClient(zookeeperAddress, retryPolicy);
        curatorFramework.start();

        try {
            // 创建基础路径
            Stat stat = curatorFramework.checkExists().forPath(zookeeperBasePath);
            if (stat == null) {
                curatorFramework.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT)
                        .forPath(zookeeperBasePath);
            }

            // 获取本地地址
            serverAddress = InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            logger.error("初始化Zookeeper失败", e);
        }
    }

    /**
     * 注册到Zookeeper
     */
    public void registerToZookeeper(int port) {
        try {
            // 创建临时节点
            nodePath = zookeeperBasePath + "/" + serverAddress + ":" + port;
            String data = "{\"address\":\"" + serverAddress + ":" + port + ",\"onlineCount\":0}";
            curatorFramework.create().withMode(CreateMode.EPHEMERAL).forPath(nodePath, data.getBytes());
            logger.info("注册到Zookeeper成功: {}", nodePath);
        } catch (Exception e) {
            logger.error("注册到Zookeeper失败", e);
        }
    }

    /**
     * 获取动态端口
     */
    public int getPort() {
        // 如果配置了固定端口则使用固定端口，否则动态分配
        if (port > 0) {
            return port;
        }

        // 从Redis动态分配端口（示例算法）
        // 实际实现中应该使用Redis来协调端口分配
        return 8000 + new Random().nextInt(1000);
    }

    /**
     * 增加在线人数
     */
    public void incrementOnlineCount() {
        int count = onlineCount.incrementAndGet();
        updateOnlineCount(count);
    }

    /**
     * 减少在线人数
     */
    public void decrementOnlineCount() {
        int count = onlineCount.decrementAndGet();
        if (count < 0) {
            count = 0;
            onlineCount.set(0);
        }
        updateOnlineCount(count);
    }

    /**
     * 更新在线人数
     */
    private void updateOnlineCount(int count) {
        try {
            String data = "{\"address\":\"" + serverAddress + ":" + port + ",\"onlineCount\":" + count + "}";
            curatorFramework.setData().forPath(nodePath, data.getBytes());
        } catch (Exception e) {
            logger.error("更新Zookeeper节点数据失败", e);
        }
    }

    /**
     * 获取在线人数
     */
    public int getOnlineCount() {
        return onlineCount.get();
    }

    /**
     * 关闭Zookeeper客户端
     */
    @PreDestroy
    public void close() {
        if (curatorFramework != null) {
            curatorFramework.close();
        }
    }
    
    /**
     * 创建ChannelInitializer，用于配置Netty的通道处理器链
     */
    @Bean
    public ChannelInitializer<SocketChannel> channelInitializer() {
        return new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                // 获取通道的pipeline
                ChannelPipeline pipeline = ch.pipeline();
                
                // 添加HTTP编解码器
                pipeline.addLast("http-codec", new HttpServerCodec());
                // 添加HTTP聚合器，用于将HTTP消息聚合为FullHttpRequest或FullHttpResponse
                pipeline.addLast("http-aggregator", new HttpObjectAggregator(65536));
                // 添加块写入处理器，支持大文件传输
                pipeline.addLast("http-chunked", new ChunkedWriteHandler());
                // 添加空闲状态处理器，用于心跳检测
                pipeline.addLast("idle-handler", new IdleStateHandler(
                        readIdleTime, writeIdleTime, allIdleTime, TimeUnit.SECONDS));
                // 添加WebSocket协议处理器
                pipeline.addLast("websocket-protocol", new WebSocketServerProtocolHandler(webSocketPath));
                
                // 注意：具体的业务处理器需要在实际的处理器类中添加
                pipeline.addLast("message-handler", new WebSocketHandler());
            }
        };
    }
    
    // getter方法
    public String getWebSocketPath() {
        return webSocketPath;
    }

    public int getReadIdleTime() {
        return readIdleTime;
    }

    public int getWriteIdleTime() {
        return writeIdleTime;
    }

    public int getAllIdleTime() {
        return allIdleTime;
    }
}
