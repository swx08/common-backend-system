package com.common.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.http.HttpRequest;
import com.common.exception.SystemException;
import com.common.model.dto.LoginUserDto;
import com.common.model.dto.RegisterUserDto;
import com.common.model.dto.ResetPwdUserDto;
import com.common.model.dto.SearchUserDto;
import com.common.model.entity.Role;
import com.common.model.entity.User;
import com.common.model.vo.EchoRoleVo;
import com.common.model.vo.EchoUserVo;
import com.common.response.ResponseCodeEnum;
import com.common.response.ResultData;
import com.common.service.IUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * @Author: @weixueshi
 * @Create: 2024/4/21 - 13:29
 * @Version: v1.0
 */
@Slf4j
@CrossOrigin
@Validated
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Api(tags = "用户管理模块")
public class UserController {

    private final IUserService userService;

    /**
     * 分页查询用户数据
     * @param
     * @return
     */
    @GetMapping("/list/{pageNo}/{pageSize}")
    @ApiOperation(value = "分页查询用户数据")
    @SaCheckPermission(value = "permission:user:list",orRole = {"admin","common","test"})
    public ResultData queryUserList(@PathVariable("pageNo") Integer pageNo,
                                    @PathVariable("pageSize") Integer pageSize,
                                    SearchUserDto userDto) {
        Map<String,Object> map = userService.queryUserList(pageNo,pageSize,userDto);
        return ResultData.success(map);
    }

    /**
     * 用户注册
     * @param
     * @return
     */
    @PostMapping("/register")
    @ApiOperation(value = "用户注册")
    public ResultData register(@RequestBody @Valid RegisterUserDto userDto) throws SystemException {
        return userService.register(userDto);
    }

    /**
     * 用户密码登录
     * @param user
     * @return
     * @throws SystemException
     */
    @PostMapping("/login")
    @ApiOperation(value = "用户密码登录")
    public ResultData login(@RequestBody @Valid LoginUserDto user) throws SystemException {
       String token = userService.login(user);
        return ResultData.success(token);
    }

    /**
     * 登录获取用户信息
     * @return
     * @throws SystemException
     */
    @PostMapping("/info")
    @ApiOperation(value = "登录获取用户信息")
    public ResultData userInfo(HttpServletRequest request) throws SystemException {
        Map<String,Object> map = userService.getUserInfo(request);
        return ResultData.success(map);
    }

    /**
     * 获取角色名称
     * @param userId
     * @return
     * @throws SystemException
     */
    @GetMapping("/roles/{userId}")
    @ApiOperation(value = "获取角色名称")
    @SaCheckPermission(value = "permission:user:assign",orRole = {"admin"})
    public ResultData queryRoles(@PathVariable("userId") Integer userId) throws SystemException {
        List<String> roles = userService.queryRoles(userId);
        return ResultData.success(roles);
    }

    /**
     * 保存分配的用户角色
     * @param
     * @return
     * @throws SystemException
     */
    @PostMapping("/save/roles/{username}")
    @ApiOperation(value = "保存分配的用户角色")
    @SaCheckPermission(value = "permission:user:assign",orRole = {"admin"})
    public ResultData saveRoles(@PathVariable("username") String username,
                                @RequestBody List<String> roles) {
        return userService.saveRoles(username,roles);
    }

    /**
     * 修改用户
     * @param
     * @return
     */
    @PutMapping("/update")
    @ApiOperation(value = "修改用户")
    @SaCheckPermission(value = "permission:user:update",orRole = {"admin"})
    public ResultData updateUser(@RequestBody @Valid EchoUserVo userVo) throws SystemException {
        return userService.updateUser(userVo);
    }

    /**
     * 修改用户状态
     * @param
     * @return
     */
    @PutMapping("/update/status/{id}")
    @ApiOperation(value = "修改用户状态")
    @SaCheckPermission(value = "permission:user:update",orRole = {"admin"})
    public ResultData updateUserStatus(@PathVariable("id") Integer id) throws SystemException {
        return userService.updateUserStatus(id);
    }

    /**
     * 用户数据回显
     * @param
     * @return
     */
    @GetMapping("/echo/{id}")
    @ApiOperation(value = "用户数据回显")
    public ResultData echoUserById(@PathVariable("id") Integer id) {
        EchoUserVo echoUserVo = userService.echoUserById(id);
        return ResultData.success(echoUserVo);
    }

    /**
     * 用户退出登录
     * @return
     * @throws SystemException
     */
    @PostMapping("/logout")
    @ApiOperation(value = "用户退出登录")
    public ResultData logout(HttpServletRequest request) throws SystemException {
        //从请求头中获取token
        String token = request.getHeader("Authorization");
        if(StringUtils.isBlank(token)){
            throw new SystemException(ResponseCodeEnum.NEED_LOGIN);
        }
        StpUtil.logoutByTokenValue(token);
        return ResultData.success();
    }

    /**
     * 重置用户密码
     * @return
     * @throws SystemException
     */
    @PostMapping("/reset/pwd")
    @ApiOperation(value = "重置用户密码")
    @SaCheckRole("admin")
    @SaCheckPermission(value = "permission:user:resetpwd",orRole = {"admin"})
    public ResultData resetPassword(@RequestBody @Valid ResetPwdUserDto userDto) throws SystemException {
        return userService.resetPassword(userDto);
    }

    /**
     * 删除用户
     * @param
     * @return
     */
    @DeleteMapping("/delete/{id}")
    @ApiOperation(value = "删除用户")
    @SaCheckPermission(value = "permission:user:delete",orRole = {"admin"})
    public ResultData deleteUser(@PathVariable("id") Integer id) throws SystemException {
        return userService.deleteUser(id);
    }

    /**
     * 批量删除用户
     * @param
     * @return
     */
    @DeleteMapping("/batch/delete/{ids}")
    @ApiOperation(value = "批量删除用户")
    @SaCheckPermission(value = "permission:user:delete",orRole = {"admin"})
    public ResultData batchDeleteUser(@PathVariable("ids") List<Integer> ids) throws SystemException {
        for (Integer id : ids) {
            userService.deleteUser(id);
        }
        return ResultData.success();
    }

}
