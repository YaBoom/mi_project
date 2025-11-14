package com.mi.im.api.group;


import com.mi.im.common.model.Group;
import com.mi.im.common.model.GroupMember;

import java.util.List;
import java.util.Map;

/**
 * 群组服务接口
 */
public interface GroupService {

    /**
     * 创建群组
     * @param group 群组信息
     * @param memberList 初始成员列表
     * @return 创建结果
     */
    Map<String, Object> createGroup(Group group, List<Long> memberList);

    /**
     * 获取群组信息
     * @param groupId 群组ID
     * @return 群组信息
     */
    Group getGroupById(Long groupId);

    /**
     * 更新群组信息
     * @param group 群组信息
     * @return 更新结果
     */
    boolean updateGroup(Group group);

    /**
     * 解散群组
     * @param groupId 群组ID
     * @param operatorId 操作人ID
     * @return 解散结果
     */
    boolean dissolveGroup(Long groupId, Long operatorId);

    /**
     * 获取用户加入的群组列表
     * @param userId 用户ID
     * @return 群组列表
     */
    List<Group> getUserGroups(Long userId);

    /**
     * 邀请用户加入群组
     * @param groupId 群组ID
     * @param inviterId 邀请人ID
     * @param inviteeIds 被邀请人ID列表
     * @return 邀请结果
     */
    Map<String, Object> inviteMembers(Long groupId, Long inviterId, List<Long> inviteeIds);

    /**
     * 移除群成员
     * @param groupId 群组ID
     * @param operatorId 操作人ID
     * @param memberIds 被移除成员ID列表
     * @return 移除结果
     */
    boolean removeMembers(Long groupId, Long operatorId, List<Long> memberIds);

    /**
     * 退出群组
     * @param groupId 群组ID
     * @param userId 用户ID
     * @return 退出结果
     */
    boolean quitGroup(Long groupId, Long userId);

    /**
     * 获取群组成员列表
     * @param groupId 群组ID
     * @param page 页码
     * @param pageSize 每页大小
     * @return 成员列表
     */
    Map<String, Object> getGroupMembers(Long groupId, Integer page, Integer pageSize);

    /**
     * 更新群成员角色
     * @param groupId 群组ID
     * @param operatorId 操作人ID
     * @param memberId 成员ID
     * @param role 角色
     * @return 更新结果
     */
    boolean updateMemberRole(Long groupId, Long operatorId, Long memberId, Integer role);

    /**
     * 获取群成员信息
     * @param groupId 群组ID
     * @param userId 用户ID
     * @return 群成员信息
     */
    GroupMember getGroupMember(Long groupId, Long userId);

    /**
     * 批量获取群成员信息
     * @param groupId 群组ID
     * @param userIds 用户ID列表
     * @return 群成员信息列表
     */
    List<GroupMember> getGroupMembersByIds(Long groupId, List<Long> userIds);

    /**
     * 检查用户是否为群成员
     * @param groupId 群组ID
     * @param userId 用户ID
     * @return 是否为群成员
     */
    boolean isGroupMember(Long groupId, Long userId);

    /**
     * 检查用户是否为群主或管理员
     * @param groupId 群组ID
     * @param userId 用户ID
     * @return 是否为群主或管理员
     */
    boolean isGroupManager(Long groupId, Long userId);
}