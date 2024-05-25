package com.common.service;

import com.common.exception.SystemException;
import com.common.model.entity.User;
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
public interface IUserService extends IService<User> {

    /**
     * 用户登录
     * @param user
     */
    String login(User user) throws SystemException;

    Map<String, Object> getUserInfo(Integer userId) throws SystemException;

    /**
     * 分页查询用户数据
     * @param pageNo
     * @param pageSize
     * @param username
     * @return
     */
    Map<String,Object> queryUserList(Integer pageNo, Integer pageSize, String username);

    List<String> queryRoles(Integer userId);

    void saveRoles(String username,List<String> roles);
}
