package com.common.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.exception.SystemException;
import com.common.mapper.*;
import com.common.model.dto.LoginUserDto;
import com.common.model.dto.RegisterUserDto;
import com.common.model.dto.SearchUserDto;
import com.common.model.dto.UserDto;
import com.common.model.entity.*;
import com.common.model.enums.MenuStatusEnum;
import com.common.model.enums.MenuTypeEnum;
import com.common.model.enums.UserStatusEnum;
import com.common.model.vo.EchoUserVo;
import com.common.model.vo.UserListVo;
import com.common.response.ResponseCodeEnum;
import com.common.response.ResultData;
import com.common.service.IUserRoleService;
import com.common.service.IUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
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
    private final IUserRoleService userRoleService;

    @Override
    public String login(LoginUserDto user) throws SystemException {
        log.info("用户{}正在登录.....",user.getUsername());
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("username",user.getUsername());
        User selectOne = baseMapper.selectOne(wrapper);
        //校验当前登录用户信息
        verifyUserWithLogin(selectOne,user);
        //获取当前用户所拥有的所有菜单id
        log.info("用户{}登录成功！",user.getUsername());
        //维护用户登录上下文信息
        StpUtil.login(selectOne.getId());
        //返回当前登录用户的token
        return StpUtil.getTokenValueByLoginId(selectOne.getId());
    }

    /**
     * 校验当前登录用户信息
     * @param selectOne
     * @param user
     * @throws SystemException
     */
    private void verifyUserWithLogin(User selectOne, LoginUserDto user) throws SystemException {
        if(selectOne == null){
            log.error("用户{}不存在",user.getUsername()));
            throw new SystemException(ResponseCodeEnum.USER_NOT_EXITS);
        }
        //校验用户状态
        if(selectOne.getStatus().intValue() != UserStatusEnum.OPEN.getCode()){
            log.error("用户{}已被禁用",user.getUsername()));
            throw new SystemException(ResponseCodeEnum.USER_FORBIDDEN);
        }
        if(!selectOne.getPassword().equalsIgnoreCase(user.getPassword())){
            log.error("用户{}密码错误",user.getUsername()));
            throw new SystemException(ResponseCodeEnum.PASSWOR_ERROR);
        }
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
        if(isAdmin){
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
    public Map<String,Object> queryUserList(Integer pageNo, Integer pageSize, SearchUserDto userDto) {
        Page<User> pageInfo = new Page<User>(pageNo, pageSize);
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.like(StringUtils.isNotBlank(userDto.getUsername()),"username",userDto.getUsername().trim());
        wrapper.like(StringUtils.isNotBlank(userDto.getPhone()),"phone",userDto.getPhone().trim());
        wrapper.eq(null != userDto.getStatus(),"status",userDto.getStatus());
        wrapper.orderByDesc("create_time");
        Page<User> userPage = baseMapper.selectPage(pageInfo, wrapper);
        if(userPage != null){
            List<UserListVo> list = new ArrayList<>();
            List<User> userList = userPage.getRecords();
            if(!CollectionUtils.isEmpty(userList)){
                userList.stream().forEach(user -> {
                    UserListVo userVo = new UserListVo();
                    BeanUtils.copyProperties(user, userVo);
                    if(user.getStatus().intValue() == UserStatusEnum.OPEN.getCode()){
                        userVo.setChecked(true);
                    }else {
                        userVo.setChecked(false);
                    }
                    list.add(userVo);
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

    @Override
    public ResultData updateUserStatus(Integer id) throws SystemException {
        User user = baseMapper.selectById(id);
        log.info("正在修改用户{}的状态...",user.getUsername());
        //拥有管理员角色的用户无权修改
        boolean verify = verifyRole(user);
        if(verify) {
            throw new SystemException(ResponseCodeEnum.INSUFFICIENT_AUTHORITY);
        }else{
            if(user.getStatus().intValue() == UserStatusEnum.OPEN.getCode()) {
                log.info("修改状态为：{}",UserStatusEnum.CLOSE.getStatus());
                user.setStatus(UserStatusEnum.CLOSE.getCode());
            }else{
                log.info("修改状态为：{}",UserStatusEnum.OPEN.getStatus());
                user.setStatus(UserStatusEnum.OPEN.getCode());
            }
            if(baseMapper.updateById(user) > 0) {
                log.info("用户{}的状态修改成功",user.getUsername());
                return ResultData.success();
            }else{
                log.error("用户{}的状态修改失败",user.getUsername());
                return ResultData.fail(1013,"用户状态修改失败！");
            }
        }
    }

    /**
     * 拥有管理员角色的用户无权修改
     * @param user
     * @return
     */
    private boolean verifyRole(User user) {
        QueryWrapper<UserRole> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", user.getId());
        List<UserRole> userRoleList = userRoleMapper.selectList(wrapper);
        if(!CollectionUtils.isEmpty(userRoleList)){
            List<Integer> roleIds = userRoleList.stream().map(UserRole::getRoleId).collect(Collectors.toList());
            List<Role> roleList = roleMapper.selectBatchIds(roleIds);
            for (Role role : roleList) {
                if(role.getCode().equalsIgnoreCase("admin")){
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public EchoUserVo echoUserById(Integer id) {
        User user = baseMapper.selectById(id);
        if(null != user){
            EchoUserVo echoUserVo = new EchoUserVo();
            BeanUtils.copyProperties(user, echoUserVo);
            return echoUserVo;
        }
        return null;
    }

    @Override
    public ResultData updateUser(EchoUserVo userVo) throws SystemException {
        User user = baseMapper.selectById(userVo.getId());
        log.info("正在修改用户{}的数据...", user.getUsername());
        if(null != user){
            //验证用户名、手机号、邮箱唯一
            verifyUserWithUpdate(userVo);
            //用户数据修改
            BeanUtils.copyProperties(userVo, user);
            if(baseMapper.updateById(user) > 0){
                log.info("用户{}的数据修改成功！", user.getUsername());
                return ResultData.success();
            }else {
                log.error("用户{}的数据修改失败！", user.getUsername());
                return ResultData.fail(1016,"用户信息修改失败！");
            }
        }
        return ResultData.fail(ResponseCodeEnum.USER_NOT_EXITS);
    }

    /**
     * 验证用户名、手机号、邮箱唯一
     * @param userVo
     */
    private void verifyUserWithUpdate(EchoUserVo userVo) throws SystemException {
        QueryWrapper<User> wrapper1 = new QueryWrapper<>();
        wrapper1.eq("username", userVo.getUsername());
        User user1 = baseMapper.selectOne(wrapper1);
        if((user1 != null) && (userVo.getId().intValue() != user1.getId().intValue())){
            log.error("用户名称已存在{}",userVo.getUsername());
            throw new SystemException(ResponseCodeEnum.USERNAME_EXITS);
        }

        //手机号唯一
        QueryWrapper<User> wrapper2 = new QueryWrapper<>();
        wrapper2.eq("phone", userVo.getPhone());
        User user2 = baseMapper.selectOne(wrapper2);
        if((user2 != null) && (userVo.getId().intValue() != user2.getId().intValue())){
            log.error("手机号已存在{}",userVo.getPhone());
            throw new SystemException(ResponseCodeEnum.PHONE_EXITS);
        }

        //邮箱唯一
        QueryWrapper<User> wrapper3 = new QueryWrapper<>();
        wrapper3.eq("email", userVo.getEmail());
        User user3 = baseMapper.selectOne(wrapper3);
        if((user3 != null) && (userVo.getId().intValue() != user3.getId().intValue())){
            log.error("邮箱已存在{}",userVo.getEmail());
            throw new SystemException(ResponseCodeEnum.EMAIL_EXITS);
        }
    }

    @Override
    public ResultData deleteUser(Integer id) {
        log.info("正在删除id为{}的用户",id);
        //删除用户角色关联表的数据
        userRoleService.removeByUserId(id);
        //删除用户
        if(baseMapper.deleteById(id) > 0) {
            log.info("id为{}的用户已被彻底删除",id);
            return ResultData.success();
        }else{
            log.error("id为{}的用户删除失败",id);
            return ResultData.fail(1017,"用户删除失败！");
        }
    }

    /**
     * TODO： 真实环境下需要手机号验证和邮箱验证
     * @param userDto
     * @return
     * @throws SystemException
     */
    @Override
    public ResultData register(RegisterUserDto userDto) throws SystemException {
        log.info("用户{}正在进行注册操作...",userDto.getUsername());
        //验证用户名、手机号、邮箱唯一
        verifyUserWithRegister(userDto);
        //用户密码加密
        userDto.setPassword(DigestUtil.md5Hex(userDto.getPassword()));
        User user = new User();
        BeanUtils.copyProperties(userDto,user);
        if(baseMapper.insert(user) > 0){
            log.info("用户{}注册成功！",userDto.getUsername());
            return ResultData.success();
        }else {
            log.error("用户{}注册失败！",userDto.getUsername());
            return ResultData.fail(1018,"用户注册失败！");
        }
    }

    /**
     * 验证用户名、手机号、邮箱唯一
     * @param user
     */
    private void verifyUserWithRegister(RegisterUserDto user) throws SystemException {
        QueryWrapper<User> wrapper1 = new QueryWrapper<>();
        wrapper1.eq("username", user.getUsername());
        User user1 = baseMapper.selectOne(wrapper1);
        if(user1 != null){
            log.error("用户名称已存在{}",user.getUsername());
            throw new SystemException(ResponseCodeEnum.USERNAME_EXITS);
        }

        //手机号唯一
        QueryWrapper<User> wrapper2 = new QueryWrapper<>();
        wrapper2.eq("phone", user.getPhone());
        User user2 = baseMapper.selectOne(wrapper2);
        if(user2 != null){
            log.error("手机号已存在{}",user.getPhone());
            throw new SystemException(ResponseCodeEnum.PHONE_EXITS);
        }

        //邮箱唯一
        QueryWrapper<User> wrapper3 = new QueryWrapper<>();
        wrapper3.eq("email", user.getEmail());
        User user3 = baseMapper.selectOne(wrapper3);
        if(user3 != null){
            log.error("邮箱已存在{}",user.getEmail());
            throw new SystemException(ResponseCodeEnum.EMAIL_EXITS);
        }
    }
}
