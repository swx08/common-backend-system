package com.common.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.common.exception.SystemException;
import com.common.model.entity.User;
import com.common.response.ResultData;
import com.common.service.IUserService;
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
public class UserController {

    private final IUserService userService;

    /**
     * 分页查询用户数据
     * @param
     * @return
     */
    @GetMapping("/list/{pageNo}/{pageSize}")
    public ResultData queryUserList(@PathVariable("pageNo") Integer pageNo,
                                    @PathVariable("pageSize") Integer pageSize,
                                    String username) {
        Map<String,Object> map = userService.queryUserList(pageNo,pageSize,username);
        return ResultData.success(map);
    }

    @PostMapping("/login")
    public ResultData login(@RequestBody User user) throws SystemException {
       String token = userService.login(user);
        return ResultData.success(token);
    }

    @GetMapping("/info")
    public ResultData userInfo() throws SystemException {
        int userId = StpUtil.getLoginIdAsInt();
        Map<String,Object> map = userService.getUserInfo(userId);
        return ResultData.success(map);
    }

    @GetMapping("/roles/{userId}")
    public ResultData queryRoles(@PathVariable("userId") Integer userId) throws SystemException {
        List<String> roles = userService.queryRoles(userId);
        return ResultData.success(roles);
    }

    /**
     * 保存已分配的用户角色
     * @param
     * @return
     * @throws SystemException
     */
    @PostMapping("/save/roles/{username}")
    public ResultData saveRoles(@PathVariable("username") String username,
                                @RequestBody List<String> roles) {
        userService.saveRoles(username,roles);
        return ResultData.success();
    }

    /**
     * 新增用户
     * @param
     * @return
     */
    @PostMapping("/save")
    public ResultData saveUser(@RequestBody User user) {
        userService.save(user);
        return ResultData.success();
    }
}
