package com.common.service;

import com.common.model.entity.Role;
import com.baomidou.mybatisplus.extension.service.IService;

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
     * @param roleName
     * @return
     */
    Map<String, Object> queryRoleListByPage(Integer pageNo, Integer pageSize, String roleName);

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
}
