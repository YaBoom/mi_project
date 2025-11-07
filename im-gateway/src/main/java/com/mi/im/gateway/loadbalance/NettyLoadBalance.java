package com.mi.im.gateway.loadbalance;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Netty服务负载均衡器
 */
@Component
public class NettyLoadBalance {

    @Value("${zookeeper.address:localhost:2181}")
    private String zookeeperAddress;
    
    @Value("${zookeeper.base-path:/im-netty}")
    private String zookeeperBasePath;

    private CuratorFramework curatorFramework;
    private Map<String, Integer> nodeOnlineCountMap = new ConcurrentHashMap<>();

    public void init() {
        ExponentialBackoffRetry retryPolicy = new ExponentialBackoffRetry(1000, 3);
        curatorFramework = CuratorFrameworkFactory.newClient(zookeeperAddress, retryPolicy);
        curatorFramework.start();
    }

    /**
     * 获取在线人数最少的Netty节点
     */
    public String getBestNode() {
        try {
            // 获取所有Netty节点
            List<String> nodes = curatorFramework.getChildren().forPath(zookeeperBasePath);
            if (nodes == null || nodes.isEmpty()) {
                return null;
            }

            // 找出在线人数最少的节点
            String bestNode = null;
            int minCount = Integer.MAX_VALUE;

            for (String node : nodes) {
                byte[] data = curatorFramework.getData().forPath(zookeeperBasePath + "/" + node);
                String nodeData = new String(data);
                
                // 解析在线人数
                int onlineCount = parseOnlineCount(nodeData);
                nodeOnlineCountMap.put(node, onlineCount);

                if (onlineCount < minCount) {
                    minCount = onlineCount;
                    bestNode = node;
                }
            }

            return bestNode;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 解析节点数据中的在线人数
     */
    private int parseOnlineCount(String nodeData) {
        // 简化实现，实际应该使用JSON解析
        try {
            int start = nodeData.indexOf("onlineCount") + 13;
            int end = nodeData.indexOf("}", start);
            return Integer.parseInt(nodeData.substring(start, end));
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 获取所有可用节点
     */
    public List<String> getAllNodes() {
        try {
            return curatorFramework.getChildren().forPath(zookeeperBasePath);
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}