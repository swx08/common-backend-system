package com.common.service.impl;

import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.lang.tree.TreeNodeConfig;
import cn.hutool.core.lang.tree.TreeUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.common.exception.SystemException;
import com.common.model.dto.AddMenuDto;
import com.common.model.entity.Menu;
import com.common.model.entity.Role;
import com.common.model.enums.MenuStatusEnum;
import com.common.model.enums.MenuTypeEnum;
import com.common.response.ResponseCodeEnum;
import com.common.service.IMenuService;
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

import java.util.List;
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

    /**
     * 将菜单数据封装成树形数据格式返回
     */
    @Override
    public List<Tree<String>> queryMenuList() {
        QueryWrapper<Menu> wrapper = new QueryWrapper<>();
        //查询菜单数据
        log.info("查询菜单数据...");
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
                    tree.putExtra("type", treeNode.getType());
                    tree.putExtra("name", treeNode.getName());
                    tree.putExtra("title", treeNode.getTitle());
                    tree.putExtra("permission", treeNode.getPermission());
                    tree.putExtra("component", treeNode.getComponent());
                    tree.putExtra("status", treeNode.getStatus());
                });
        return treeNodes;
    }

    @Override
    public List<Integer> queryRoleMenuList(Role role) {
        //如果是admin管理员角色则返回所有的菜单及按钮
        if("admin".equalsIgnoreCase(role.getCode())){
            //由于前端展示需求，只需返回按钮级别的id即可
            //过滤出按钮的id并且状态是开启的
            List<Menu> menuList = baseMapper.selectList(null);
            List<Integer> buttonIds = menuList.stream().filter(menu -> (menu.getType() == MenuTypeEnum.BUTTON.getCode()) && (menu.getStatus() == MenuStatusEnum.OPEN.getCode()))
                    .collect(Collectors.toList())
                    .stream().map(Menu::getId).collect(Collectors.toList());
            return buttonIds;
        }else{
            QueryWrapper<RoleMenu> wrapper = new QueryWrapper<>();
            wrapper.eq("role_id", role.getId());
            List<Integer> menuIdList = roleMenuMapper.selectList(wrapper).stream().map(RoleMenu::getMenuId).collect(Collectors.toList());
            if(!CollectionUtils.isEmpty(menuIdList)){
                //TODO：其实这里不应该直接返回数据，还需从menu表中查询菜单或按钮是否是开启状态，否则不返回。
                //由于前端展示需求，只需返回按钮级别的id即可
                //过滤出按钮的id并且状态是开启的
                List<Menu> menuList = baseMapper.selectBatchIds(menuIdList);
                List<Integer> buttonIds = menuList.stream().filter(menu -> (menu.getType() == MenuTypeEnum.BUTTON.getCode()) && (menu.getStatus() == MenuStatusEnum.OPEN.getCode()))
                        .collect(Collectors.toList())
                        .stream().map(Menu::getId).collect(Collectors.toList());
                return buttonIds;
            }
            return null;
        }
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
            log.info("id为{}的菜单有子菜单，无法删除...",id);
            throw new SystemException(ResponseCodeEnum.INCLUDE_SUBMENU);
        }
        if(baseMapper.deleteById(id) > 0){
            log.info("id为{}的菜单删除成功！",id);
        }else{
            log.info("id为{}的菜单删除失败！",id);
            throw new SystemException(ResponseCodeEnum.SYSTEM_ERROR);
        }
    }

    @Override
    public AddMenuDto echoMenu(Integer id) {
        if(null != id){
            Menu menu = baseMapper.selectById(id);
            if(null != menu){
                AddMenuDto menuDto = new AddMenuDto();
                Menu parentMenu = baseMapper.selectById(menu.getParentId());
                if(null != parentMenu){
                    menuDto.setParent(parentMenu.getTitle());
                }else{
                    menuDto.setParent("主类目");
                }
                BeanUtils.copyProperties(menu,menuDto);
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

    private void verifyMenuUniqueWithUpdate(AddMenuDto menu) throws SystemException {
        QueryWrapper<Menu> wrapper = new QueryWrapper<>();
        wrapper.eq("name", menu.getName());
        Menu selectOne = baseMapper.selectOne(wrapper);
        if((null != selectOne) && (selectOne.getId().intValue() != menu.getId())) {
            throw new SystemException(ResponseCodeEnum.MENU_NAME_EXITS);
        }
        QueryWrapper<Menu> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("title", menu.getTitle());
        Menu selectTwo = baseMapper.selectOne(queryWrapper);
        if((null != selectTwo) && (selectTwo.getId().intValue() != menu.getId())) {
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
                log.info("校验菜单权限标识命名规范不通过...");
                throw new SystemException(ResponseCodeEnum.PERMISSION_NAME_ILLEGAL);
            }

        }
        if(!StringUtils.isBlank(menu.getComponent())) {
            //校验菜单权限标识和组件路径是否符合命名规范
            if(!RegexUtils.verifyMenuComponent(menu.getComponent())) {
                log.info("校验组件路径命名规范不通过...");
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
            throw new SystemException(ResponseCodeEnum.MENU_NAME_EXITS);
        }
        QueryWrapper<Menu> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("title", menu.getTitle());
        if(!CollectionUtils.isEmpty(baseMapper.selectList(queryWrapper))) {
            throw new SystemException(ResponseCodeEnum.MENU_TITLE_EXITS);
        }
    }

    /**
     * 将菜单数据封装成树形数据格式返回
     * @param menuIds
     */
//    private List<Tree<String>> buildTree(List<Integer> menuIds) {
//        List<Menu> menusList = menuMapper.selectBatchIds(menuIds);
//        TreeNodeConfig treeNodeConfig = new TreeNodeConfig();
//        // 最大递归深度
//        treeNodeConfig.setDeep(2);
//        List<Tree<String>> treeNodes = TreeUtil.build(menusList, "0", treeNodeConfig,
//                (treeNode, tree) -> {
//                    //这俩属性必须设置
//                    tree.setId(treeNode.getId().toString());
//                    tree.setParentId(treeNode.getParentId().toString());
//                    // 扩展属性 ...
//                    tree.putExtra("path", treeNode.getPath());
//                    tree.putExtra("name", treeNode.getName());
//                    tree.putExtra("title", treeNode.getTitle());
//                    tree.putExtra("hidden", treeNode.getHidden());
//                    tree.putExtra("icon", treeNode.getIcon());
//                });
//        return treeNodes;
//    }
}
