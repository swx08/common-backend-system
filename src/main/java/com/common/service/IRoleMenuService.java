package com.common.service;

import com.common.model.entity.RoleMenu;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author author
 * @since 2024-04-21
 */
public interface IRoleMenuService extends IService<RoleMenu> {

    /**
     * 删除角色菜单关联表数据
     * @param id
     */
    void removeByRoleId(Integer id);

    /**
     * 将角色菜单关联表中的数据删除
     * @param id
     */
    void removeByMenuId(Integer id);
}
