package com.common.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.exception.SystemException;
import com.common.mapper.*;
import com.common.model.dto.UserDto;
import com.common.model.entity.*;
import com.common.model.enums.MenuTypeEnum;
import com.common.response.ResponseCodeEnum;
import com.common.service.IUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
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
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    private final UserRoleMapper userRoleMapper;
    private final RoleMenuMapper roleMenuMapper;
    private final MenuMapper menuMapper;
    private final RoleMapper roleMapper;

    @Override
    public String login(User user) throws SystemException {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("username",user.getUsername());
        User selectOne = baseMapper.selectOne(wrapper);
        if(selectOne == null){
            throw new SystemException(ResponseCodeEnum.USER_NOT_EXITS);
        }
        if(!selectOne.getPassword().equalsIgnoreCase(user.getPassword())){
            throw new SystemException(ResponseCodeEnum.PASSWOR_ERROR);
        }
        //获取当前用户所拥有的所有菜单id
        log.info("用户{}正在登录.....",selectOne.getUsername());
        StpUtil.login(selectOne.getId());
        return StpUtil.getTokenValueByLoginId(selectOne.getId());

//        List<Integer> menuIds = this.findUserMenus(selectOne.getId());
//        //将菜单数据封装成树形数据格式返回
//        //List<Tree<String>> treeList = this.buildTree(menuIds);
//        //只获取路由名称
//        List<String> routes = menuMapper.selectBatchIds(menuIds).stream().map(Menu::getName).collect(Collectors.toList());
//        Map<String,Object> map = new HashMap<>();
//        map.put("routes", routes);
//        return map;
    }

    @Override
    public Map<String, Object> getUserInfo(Integer userId) throws SystemException {
        User user = baseMapper.selectById(userId);
        if(user == null){
            throw new SystemException(ResponseCodeEnum.USER_NOT_EXITS);
        }
        //获取当前用户所拥有的所有菜单id
        log.info("用户{}正在菜单和按钮数据.....",user.getUsername());
        //判断当前是否拥有管理员权限
        QueryWrapper<UserRole> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        List<Integer> roleIds = userRoleMapper.selectList(wrapper).stream().map(UserRole::getRoleId).collect(Collectors.toList());
        boolean isAdmin = false;
        if(!CollectionUtils.isEmpty(roleIds)){
            isAdmin = this.hasAdminPermission(roleIds);
        }
        if((user.getUsername().equalsIgnoreCase("admin")) || (isAdmin)){
            //如果用户名是admin则查询所有的菜单和按钮
            return this.queryAdminInfo(user);
        }else{
            //其他角色的用户查询相应的菜单和按钮
            return this.queryOtherInfo(user);
        }
    }

    private boolean hasAdminPermission(List<Integer> roleIds) {
        boolean isAdmin = false;
        List<Role> roleList = roleMapper.selectBatchIds(roleIds);
        for (int i = 0; i < roleList.size(); i++) {
            if(roleList.get(i).getCode().equalsIgnoreCase("admin")){
                isAdmin = true;
                break;
            }
        }
        return isAdmin;
    }

    private Map<String, Object> queryOtherInfo(User user) {
        //查询相应的目录以及菜单
        //查询用户的角色ids
        QueryWrapper<UserRole> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id",user.getId());
        List<Integer> roleIds = userRoleMapper.selectList(wrapper).stream().map(UserRole::getRoleId).collect(Collectors.toList());
        if(!CollectionUtils.isEmpty(roleIds)){
            //说明此用户已经被管理员分配过角色
            //根据角色ids查询相应的所有目录、菜单、按钮，追后过滤出routes和buttons即可
            //存储所有的菜单ids，使用Set存储的目的是去重相同的menuId，因为不同的角色可能会分配到相同的菜单或者按钮权限
            Set<Integer> menuIdSet = new HashSet<>();
            roleIds.stream().forEach(roleId -> {
                QueryWrapper<RoleMenu> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("role_id",roleId);
                List<Integer> menuIds = roleMenuMapper.selectList(queryWrapper).stream().map(RoleMenu::getMenuId).collect(Collectors.toList());
                menuIdSet.addAll(menuIds);
            });
            if(!CollectionUtils.isEmpty(menuIdSet)){
                List<Menu> menuList = menuMapper.selectBatchIds(menuIdSet);
                //过滤出routes和button
                List<String> routes = menuList.stream()
                        .filter(menu -> (menu.getType() == MenuTypeEnum.DIRECTORY.getCode()) || (menu.getType() == MenuTypeEnum.MENU.getCode()))
                        .collect(Collectors.toList())
                        .stream().map(Menu::getName).collect(Collectors.toList());
                List<String> permissions = menuList.stream()
                        .filter(menu -> menu.getType() == MenuTypeEnum.BUTTON.getCode())
                        .collect(Collectors.toList())
                        .stream().map(Menu::getPermission).collect(Collectors.toList());

                Map<String,Object> map = new HashMap<>();
                map.put("routes", routes);
                map.put("user", user);
                map.put("permissions", permissions);
                return map;
            }
        }
        return null;
    }

    private Map<String,Object> queryAdminInfo(User user) {
        //(管理员)查询所有的菜单和按钮
        //查询所有的目录以及菜单
        QueryWrapper<Menu> wrapper = new QueryWrapper<>();
        wrapper.eq("type", MenuTypeEnum.DIRECTORY.getCode())
                .or()
                .eq("type", MenuTypeEnum.MENU.getCode());
        List<String> routes = menuMapper.selectList(wrapper).stream().map(Menu::getName).collect(Collectors.toList());

        //获取所有的按钮
        QueryWrapper<Menu> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("type", MenuTypeEnum.BUTTON.getCode());
        List<String> permissions = menuMapper.selectList(queryWrapper).stream().map(Menu::getPermission).collect(Collectors.toList());

        Map<String,Object> map = new HashMap<>();
        map.put("routes", routes);
        map.put("user", user);
        map.put("permissions", permissions);
        return map;
    }

    @Override
    public Map<String,Object> queryUserList(Integer pageNo, Integer pageSize, String username) {
        Page<User> pageInfo = new Page<User>(pageNo, pageSize);
        String trimUsername = "";
        if(!StringUtils.isBlank(username)){
            trimUsername = username.trim();
        }
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq(!StringUtils.isBlank(trimUsername),"username",trimUsername);
        Page<User> userPage = baseMapper.selectPage(pageInfo, wrapper);
        if(userPage != null){
            List<User> userList = userPage.getRecords();
            List<UserDto> list = new ArrayList<>();
            if(!CollectionUtils.isEmpty(userList)){
                userList.stream().forEach(user -> {
                    QueryWrapper<UserRole> queryWrapper = new QueryWrapper<>();
                    queryWrapper.eq("user_id",user.getId());
                    List<Integer> roleIds = userRoleMapper.selectList(queryWrapper).stream().map(UserRole::getRoleId).collect(Collectors.toList());
                    UserDto userDto = new UserDto();
                    userDto.setId(user.getId());
                    userDto.setUsername(user.getUsername());
                    userDto.setPassword(user.getPassword());
                    if(!CollectionUtils.isEmpty(roleIds)){
                        //用户已经分配过角色
                        List<String> roles = roleMapper.selectBatchIds(roleIds).stream().map(Role::getName).collect(Collectors.toList());
                        userDto.setRoles(roles);
                    }
                    list.add(userDto);
                });
                Map<String,Object> map = new HashMap<>();
                map.put("data", list);
                map.put("total", userPage.getTotal());
                return map;
            }
        }
        return null;
    }

    @Override
    public List<String> queryRoles(Integer userId) {
        QueryWrapper<UserRole> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id",userId);
        List<UserRole> userRoles = userRoleMapper.selectList(wrapper);
        if(!CollectionUtils.isEmpty(userRoles)){
            List<Integer> roleIds = userRoles.stream().map(UserRole::getRoleId).collect(Collectors.toList());
            List<Role> roleList = roleMapper.selectBatchIds(roleIds);
            return roleList.stream().map(Role::getName).collect(Collectors.toList());
        }
        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveRoles(String username,List<String> roles) {
        QueryWrapper<User> userWrapper = new QueryWrapper<>();
        userWrapper.eq("username",username);
        User user = baseMapper.selectOne(userWrapper);
        //分配角色之前，将之前拥有的角色先删除后再重新保存新的角色
        QueryWrapper<UserRole> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id",user.getId());
        List<UserRole> userRoles = userRoleMapper.selectList(wrapper);
        if(!CollectionUtils.isEmpty(userRoles)){
            //将原先拥有的角色删除
            List<Integer> roleIds = userRoles.stream().map(UserRole::getRoleId).collect(Collectors.toList());
            QueryWrapper<UserRole> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id",user.getId());
            queryWrapper.in("role_id",roleIds);
            userRoleMapper.delete(queryWrapper);
        }
        //保存新的用户角色
        if(!CollectionUtils.isEmpty(roles)){
            QueryWrapper<Role> queryWrapper = new QueryWrapper<>();
            queryWrapper.in("role_name",roles);
            List<Integer> roleIds = roleMapper.selectList(queryWrapper).stream().map(Role::getId).collect(Collectors.toList());
            roleIds.stream().forEach(roleId -> {
                UserRole userRole = new UserRole();
                userRole.setUserId(user.getId());
                userRole.setRoleId(roleId);
                userRoleMapper.insert(userRole);
            });
        }
    }
}
