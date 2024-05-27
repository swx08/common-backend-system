package com.common.service.impl;

import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.lang.tree.TreeNodeConfig;
import cn.hutool.core.lang.tree.TreeUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.common.exception.SystemException;
import com.common.model.dto.AddMenuDto;
import com.common.model.dto.SearchMenuDto;
import com.common.model.entity.Menu;
import com.common.model.entity.Role;
import com.common.model.enums.MenuStatusEnum;
import com.common.model.enums.MenuTypeEnum;
import com.common.response.ResponseCodeEnum;
import com.common.service.IMenuService;
import com.common.service.IRoleMenuService;
import com.common.util.RegexUtils;
import lombok.RequiredArgsConstructor;
import com.common.mapper.RoleMenuMapper;
import com.common.mapper.MenuMapper;
import com.common.model.entity.RoleMenu;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author author
 * @since 2024-04-21
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MenuServiceImpl extends ServiceImpl<MenuMapper, Menu> implements IMenuService {

    private final RoleMenuMapper roleMenuMapper;

    private final IRoleMenuService roleMenuService;

    /**
     * 将菜单数据封装成树形数据格式返回
     */
    @Override
    public List<Tree<String>> queryMenuList() {
        //查询菜单数据
        log.info("查询菜单数据...");
        QueryWrapper<Menu> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("create_time");
        List<Menu> menusList = baseMapper.selectList(wrapper);
        TreeNodeConfig treeNodeConfig = new TreeNodeConfig();
        // 最大递归深度
        treeNodeConfig.setDeep(3);
        List<Tree<String>> treeNodes = buildTree(menusList, treeNodeConfig);
        return treeNodes;
    }

    private List<Tree<String>> buildTree(List<Menu> menusList, TreeNodeConfig treeNodeConfig) {
        return TreeUtil.build(menusList, "0", treeNodeConfig, this::populateTreeNode);
    }

    private void populateTreeNode(Menu treeNode, Tree<String> tree) {
        // 避免空指针，进行必要的空检查
        String id = Optional.ofNullable(treeNode.getId()).map(Object::toString).orElse(null);
        String parentId = Optional.ofNullable(treeNode.getParentId()).map(Object::toString).orElse(null);

        tree.setId(id);
        tree.setParentId(parentId);
        // 扩展属性 ...
        tree.putExtra("key", id);
        tree.putExtra("type", treeNode.getType());
        tree.putExtra("name", treeNode.getName());
        tree.putExtra("title", treeNode.getTitle());
        tree.putExtra("permission", treeNode.getPermission());
        tree.putExtra("component", treeNode.getComponent());
        tree.putExtra("status", treeNode.getStatus());
    }


    @Override
    public List<Integer> queryButtonIdsByRoleId(Integer id) {
        if (id == null) {
            return Collections.emptyList();
        }

        QueryWrapper<RoleMenu> wrapper = new QueryWrapper<>();
        wrapper.eq("role_id", id);

        // 检查Mapper是否为null，避免空指针异常
        if (roleMenuMapper == null || baseMapper == null) {
            return Collections.emptyList();
        }

        List<RoleMenu> roleMenus = roleMenuMapper.selectList(wrapper);
        if (!CollectionUtils.isEmpty(roleMenus)) {
            List<Integer> menuIdList = roleMenus.stream().map(RoleMenu::getMenuId).collect(Collectors.toList());
            List<Menu> menuList = baseMapper.selectBatchIds(menuIdList);

            // 合并流操作，提高效率
            List<Integer> buttonIds = menuList.stream()
                    .filter(menu ->
                        menu.getType() == MenuTypeEnum.BUTTON.getCode() &&
                        menu.getStatus() == MenuStatusEnum.OPEN.getCode()
                    )
                    .map(Menu::getId)
                    .collect(Collectors.toList());

            return buttonIds;
        }

        return Collections.emptyList();
    }


    @Override
    public void addMenu(AddMenuDto menu) throws SystemException {
        //校验菜单权限标识和组件路径是否符合命名规范
        log.info("新增菜单：校验菜单权限标识和组件路径是否符合命名规范...");
        verifyMenuName(menu);
        log.info("新增菜单：校验通过！");
        //校验菜单名称和组件名称唯一
        log.info("新增菜单：校验菜单名称和组件名称唯一...");
        verifyMenuUnique(menu);
        log.info("新增菜单：校验通过！");
        //插入菜单数据
        Menu saveMenu = new Menu();
        //设置父级菜单id
        handleParentId(menu);
        BeanUtils.copyProperties(menu,saveMenu);
        baseMapper.insert(saveMenu);
    }

    @Override
    public void deleteMenu(Integer id) throws SystemException {
        log.info("正在删除id为{}的菜单...",id);
        //要删除的菜单如果有子菜单则提示不能删除
        QueryWrapper<Menu> wrapper = new QueryWrapper<>();
        wrapper.eq("parent_id",id);
        if(baseMapper.selectCount(wrapper) > 0){
            log.error("id为{}的菜单有子菜单，无法删除...",id);
            throw new SystemException(ResponseCodeEnum.INCLUDE_SUBMENU);
        }
        //将角色菜单关联表中的数据删除
        roleMenuService.removeByMenuId(id);
        if(baseMapper.deleteById(id) > 0){
            log.info("id为{}的菜单删除成功！",id);
        }else{
            log.error("id为{}的菜单删除失败！",id);
            throw new SystemException(ResponseCodeEnum.SYSTEM_ERROR);
        }
    }

    @Override
    public AddMenuDto getMenuAsDto(Integer id) {
        if (id != null) {
            Menu menu = baseMapper.selectById(id);
            if (menu != null) {
                AddMenuDto menuDto = new AddMenuDto();
                Menu parentMenu = baseMapper.selectById(menu.getParentId());
                if (parentMenu != null) {
                    menuDto.setParent(parentMenu.getTitle());
                } else {
                    // 根据业务需求，可以调整默认值或移除该else块
                    menuDto.setParent("主类目");
                }
                BeanUtils.copyProperties(menu, menuDto);
                return menuDto;
            }
        }
        return null;
    }

    @Override
    public void updateMenu(AddMenuDto menu) throws SystemException {
        //校验菜单权限标识和组件路径是否符合命名规范
        log.info("修改菜单：校验菜单权限标识和组件路径是否符合命名规范...");
        verifyMenuName(menu);
        log.info("修改菜单：校验通过！");
        //校验菜单名称和组件名称唯一
        log.info("修改菜单：校验菜单名称和组件名称唯一...");
        verifyMenuUniqueWithUpdate(menu);
        log.info("修改菜单：校验通过！");
        Menu updateMenu = baseMapper.selectById(menu.getId());
        BeanUtils.copyProperties(menu,updateMenu);
        log.info("要修改的菜单数据为：{}",updateMenu);
        baseMapper.updateById(updateMenu);
    }

    @Override
    public List<AddMenuDto> queryMenuListByLike(SearchMenuDto menuDto) {
        //查询菜单数据
        log.info("模糊查询菜单数据...");
        QueryWrapper<Menu> wrapper = new QueryWrapper<>();
        //模糊查询
        wrapper.like(!StringUtils.isBlank(menuDto.getTitle()),"title",menuDto.getTitle().trim());
        wrapper.eq(null != menuDto.getType(),"type",menuDto.getType());
        wrapper.eq(null != menuDto.getStatus(),"status",menuDto.getStatus());
        List<Menu> menuList = baseMapper.selectList(wrapper);
        if(!CollectionUtils.isEmpty(menuList)){
            List<AddMenuDto> list = new ArrayList<AddMenuDto>();
            menuList.stream().forEach(menu -> {
                AddMenuDto addMenuDto = new AddMenuDto();
                BeanUtils.copyProperties(menu,addMenuDto);
                list.add(addMenuDto);
            });
            return list;
        }
        return null;
    }

    @Override
    public List<Tree<String>> queryMenuListWithPermission() {
        //查询菜单数据
        log.info("查询分配权限菜单数据...");
        QueryWrapper<Menu> wrapper = new QueryWrapper<>();
        wrapper.eq("status",MenuStatusEnum.OPEN.getCode());
        wrapper.orderByDesc("create_time");
        List<Menu> menusList = baseMapper.selectList(wrapper);
        TreeNodeConfig treeNodeConfig = new TreeNodeConfig();
        // 最大递归深度
        treeNodeConfig.setDeep(3);
        List<Tree<String>> treeNodes = TreeUtil.build(menusList, "0", treeNodeConfig,
                (treeNode, tree) -> {
                    //这俩属性必须设置
                    tree.setId(treeNode.getId().toString());
                    tree.setParentId(treeNode.getParentId().toString());
                    // 扩展属性 ...
                    tree.putExtra("key", treeNode.getId());
                    tree.putExtra("title", treeNode.getTitle());
                });
        return treeNodes;
    }

    private void verifyMenuUniqueWithUpdate(AddMenuDto menu) throws SystemException {
        QueryWrapper<Menu> wrapper = new QueryWrapper<>();
        wrapper.eq("name", menu.getName());
        Menu selectOne = baseMapper.selectOne(wrapper);
        if((null != selectOne) && (selectOne.getId().intValue() != menu.getId())) {
            log.error("菜单组件名称已存在，修改失败...");
            throw new SystemException(ResponseCodeEnum.MENU_NAME_EXITS);
        }
        QueryWrapper<Menu> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("title", menu.getTitle());
        Menu selectTwo = baseMapper.selectOne(queryWrapper);
        if((null != selectTwo) && (selectTwo.getId().intValue() != menu.getId())) {
            log.error("菜单名称已存在，修改失败...");
            throw new SystemException(ResponseCodeEnum.MENU_TITLE_EXITS);
        }
    }

    /**
     * 设置父级菜单id
     * @param menuDto
     */
    private void handleParentId(AddMenuDto menuDto) {
        //说明是目录，则父级id为0
        if(menuDto.getType() == MenuTypeEnum.DIRECTORY.getCode()) {
            menuDto.setParentId(0);
        }

        if((menuDto.getType() == MenuTypeEnum.MENU.getCode())
                || (menuDto.getType() == MenuTypeEnum.BUTTON.getCode())){
            QueryWrapper<Menu> wrapper = new QueryWrapper<>();
            wrapper.eq("title",menuDto.getParent());
            Menu menu = baseMapper.selectOne(wrapper);
            if(null != menu){
                menuDto.setParentId(menu.getId());
            }
        }
    }

    /**
     * 校验菜单权限标识和组件路径是否符合命名规范
     * @param menu
     */
    private void verifyMenuName(AddMenuDto menu) throws SystemException {
        if(!StringUtils.isBlank(menu.getPermission())){
            if(!RegexUtils.verifyMenuPermission(menu.getPermission())) {
                log.error("校验菜单权限标识命名规范不通过...");
                throw new SystemException(ResponseCodeEnum.PERMISSION_NAME_ILLEGAL);
            }

        }
        if(!StringUtils.isBlank(menu.getComponent())) {
            //校验菜单权限标识和组件路径是否符合命名规范
            if(!RegexUtils.verifyMenuComponent(menu.getComponent())) {
                log.error("校验组件路径命名规范不通过...");
                throw new SystemException(ResponseCodeEnum.COMPONENT_NAME_ILLEGAL);
            }
        }
    }

    /**
     * 校验菜单名称和组件名称唯一
     * @param menu
     * @return
     */
    private void verifyMenuUnique(AddMenuDto menu) throws SystemException {
        QueryWrapper<Menu> wrapper = new QueryWrapper<>();
        wrapper.eq("name", menu.getName());
        if(!CollectionUtils.isEmpty(baseMapper.selectList(wrapper))) {
            log.error("菜单组件名称已存在...");
            throw new SystemException(ResponseCodeEnum.MENU_NAME_EXITS);
        }
        QueryWrapper<Menu> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("title", menu.getTitle());
        if(!CollectionUtils.isEmpty(baseMapper.selectList(queryWrapper))) {
            log.error("菜单名称已存在...");
            throw new SystemException(ResponseCodeEnum.MENU_TITLE_EXITS);
        }
    }
}
