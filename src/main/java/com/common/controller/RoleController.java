package com.common.controller;


import com.common.exception.SystemException;
import com.common.model.dto.AddMenuDto;
import com.common.model.dto.SearchRoleDto;
import com.common.model.entity.Role;
import com.common.model.vo.EchoRoleVo;
import com.common.response.ResultData;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.common.service.IRoleService;
import org.springframework.beans.BeanUtils;
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
@Api(tags = "角色管理模块")
public class RoleController {

    private final IRoleService roleService;

    /**
     * 分页查询所有角色数据
     * @param
     * @return
     */
    @GetMapping("/list/{pageNo}/{pageSize}")
    @ApiOperation(value = "分页查询角色数据")
    public ResultData queryRoleListByPage(@PathVariable("pageNo") Integer pageNo,
                                          @PathVariable("pageSize") Integer pageSize,
                                          SearchRoleDto roleDto) {
        Map<String,Object> map = roleService.queryRoleListByPage(pageNo,pageSize,roleDto);
        return ResultData.success(map);
    }

    /**
     * 查询所有角色名称
     * @param
     * @return
     */
    @GetMapping("/list")
    @ApiOperation(value = "查询所有角色名称")
    public ResultData queryRoleList() {
        List<String> roleList = roleService.queryRoleList();
        return ResultData.success(roleList);
    }

    /**
     * 新增角色
     * @param
     * @return
     */
    @PostMapping("/add")
    @ApiOperation(value = "新增角色")
    public ResultData saveRole(@RequestBody Role role) throws SystemException {
        roleService.saveRole(role);
        return ResultData.success();
    }

    /**
     * 角色分配权限
     * @param
     * @return
     */
    @PostMapping("/save/permission/{roleId}")
    @ApiOperation(value = "角色分配权限")
    public ResultData savePermission(@PathVariable("roleId") Integer roleId,
                               @RequestBody List<Integer> menuIds) {
        roleService.savePermission(roleId,menuIds);
        return ResultData.success();
    }

    /**
     * 修改角色
     * @param
     * @return
     */
    @PutMapping("/update")
    @ApiOperation(value = "修改角色")
    public ResultData updateRole(@RequestBody Role role) throws SystemException {
        roleService.updateRole(role);
        return ResultData.success();
    }

    /**
     * 角色数据回显
     * @param
     * @return
     */
    @GetMapping("/echo")
    @ApiOperation(value = "角色数据回显")
    public ResultData echoRole(@RequestParam("id") Integer id) {
        Role role = roleService.getById(id);
        EchoRoleVo echoRoleVo = new EchoRoleVo();
        BeanUtils.copyProperties(role,echoRoleVo);
        return ResultData.success(echoRoleVo);
    }

    /**
     * 删除角色
     * @param
     * @return
     */
//    @DeleteMapping("/delete")
//    @ApiOperation(value = "删除角色")
//    public ResultData deleteMenu(@RequestParam("id") Integer id) throws SystemException {
//        menuService.deleteMenu(id);
//        return ResultData.success();
//    }
}
