package com.mi.im.search.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 搜索服务接口
 */
public interface SearchService {
    
    /**
     * 搜索消息
     * @param keyword 关键词
     * @param userId 用户ID
     * @param page 页码
     * @param pageSize 每页大小
     * @return 搜索结果列表
     */
    List<Map<String, Object>> searchMessages(String keyword, Long userId, int page, int pageSize) throws IOException;
    
    /**
     * 搜索用户
     * @param keyword 关键词
     * @param currentUserId 当前用户ID
     * @param page 页码
     * @param pageSize 每页大小
     * @return 搜索结果列表
     */
    List<Map<String, Object>> searchUsers(String keyword, Long currentUserId, int page, int pageSize) throws IOException;
    
    /**
     * 搜索群组
     * @param keyword 关键词
     * @param userId 用户ID
     * @param page 页码
     * @param pageSize 每页大小
     * @return 搜索结果列表
     */
    List<Map<String, Object>> searchGroups(String keyword, Long userId, int page, int pageSize) throws IOException;
}