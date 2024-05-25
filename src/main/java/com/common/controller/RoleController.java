package com.common.controller;


import com.common.model.entity.Role;
import com.common.response.ResultData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.common.service.IRoleService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author author
 * @since 2024-04-21
 */
@Slf4j
@CrossOrigin
@RestController
@RequiredArgsConstructor
@RequestMapping("/role")
public class RoleController {

    private final IRoleService roleService;

    /**
     * 分页查询所有角色数据
     * @param
     * @return
     */
    @GetMapping("/list/{pageNo}/{pageSize}")
    public ResultData queryRoleListByPage(@PathVariable("pageNo") Integer pageNo,
                                          @PathVariable("pageSize") Integer pageSize,
                                          String roleName) {
        Map<String,Object> map = roleService.queryRoleListByPage(pageNo,pageSize,roleName);
        return ResultData.success(map);
    }

    /**
     * 查询所有角色数据
     * @param
     * @return
     */
    @GetMapping("/list")
    public ResultData queryRoleList() {
        List<String> roleList = roleService.queryRoleList();
        return ResultData.success(roleList);
    }

    /**
     * 新增角色
     * @param
     * @return
     */
    @PostMapping("/save")
    public ResultData saveRole(@RequestBody Role role) {
        roleService.save(role);
        return ResultData.success();
    }

    /**
     * 分配权限
     * @param
     * @return
     */
    @PostMapping("/save/permission/{roleId}")
    public ResultData savePermission(@PathVariable("roleId") Integer roleId,
                               @RequestBody List<Integer> menuIds) {
        roleService.savePermission(roleId,menuIds);
        return ResultData.success();
    }
}
