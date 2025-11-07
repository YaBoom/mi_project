package com.mi.im.group.mapper;

import com.mi.im.group.entity.GroupMember;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 群组成员数据访问接口
 */
@Mapper
public interface GroupMemberMapper {

    /**
     * 插入群成员信息
     * @param member 群成员信息
     * @return 插入成功的行数
     */
    int insert(GroupMember member);

    /**
     * 根据群组ID和用户ID查询群成员信息
     * @param groupId 群组ID
     * @param userId 用户ID
     * @return 群成员信息
     */
    GroupMember selectByGroupIdAndUserId(@Param("groupId") Long groupId, @Param("userId") Long userId);

    /**
     * 根据群组ID查询群成员列表
     * @param groupId 群组ID
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 群成员列表
     */
    List<GroupMember> selectByGroupId(@Param("groupId") Long groupId, @Param("offset") int offset, @Param("limit") int limit);

    /**
     * 统计群组成员数量
     * @param groupId 群组ID
     * @return 成员数量
     */
    int countByGroupId(Long groupId);

    /**
     * 根据群组ID和用户ID列表查询群成员信息
     * @param groupId 群组ID
     * @param userIds 用户ID列表
     * @return 群成员信息列表
     */
    List<GroupMember> selectByGroupIdAndUserIds(@Param("groupId") Long groupId, @Param("userIds") List<Long> userIds);

    /**
     * 更新群成员角色
     * @param groupId 群组ID
     * @param userId 用户ID
     * @param role 角色
     * @return 更新成功的行数
     */
    int updateMemberRole(@Param("groupId") Long groupId, @Param("userId") Long userId, @Param("role") Integer role);

    /**
     * 更新群成员状态
     * @param groupId 群组ID
     * @param userId 用户ID
     * @param status 状态
     * @return 更新成功的行数
     */
    int updateMemberStatus(@Param("groupId") Long groupId, @Param("userId") Long userId, @Param("status") Integer status);

    /**
     * 更新群组所有成员状态
     * @param groupId 群组ID
     * @param status 状态
     * @return 更新成功的行数
     */
    int updateMemberStatusByGroupId(@Param("groupId") Long groupId, @Param("status") Integer status);

    /**
     * 批量删除群成员
     * @param groupId 群组ID
     * @param userIds 用户ID列表
     * @return 删除成功的行数
     */
    int deleteMembers(@Param("groupId") Long groupId, @Param("userIds") List<Long> userIds);

    /**
     * 查询用户加入的所有群组ID
     * @param userId 用户ID
     * @return 群组ID列表
     */
    List<Long> selectGroupIdsByUserId(Long userId);

    /**
     * 根据用户ID查询群成员信息列表
     * @param userId 用户ID
     * @return 群成员信息列表
     */
    List<GroupMember> selectByUserId(Long userId);
}