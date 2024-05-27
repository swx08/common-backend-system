package com.common.service;

import com.common.exception.SystemException;
import com.common.model.dto.SearchRoleDto;
import com.common.model.entity.Role;
import com.baomidou.mybatisplus.extension.service.IService;
import com.common.response.ResultData;

import java.util.List;
import java.util.Map;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author author
 * @since 2024-04-21
 */
public interface IRoleService extends IService<Role> {

    /**
     * 分页查询角色数据
     * @param pageNo
     * @param pageSize
     * @param roleDto
     * @return
     */
    Map<String, Object> queryRoleListByPage(Integer pageNo, Integer pageSize, SearchRoleDto roleDto);

    /**
     * 查询所有角色数据
     * @return
     */
    List<String> queryRoleList();

    /**
     * 分配权限
     * @param roleId
     * @param menuIds
     */
    void savePermission(Integer roleId, List<Integer> menuIds);

    /**
     * 新增角色
     * @param role
     */
    ResultData saveRole(Role role) throws SystemException;

    /**
     * 修改角色
     * @param role
     */
    ResultData updateRole(Role role) throws SystemException;

    /**
     * 删除角色
     * @param id
     */
    ResultData deleteRole(Integer id) throws SystemException;

    /**
     * 修改角色状态
     * @param id
     */
    ResultData updateRoleStatus(Integer id) throws SystemException;
}
