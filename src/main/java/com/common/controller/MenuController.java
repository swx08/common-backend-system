package com.common.controller;


import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.hutool.core.lang.tree.Tree;
import com.common.exception.SystemException;
import com.common.model.dto.AddMenuDto;
import com.common.model.dto.SearchMenuDto;
import com.common.model.entity.Menu;
import com.common.model.entity.Role;
import com.common.model.vo.PrimeVueMenuVO;
import com.common.response.ResultData;
import com.common.service.IMenuService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
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
@Validated
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
    @SaCheckPermission(value = "permission:menu:list",orRole = {"admin","common","test"})
    public ResultData queryMenuList() {
        List<Tree<String>> menuList = menuService.queryMenuList();
        return ResultData.success(menuList);
    }

    /**
     * 适配primeVue ui框架的树形菜单数据
     * @param
     * @return
     */
    @GetMapping("/primeVue/list")
    @ApiOperation(value = "查询所有菜单数据(primeVue框架)")
    @SaCheckPermission(value = "permission:menu:list",orRole = {"admin","common","test"})
    public ResultData queryMenuListWithPrimeVue() {
        List<Tree<String>> menuList = menuService.queryMenuListWithPrimeVue();
        return ResultData.success(menuList);
    }

    /**
     * 分配权限时获取菜单树形数据
     * @param
     * @return
     */
    @GetMapping("/permission/list")
    @ApiOperation(value = "分配权限时获取菜单树形数据")
    @SaCheckPermission(value = "permission:role:assign",orRole = {"admin"})
    public ResultData queryMenuListWithPermission() {
        List<Tree<String>> menuList = menuService.queryMenuListWithPermission();
        return ResultData.success(menuList);
    }

    /**
     * 模糊查询菜单数据
     * @param
     * @return
     */
    @PostMapping("/list/like")
    @ApiOperation(value = "模糊查询菜单数据")
    @SaCheckPermission(value = "permission:menu:query",orRole = {"admin","common","test"})
    public ResultData queryMenuListByLike(@RequestBody SearchMenuDto menuDto) {
        List<AddMenuDto> menuList = menuService.queryMenuListByLike(menuDto);
        return ResultData.success(menuList);
    }

    /**
     * 适配primeVue ui框架的模糊查询菜单数据
     * @param
     * @return
     */
    @PostMapping("/primeVue/list/like")
    @ApiOperation(value = "模糊查询菜单数据(primeVue框架)")
    @SaCheckPermission(value = "permission:menu:query",orRole = {"admin","common","test"})
    public ResultData queryMenuListByLikeWithPrimeVue(@RequestBody SearchMenuDto menuDto) {
        List<Map<String,Object>> menuList = menuService.queryMenuListByLikeWithPrimeVue(menuDto);
        return ResultData.success(menuList);
    }

    /**
     * 查询当前角色所拥有的所有的菜单以及按钮权限
     * @param
     * @return
     */
    @GetMapping("/query/role/permissions")
    @ApiOperation(value = "查询角色的权限数据")
    @SaCheckPermission(value = "permission:role:assign",orRole = {"admin"})
    public ResultData queryRoleMenuList(@RequestParam("id") Integer id) {
        List<String> menuIdList = menuService.queryButtonIdsByRoleId(id);
        return ResultData.success(menuIdList);
    }

    /**
     * 查询当前角色所拥有的所有的菜单以及按钮权限（适配primeVue ui框架）
     * @param
     * @return
     */
    @GetMapping("/primeVue/query/role/permissions")
    @ApiOperation(value = "查询角色的权限数据（适配primeVue ui框架）")
    @SaCheckPermission(value = "permission:role:assign",orRole = {"admin"})
    public ResultData queryRoleMenuListWithPrimeVue(@RequestParam("id") Integer id) {
        List<String> menuIdList = menuService.queryButtonIdsByRoleIdWithPrimeVue(id);
        return ResultData.success(menuIdList);
    }

    /**
     * 新增菜单
     * @param
     * @return
     */
    @PostMapping("/add")
    @ApiOperation(value = "新增菜单")
    @SaCheckPermission(value = "permission:menu:add",orRole = {"admin"})
    public ResultData addMenu(@RequestBody AddMenuDto menu) throws SystemException {
        return menuService.addMenu(menu);
    }

    /**
     * 修改菜单
     * @param
     * @return
     */
    @PutMapping("/update")
    @ApiOperation(value = "修改菜单")
    @SaCheckPermission(value = "permission:menu:update",orRole = {"admin"})
    public ResultData updateMenu(@RequestBody AddMenuDto menu) throws SystemException {
        return menuService.updateMenu(menu);
    }

    /**
     * 菜单数据回显
     * @param
     * @return
     */
    @GetMapping("/echo")
    @ApiOperation(value = "菜单数据回显")
    public ResultData echoMenu(@RequestParam("id") Integer id) throws SystemException {
        AddMenuDto menu = menuService.getMenuAsDto(id);
        return ResultData.success(menu);
    }

    /**
     * 删除菜单
     * @param
     * @return
     */
    @DeleteMapping("/delete")
    @ApiOperation(value = "删除菜单")
    @SaCheckPermission(value = "permission:menu:delete",orRole = {"admin"})
    public ResultData deleteMenu(@RequestParam("id") Integer id) throws SystemException {
        return menuService.deleteMenu(id);
    }
}
