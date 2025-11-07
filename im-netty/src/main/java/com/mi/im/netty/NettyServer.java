package com.mi.im.netty;

import com.mi.im.netty.config.NettyConfig;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class NettyServer implements ApplicationRunner {
    
    private static final Logger logger = LoggerFactory.getLogger(NettyServer.class);
    
    @Value("${netty.port:8080}")
    private int port;
    
    @Value("${zookeeper.address:localhost:2181}")
    private String zookeeperAddress;
    
    @Value("${netty.server.path:/im/netty/servers}")
    private String serverPath;
    
    @Autowired
    private NettyConfig nettyConfig;
    
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private CuratorFramework client;
    private String serverNodePath;
    
    @Override
    public void run(ApplicationArguments args) throws Exception {
        start();
    }
    
    public void start() throws Exception {
        // 初始化Zookeeper客户端
        initZookeeper();
        
        // 创建事件循环组
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();
        
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childHandler(nettyConfig.channelInitializer());
            
            // 绑定端口
            ChannelFuture future = bootstrap.bind(port).sync();
            logger.info("Netty服务器启动成功，端口: {}", port);
            
            // 注册到Zookeeper
            registerToZookeeper();
            
            // 启动心跳检测
            startHeartbeat();
            
            // 等待关闭
            future.channel().closeFuture().sync();
        } finally {
            shutdown();
        }
    }
    
    // 初始化Zookeeper
    private void initZookeeper() {
        client = CuratorFrameworkFactory.builder()
                .connectString(zookeeperAddress)
                .retryPolicy(new ExponentialBackoffRetry(1000, 3))
                .build();
        client.start();
    }
    
    // 注册到Zookeeper
    private void registerToZookeeper() throws Exception {
        // 获取本机IP
        String ip = InetAddress.getLocalHost().getHostAddress();
        String serverInfo = ip + ":" + port;
        
        // 确保父节点存在
        if (client.checkExists().forPath(serverPath) == null) {
            client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(serverPath);
        }
        
        // 创建临时节点
        serverNodePath = client.create().withMode(CreateMode.EPHEMERAL_SEQUENTIAL)
                .forPath(serverPath + "/server-", serverInfo.getBytes());
        
        logger.info("注册到Zookeeper成功: {}", serverNodePath);
    }
    
    // 启动心跳检测
    private void startHeartbeat() {
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(() -> {
            try {
                // 检查Zookeeper连接
                if (client != null && client.getZookeeperClient().isConnected()) {
                    // 更新节点数据（心跳）
                    client.setData().forPath(serverNodePath, "".getBytes());
                }
            } catch (Exception e) {
                logger.error("心跳检测失败: {}", e.getMessage());
            }
        }, 30, 30, TimeUnit.SECONDS);
    }
    
    // 关闭服务器
    public void shutdown() {
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
        if (client != null) {
            client.close();
        }
        logger.info("Netty服务器关闭");
    }
}