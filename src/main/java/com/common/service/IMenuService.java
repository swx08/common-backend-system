package com.common.service;

import cn.hutool.core.lang.tree.Tree;
import com.common.exception.SystemException;
import com.common.model.dto.AddMenuDto;
import com.common.model.dto.SearchMenuDto;
import com.common.model.entity.Menu;
import com.common.model.entity.Role;
import com.baomidou.mybatisplus.extension.service.IService;
import com.common.model.vo.PrimeVueMenuVO;
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
public interface IMenuService extends IService<Menu> {

    /**
     * 查询所有菜单数据
     */
    List<Tree<String>> queryMenuList();

    /**
     * 查询当前角色所拥有的所有的菜单以及按钮权限
     * @return
     */
    List<String> queryButtonIdsByRoleId(Integer id);

    /**
     * 新增菜单
     * @param menu
     */
    ResultData addMenu(AddMenuDto menu) throws SystemException;

    /**
     * 删除菜单
     * @param id
     */
    ResultData deleteMenu(Integer id) throws SystemException;

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
    ResultData updateMenu(AddMenuDto menu) throws SystemException;

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

    List<Tree<String>> queryMenuListWithPrimeVue();

    /**
     * 模糊查询菜单数据(primeVue框架)
     * @param menuDto
     * @return
     */
    List<Map<String,Object>> queryMenuListByLikeWithPrimeVue(SearchMenuDto menuDto);

    /**
     *  查询当前角色所拥有的所有的菜单以及按钮权限（适配primeVue ui框架）
     * @param id
     * @return
     */
    List<String> queryButtonIdsByRoleIdWithPrimeVue(Integer id);
}
