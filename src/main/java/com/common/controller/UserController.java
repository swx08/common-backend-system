package com.common.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.common.exception.SystemException;
import com.common.model.dto.LoginUserDto;
import com.common.model.dto.SearchUserDto;
import com.common.model.entity.User;
import com.common.response.ResultData;
import com.common.service.IUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @Author: @weixueshi
 * @Create: 2024/4/21 - 13:29
 * @Version: v1.0
 */
@Slf4j
@CrossOrigin
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
    public ResultData register(@RequestBody User user) {
        userService.save(user);
        return ResultData.success();
    }

    /**
     * 用户密码登录
     * @param user
     * @return
     * @throws SystemException
     */
    @PostMapping("/login")
    @ApiOperation(value = "用户密码登录")
    public ResultData login(@RequestBody LoginUserDto user) throws SystemException {
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
    public ResultData userInfo(@RequestParam("token") String token) throws SystemException {
        String userId = (String) StpUtil.getLoginIdByToken(token);
        Map<String,Object> map = userService.getUserInfo(Integer.parseInt(userId));
        return ResultData.success(map);
    }

    /**
     * 获取用户角色数据
     * @param userId
     * @return
     * @throws SystemException
     */
    @GetMapping("/roles/{userId}")
    @ApiOperation(value = "获取用户角色数据")
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
    public ResultData saveRoles(@PathVariable("username") String username,
                                @RequestBody List<String> roles) {
        userService.saveRoles(username,roles);
        return ResultData.success();
    }

    /**
     * 修改用户状态
     * @param
     * @return
     */
    @PutMapping("/update/status/{id}")
    @ApiOperation(value = "修改用户状态")
    public ResultData updateUserStatus(@PathVariable("id") Integer id){
        return userService.updateUserStatus(id);
    }

    /**
     * 用户退出登录
     * @return
     * @throws SystemException
     */
    @PostMapping("/logout")
    @ApiOperation(value = "用户退出登录")
    public ResultData logout(@RequestParam("token") String token) throws SystemException {
        StpUtil.logoutByTokenValue(token);
        return ResultData.success();
    }
}
