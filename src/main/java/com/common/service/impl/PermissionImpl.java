package com.common.service.impl;

import cn.dev33.satoken.stp.StpInterface;
import com.common.exception.SystemException;
import com.common.service.IUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * ClassName: PermissionImpl
 * Package: com.common.service.impl
 * Description:
 *
 * @Author: @weixueshi
 * @Create: 2024/5/28 - 14:19
 * @Version: v1.0
 */

/**
 * 权限接口实现类
 */
@Slf4j
@Component
public class PermissionImpl implements StpInterface {

    @Autowired
    private IUserService userService;

    /**
     * 权限校验
     * @param loginId
     * @param loginType
     * @return
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        log.info("获取用户权限列表............");
        // 返回权限列表
        try {
            String userId = (String) loginId;
            // 获取用户权限列表
            List<String> permissionList = userService.getUserPermissions(Integer.parseInt(userId));
            log.info("成功获取用户权限列表............");
            return permissionList;
        } catch (SystemException e) {
            log.error("获取用户权限列表失败", e);
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    /**
     * 角色校验
     * @param loginId
     * @param loginType
     * @return
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        log.info("获取用户角色标识............");
        // 返回角色标识
        String userId = (String) loginId;
        // 获取用户角色标识
        List<String> roleList = userService.queryRoleCode(Integer.parseInt(userId));
        log.info("成功获取用户角色标识............");
        return roleList;
    }
}
