package com.common.service;

import cn.hutool.core.lang.tree.Tree;
import com.common.exception.SystemException;
import com.common.model.dto.AddMenuDto;
import com.common.model.dto.SearchMenuDto;
import com.common.model.entity.Menu;
import com.common.model.entity.Role;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author author
 * @since 2024-04-21
 */
public interface IMenuService extends IService<Menu> {

    /**
     * 查询所有菜单数据
     */
    List<Tree<String>> queryMenuList();

    /**
     * 查询当前角色所拥有的所有的菜单以及按钮权限
     * @return
     */
    List<Integer> queryButtonIdsByRoleId(Integer id);

    /**
     * 新增菜单
     * @param menu
     */
    void addMenu(AddMenuDto menu) throws SystemException;

    /**
     * 删除菜单
     * @param id
     */
    void deleteMenu(Integer id) throws SystemException;

    /**
     * 菜单数据回显
     * @param id
     * @return
     */
    AddMenuDto getMenuAsDto(Integer id);

    /**
     * 修改菜单
     * @param menu
     */
    void updateMenu(AddMenuDto menu) throws SystemException;

    /**
     * 模糊查询菜单数据
     * @param menuDto
     * @return
     */
    List<AddMenuDto> queryMenuListByLike(SearchMenuDto menuDto);

    /**
     * 分配权限时获取菜单树形数据
     * @return
     */
    List<Tree<String>> queryMenuListWithPermission();
}
