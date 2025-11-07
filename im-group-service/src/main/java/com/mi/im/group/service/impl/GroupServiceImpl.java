package com.mi.im.group.service.impl;

import com.mi.im.group.entity.Group;
import com.mi.im.group.entity.GroupMember;
import com.mi.im.group.mapper.GroupMapper;
import com.mi.im.group.mapper.GroupMemberMapper;
import com.mi.im.group.service.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 群组服务实现类
 */
@Service
public class GroupServiceImpl implements GroupService {

    @Autowired
    private GroupMapper groupMapper;

    @Autowired
    private GroupMemberMapper groupMemberMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Value("${group.max-members}")
    private Integer maxMembers;

    @Value("${group.default-avatar}")
    private String defaultAvatar;

    @Value("${group.invite-expire-time}")
    private Long inviteExpireTime;

    // Redis Key前缀
    private static final String GROUP_INFO_KEY = "im:group:info:";
    private static final String GROUP_MEMBER_KEY = "im:group:member:";
    private static final String GROUP_MEMBER_LIST_KEY = "im:group:members:";

    @Override
    @Transactional
    public Map<String, Object> createGroup(Group group, List<Long> memberList) {
        // 设置群组默认值
        group.setMaxMembers(maxMembers);
        if (group.getGroupAvatar() == null || group.getGroupAvatar().isEmpty()) {
            group.setGroupAvatar(defaultAvatar);
        }
        group.setMemberCount(memberList.size() + 1); // +1 是因为群主也算成员
        group.setStatus(0); // 0-正常
        group.setCreateTime(new Date());
        group.setUpdateTime(new Date());

        // 插入群组信息
        groupMapper.insert(group);
        Long groupId = group.getId();

        // 插入群主信息
        GroupMember ownerMember = new GroupMember();
        ownerMember.setGroupId(groupId);
        ownerMember.setUserId(group.getOwnerId());
        ownerMember.setRole(2); // 2-群主
        ownerMember.setJoinType(0); // 0-邀请
        ownerMember.setStatus(0); // 0-正常
        ownerMember.setJoinTime(new Date());
        ownerMember.setUpdateTime(new Date());
        groupMemberMapper.insert(ownerMember);

        // 插入成员信息
        if (memberList != null && !memberList.isEmpty()) {
            for (Long memberId : memberList) {
                GroupMember member = new GroupMember();
                member.setGroupId(groupId);
                member.setUserId(memberId);
                member.setRole(0); // 0-普通成员
                member.setJoinType(0); // 0-邀请
                member.setStatus(0); // 0-正常
                member.setJoinTime(new Date());
                member.setUpdateTime(new Date());
                groupMemberMapper.insert(member);
            }
        }

        // 更新缓存
        cacheGroupInfo(group);

        Map<String, Object> result = new HashMap<>();
        result.put("groupId", groupId);
        result.put("group", group);
        result.put("success", true);
        return result;
    }

    @Override
    public Group getGroupById(Long groupId) {
        // 先从缓存获取
        String key = GROUP_INFO_KEY + groupId;
        Group group = (Group) redisTemplate.opsForValue().get(key);
        if (group != null) {
            return group;
        }

        // 从数据库获取
        group = groupMapper.selectById(groupId);
        if (group != null) {
            // 更新缓存
            cacheGroupInfo(group);
        }
        return group;
    }

    @Override
    @Transactional
    public boolean updateGroup(Group group) {
        group.setUpdateTime(new Date());
        int result = groupMapper.updateById(group);
        
        if (result > 0) {
            // 更新缓存
            cacheGroupInfo(group);
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public boolean dissolveGroup(Long groupId, Long operatorId) {
        // 检查是否为群主
        Group group = groupMapper.selectById(groupId);
        if (group == null || !group.getOwnerId().equals(operatorId)) {
            return false;
        }

        // 更新群组状态为解散
        group.setStatus(1); // 1-解散
        group.setUpdateTime(new Date());
        groupMapper.updateById(group);

        // 更新所有成员状态为已退出
        groupMemberMapper.updateMemberStatusByGroupId(groupId, 1); // 1-已退出

        // 清除缓存
        clearGroupCache(groupId);

        return true;
    }

    @Override
    public List<Group> getUserGroups(Long userId) {
        return groupMapper.selectUserGroups(userId);
    }

    @Override
    @Transactional
    public Map<String, Object> inviteMembers(Long groupId, Long inviterId, List<Long> inviteeIds) {
        // 检查群组是否存在
        Group group = groupMapper.selectById(groupId);
        if (group == null || group.getStatus() != 0) {
            throw new RuntimeException("群组不存在或已解散");
        }

        // 检查邀请人是否为群成员
        if (!isGroupMember(groupId, inviterId)) {
            throw new RuntimeException("邀请人不是群成员");
        }

        // 检查群成员数量是否超过限制
        if (group.getMemberCount() + inviteeIds.size() > group.getMaxMembers()) {
            throw new RuntimeException("群成员数量超过限制");
        }

        // 邀请结果统计
        int successCount = 0;
        List<Long> failedIds = new ArrayList<>();

        for (Long inviteeId : inviteeIds) {
            // 检查被邀请人是否已经是群成员
            if (isGroupMember(groupId, inviteeId)) {
                failedIds.add(inviteeId);
                continue;
            }

            // 插入群成员信息
            GroupMember member = new GroupMember();
            member.setGroupId(groupId);
            member.setUserId(inviteeId);
            member.setRole(0); // 0-普通成员
            member.setJoinType(0); // 0-邀请
            member.setStatus(0); // 0-正常
            member.setJoinTime(new Date());
            member.setUpdateTime(new Date());
            groupMemberMapper.insert(member);

            // 更新缓存
            cacheGroupMember(member);
            successCount++;
        }

        // 更新群组成员数量
        if (successCount > 0) {
            group.setMemberCount(group.getMemberCount() + successCount);
            group.setUpdateTime(new Date());
            groupMapper.updateById(group);
            cacheGroupInfo(group);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("successCount", successCount);
        result.put("failedIds", failedIds);
        return result;
    }

    @Override
    @Transactional
    public boolean removeMembers(Long groupId, Long operatorId, List<Long> memberIds) {
        // 检查操作人是否为群主或管理员
        if (!isGroupManager(groupId, operatorId)) {
            return false;
        }

        // 检查是否包含群主
        Group group = groupMapper.selectById(groupId);
        if (memberIds.contains(group.getOwnerId())) {
            return false;
        }

        // 删除成员
        int deleteCount = groupMemberMapper.deleteMembers(groupId, memberIds);
        
        if (deleteCount > 0) {
            // 更新群组成员数量
            group.setMemberCount(group.getMemberCount() - deleteCount);
            group.setUpdateTime(new Date());
            groupMapper.updateById(group);
            
            // 更新缓存
            cacheGroupInfo(group);
            for (Long memberId : memberIds) {
                clearGroupMemberCache(groupId, memberId);
            }
        }

        return deleteCount > 0;
    }

    @Override
    @Transactional
    public boolean quitGroup(Long groupId, Long userId) {
        // 检查群组是否存在
        Group group = groupMapper.selectById(groupId);
        if (group == null || group.getStatus() != 0) {
            return false;
        }

        // 检查是否为群主（群主不能退出，只能解散）
        if (group.getOwnerId().equals(userId)) {
            return false;
        }

        // 更新成员状态为已退出
        int result = groupMemberMapper.updateMemberStatus(groupId, userId, 1); // 1-已退出
        
        if (result > 0) {
            // 更新群组成员数量
            group.setMemberCount(group.getMemberCount() - 1);
            group.setUpdateTime(new Date());
            groupMapper.updateById(group);
            
            // 更新缓存
            cacheGroupInfo(group);
            clearGroupMemberCache(groupId, userId);
        }

        return result > 0;
    }

    @Override
    public Map<String, Object> getGroupMembers(Long groupId, Integer page, Integer pageSize) {
        int offset = (page - 1) * pageSize;
        List<GroupMember> members = groupMemberMapper.selectByGroupId(groupId, offset, pageSize);
        int total = groupMemberMapper.countByGroupId(groupId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("members", members);
        result.put("total", total);
        result.put("page", page);
        result.put("pageSize", pageSize);
        result.put("pages", (total + pageSize - 1) / pageSize);
        
        return result;
    }

    @Override
    @Transactional
    public boolean updateMemberRole(Long groupId, Long operatorId, Long memberId, Integer role) {
        // 检查操作人是否为群主
        Group group = groupMapper.selectById(groupId);
        if (!group.getOwnerId().equals(operatorId)) {
            return false;
        }

        // 更新角色
        int result = groupMemberMapper.updateMemberRole(groupId, memberId, role);
        
        if (result > 0) {
            // 清除缓存
            clearGroupMemberCache(groupId, memberId);
        }
        
        return result > 0;
    }

    @Override
    public GroupMember getGroupMember(Long groupId, Long userId) {
        // 先从缓存获取
        String key = GROUP_MEMBER_KEY + groupId + ":" + userId;
        GroupMember member = (GroupMember) redisTemplate.opsForValue().get(key);
        if (member != null) {
            return member;
        }

        // 从数据库获取
        member = groupMemberMapper.selectByGroupIdAndUserId(groupId, userId);
        if (member != null) {
            // 更新缓存
            cacheGroupMember(member);
        }
        return member;
    }

    @Override
    public List<GroupMember> getGroupMembersByIds(Long groupId, List<Long> userIds) {
        return groupMemberMapper.selectByGroupIdAndUserIds(groupId, userIds);
    }

    @Override
    public boolean isGroupMember(Long groupId, Long userId) {
        GroupMember member = getGroupMember(groupId, userId);
        return member != null && member.getStatus() == 0;
    }

    @Override
    public boolean isGroupManager(Long groupId, Long userId) {
        GroupMember member = getGroupMember(groupId, userId);
        return member != null && member.getStatus() == 0 && (member.getRole() == 1 || member.getRole() == 2);
    }

    // 缓存群组信息
    private void cacheGroupInfo(Group group) {
        String key = GROUP_INFO_KEY + group.getId();
        redisTemplate.opsForValue().set(key, group, 30, TimeUnit.MINUTES);
    }

    // 缓存群成员信息
    private void cacheGroupMember(GroupMember member) {
        String key = GROUP_MEMBER_KEY + member.getGroupId() + ":" + member.getUserId();
        redisTemplate.opsForValue().set(key, member, 30, TimeUnit.MINUTES);
        
        // 添加到群成员列表缓存
        String listKey = GROUP_MEMBER_LIST_KEY + member.getGroupId();
        redisTemplate.opsForSet().add(listKey, member.getUserId());
        redisTemplate.expire(listKey, 30, TimeUnit.MINUTES);
    }

    // 清除群成员缓存
    private void clearGroupMemberCache(Long groupId, Long userId) {
        String key = GROUP_MEMBER_KEY + groupId + ":" + userId;
        redisTemplate.delete(key);
        
        // 从群成员列表缓存中移除
        String listKey = GROUP_MEMBER_LIST_KEY + groupId;
        redisTemplate.opsForSet().remove(listKey, userId);
    }

    // 清除群组缓存
    private void clearGroupCache(Long groupId) {
        String infoKey = GROUP_INFO_KEY + groupId;
        redisTemplate.delete(infoKey);
        
        // 清除群成员列表
        String listKey = GROUP_MEMBER_LIST_KEY + groupId;
        redisTemplate.delete(listKey);
    }
}