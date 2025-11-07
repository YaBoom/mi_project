package com.mi.im.group.controller;

import com.mi.im.group.entity.Group;
import com.mi.im.group.entity.GroupMember;
import com.mi.im.group.service.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 群组控制器
 */
@RestController
@RequestMapping("/api/group")
public class GroupController {

    @Autowired
    private GroupService groupService;

    /**
     * 创建群组
     */
    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createGroup(
            @RequestBody Map<String, Object> request) {
        Group group = new Group();
        group.setGroupName((String) request.get("groupName"));
        group.setGroupAvatar((String) request.get("groupAvatar"));
        group.setDescription((String) request.get("description"));
        group.setOwnerId(Long.valueOf(request.get("ownerId").toString()));
        
        @SuppressWarnings("unchecked")
        List<Long> memberList = (List<Long>) request.get("memberList");
        
        Map<String, Object> result = groupService.createGroup(group, memberList);
        return ResponseEntity.ok(result);
    }

    /**
     * 获取群组信息
     */
    @GetMapping("/{groupId}")
    public ResponseEntity<Group> getGroup(@PathVariable Long groupId) {
        Group group = groupService.getGroupById(groupId);
        return ResponseEntity.ok(group);
    }

    /**
     * 更新群组信息
     */
    @PutMapping("/update")
    public ResponseEntity<Map<String, Boolean>> updateGroup(@RequestBody Group group) {
        boolean result = groupService.updateGroup(group);
        Map<String, Boolean> valueMap = new HashMap<>();
        valueMap.put("success", result);
        return ResponseEntity.ok(valueMap);
    }

    /**
     * 解散群组
     */
    @DeleteMapping("/dissolve/{groupId}")
    public ResponseEntity<Map<String, Boolean>> dissolveGroup(
            @PathVariable Long groupId,
            @RequestParam Long operatorId) {
        boolean result = groupService.dissolveGroup(groupId, operatorId);
        Map<String, Boolean> valueMap = new HashMap<>();
        valueMap.put("success", result);
        return ResponseEntity.ok(valueMap);
    }

    /**
     * 获取用户群组列表
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Group>> getUserGroups(@PathVariable Long userId) {
        List<Group> groups = groupService.getUserGroups(userId);
        return ResponseEntity.ok(groups);
    }

    /**
     * 邀请成员加入群组
     */
    @PostMapping("/{groupId}/invite")
    public ResponseEntity<Map<String, Object>> inviteMembers(
            @PathVariable Long groupId,
            @RequestBody Map<String, Object> request) {
        Long inviterId = Long.valueOf(request.get("inviterId").toString());
        
        @SuppressWarnings("unchecked")
        List<Long> inviteeIds = (List<Long>) request.get("inviteeIds");
        
        Map<String, Object> result = groupService.inviteMembers(groupId, inviterId, inviteeIds);
        return ResponseEntity.ok(result);
    }

    /**
     * 移除群成员
     */
    @DeleteMapping("/{groupId}/members")
    public ResponseEntity<Map<String, Boolean>> removeMembers(
            @PathVariable Long groupId,
            @RequestBody Map<String, Object> request) {
        Long operatorId = Long.valueOf(request.get("operatorId").toString());
        
        @SuppressWarnings("unchecked")
        List<Long> memberIds = (List<Long>) request.get("memberIds");
        
        boolean result = groupService.removeMembers(groupId, operatorId, memberIds);
        Map<String, Boolean> valueMap = new HashMap<>();
        valueMap.put("success", result);
        return ResponseEntity.ok(valueMap);
    }

    /**
     * 退出群组
     */
    @PostMapping("/{groupId}/quit")
    public ResponseEntity<Map<String, Boolean>> quitGroup(
            @PathVariable Long groupId,
            @RequestBody Map<String, Object> request) {
        Long userId = Long.valueOf(request.get("userId").toString());
        
        boolean result = groupService.quitGroup(groupId, userId);
        Map<String, Boolean> valueMap = new HashMap<>();
        valueMap.put("success", result);
        return ResponseEntity.ok(valueMap);
    }

    /**
     * 获取群组成员列表
     */
    @GetMapping("/{groupId}/members")
    public ResponseEntity<Map<String, Object>> getGroupMembers(
            @PathVariable Long groupId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "50") Integer pageSize) {
        Map<String, Object> result = groupService.getGroupMembers(groupId, page, pageSize);
        return ResponseEntity.ok(result);
    }

    /**
     * 更新群成员角色
     */
    @PutMapping("/{groupId}/members/role")
    public ResponseEntity<Map<String, Boolean>> updateMemberRole(
            @PathVariable Long groupId,
            @RequestBody Map<String, Object> request) {
        Long operatorId = Long.valueOf(request.get("operatorId").toString());
        Long memberId = Long.valueOf(request.get("memberId").toString());
        Integer role = Integer.valueOf(request.get("role").toString());
        
        boolean result = groupService.updateMemberRole(groupId, operatorId, memberId, role);
        Map<String, Boolean> valueMap = new HashMap<>();
        valueMap.put("success", result);
        return ResponseEntity.ok(valueMap);
    }

    /**
     * 获取群成员信息
     */
    @GetMapping("/{groupId}/members/{userId}")
    public ResponseEntity<GroupMember> getGroupMember(
            @PathVariable Long groupId,
            @PathVariable Long userId) {
        GroupMember member = groupService.getGroupMember(groupId, userId);
        return ResponseEntity.ok(member);
    }

    /**
     * 检查用户是否为群成员
     */
    @GetMapping("/{groupId}/members/{userId}/check")
    public ResponseEntity<Map<String, Boolean>> checkGroupMember(
            @PathVariable Long groupId,
            @PathVariable Long userId) {
        boolean isMember = groupService.isGroupMember(groupId, userId);
        Map<String, Boolean> valueMap = new HashMap<>();
        valueMap.put("success", isMember);
        return ResponseEntity.ok(valueMap);
    }

    /**
     * 检查用户是否为群管理员
     */
    @GetMapping("/{groupId}/members/{userId}/check-manager")
    public ResponseEntity<Map<String, Boolean>> checkGroupManager(
            @PathVariable Long groupId,
            @PathVariable Long userId) {
        boolean isManager = groupService.isGroupManager(groupId, userId);
        Map<String, Boolean> valueMap = new HashMap<>();
        valueMap.put("success", isManager);
        return ResponseEntity.ok(valueMap);
    }
}