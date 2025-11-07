package com.mi.im.group.mapper;

import com.mi.im.group.entity.Group;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 群组数据访问接口
 */
@Mapper
public interface GroupMapper {

    /**
     * 插入群组信息
     * @param group 群组信息
     * @return 插入成功的行数
     */
    int insert(Group group);

    /**
     * 根据ID查询群组信息
     * @param id 群组ID
     * @return 群组信息
     */
    Group selectById(Long id);

    /**
     * 更新群组信息
     * @param group 群组信息
     * @return 更新成功的行数
     */
    int updateById(Group group);

    /**
     * 查询用户加入的群组列表
     * @param userId 用户ID
     * @return 群组列表
     */
    List<Group> selectUserGroups(Long userId);

    /**
     * 根据ID列表查询群组信息
     * @param ids 群组ID列表
     * @return 群组信息列表
     */
    List<Group> selectByIds(List<Long> ids);

    /**
     * 更新群组状态
     * @param id 群组ID
     * @param status 状态
     * @return 更新成功的行数
     */
    int updateStatus(Long id, Integer status);

    /**
     * 搜索群组
     * @param keyword 关键词
     * @param page 页码
     * @param pageSize 每页大小
     * @return 群组列表
     */
    List<Group> searchGroups(@Param("keyword") String keyword, @Param("offset") int offset, @Param("limit") int pageSize);

    /**
     * 统计搜索结果数量
     * @param keyword 关键词
     * @return 数量
     */
    int countSearchGroups(String keyword);
}