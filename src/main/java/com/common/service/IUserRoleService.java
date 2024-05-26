package com.common.service;

import com.common.model.entity.UserRole;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author author
 * @since 2024-04-21
 */
public interface IUserRoleService extends IService<UserRole> {

    /**
     * 删除用户角色关联表数据
     * @param id
     */
    void removeByRoleId(Integer id);

    /**
     * 删除用户角色关联表的数据
     * @param id
     */
    void removeByUserId(Integer id);
}
