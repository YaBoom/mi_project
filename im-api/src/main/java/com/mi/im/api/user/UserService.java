package com.mi.im.api.user;

import com.mi.im.common.model.User;

/**
 * 用户服务接口
 */
public interface UserService {
    
    /**
     * 根据用户ID获取用户信息
     * @param userId 用户ID
     * @return 用户信息
     */
    User getUserById(Long userId);
    
    /**
     * 根据手机号获取用户信息
     * @param phone 手机号
     * @return 用户信息
     */
    User getUserByPhone(String phone);
    
    /**
     * 更新用户状态
     * @param userId 用户ID
     * @param status 状态
     * @return 是否成功
     */
    boolean updateUserStatus(Long userId, Integer status);
    
    /**
     * 更新用户服务器地址
     * @param userId 用户ID
     * @param serverAddress 服务器地址
     * @return 是否成功
     */
    boolean updateUserServerAddress(Long userId, String serverAddress);
    
    /**
     * 检查是否在黑名单中
     * @param userId 用户ID
     * @param blackUserId 黑名单用户ID
     * @return 是否在黑名单中
     */
    boolean checkBlacklist(Long userId, Long blackUserId);
    
    /**
     * 添加黑名单
     * @param userId 用户ID
     * @param blackUserId 黑名单用户ID
     * @return 是否成功
     */
    boolean addBlacklist(Long userId, Long blackUserId);
}