package com.common.service;

import com.common.exception.SystemException;
import com.common.model.dto.LoginUserDto;
import com.common.model.dto.RegisterUserDto;
import com.common.model.dto.ResetPwdUserDto;
import com.common.model.dto.SearchUserDto;
import com.common.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.common.model.vo.EchoUserVo;
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
public interface IUserService extends IService<User> {

    /**
     * 用户登录
     * @param user
     */
    String login(LoginUserDto user) throws SystemException;

    Map<String, Object> getUserInfo(Integer userId) throws SystemException;

    /**
     * 分页查询用户数据
     * @param pageNo
     * @param pageSize
     * @param userDto
     * @return
     */
    Map<String,Object> queryUserList(Integer pageNo, Integer pageSize, SearchUserDto userDto);

    List<String> queryRoles(Integer userId);

    ResultData saveRoles(String username,List<String> roles);

    /**
     * 修改用户状态
     * @param id
     */
    ResultData updateUserStatus(Integer id) throws SystemException;

    /**
     * 用户数据回显
     * @param id
     * @return
     */
    EchoUserVo echoUserById(Integer id);

    /**
     * 修改用户
     * @param userVo
     * @return
     */
    ResultData updateUser(EchoUserVo userVo) throws SystemException;

    /**
     * 删除用户
     * @param id
     * @return
     */
    ResultData deleteUser(Integer id) throws SystemException;

    /**
     * 用户注册
     * @param user
     * @return
     */
    ResultData register(RegisterUserDto user) throws SystemException;

    /**
     * 重置用户密码
     * @param userDto
     * @return
     */
    ResultData resetPassword(ResetPwdUserDto userDto) throws SystemException;

    /**
     * 获取用户角色标识
     * @param userId
     * @return
     */
    List<String> queryRoleCode(int userId);
}
