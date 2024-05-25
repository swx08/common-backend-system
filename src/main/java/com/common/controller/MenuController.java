package com.common.controller;


import cn.hutool.core.lang.tree.Tree;
import com.common.exception.SystemException;
import com.common.model.dto.AddMenuDto;
import com.common.model.entity.Menu;
import com.common.model.entity.Role;
import com.common.response.ResultData;
import com.common.service.IMenuService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
@RequestMapping("/menu")
@Api(tags = "菜单管理模块")
public class MenuController {

    private final IMenuService menuService;

    /**
     * 查询所有菜单数据
     * @param
     * @return
     */
    @GetMapping("/list")
    @ApiOperation(value = "查询所有菜单数据")
    public ResultData queryMenuList() {
        List<Tree<String>> menuList = menuService.queryMenuList();
        return ResultData.success(menuList);
    }

    /**
     * 查询当前角色所拥有的所有的菜单以及按钮权限
     * @param
     * @return
     */
    @PostMapping("/role/query")
    @ApiOperation(value = "查询角色的权限数据")
    public ResultData queryRoleMenuList(@RequestBody Role role) {
        List<Integer> menuIdList = menuService.queryRoleMenuList(role);
        return ResultData.success(menuIdList);
    }

    /**
     * 新增菜单
     * @param
     * @return
     */
    @PostMapping("/add")
    @ApiOperation(value = "新增菜单")
    public ResultData addMenu(@RequestBody AddMenuDto menu) throws SystemException {
        menuService.addMenu(menu);
        return ResultData.success();
    }

    /**
     * 修改菜单
     * @param
     * @return
     */
    @PutMapping("/update")
    @ApiOperation(value = "修改菜单")
    public ResultData updateMenu(@RequestBody AddMenuDto menu) throws SystemException {
        menuService.updateMenu(menu);
        return ResultData.success();
    }

    /**
     * 菜单数据回显
     * @param
     * @return
     */
    @GetMapping("/echo")
    @ApiOperation(value = "菜单数据回显")
    public ResultData echoMenu(@RequestParam("id") Integer id) throws SystemException {
        AddMenuDto menu = menuService.echoMenu(id);
        return ResultData.success(menu);
    }

    /**
     * 删除菜单
     * @param
     * @return
     */
    @DeleteMapping("/delete")
    @ApiOperation(value = "删除菜单")
    public ResultData deleteMenu(@RequestParam("id") Integer id) throws SystemException {
        menuService.deleteMenu(id);
        return ResultData.success();
    }
}
