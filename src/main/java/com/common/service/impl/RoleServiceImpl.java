package com.common.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.exception.SystemException;
import com.common.mapper.UserRoleMapper;
import com.common.model.dto.SearchRoleDto;
import com.common.model.entity.Role;
import com.common.model.enums.MenuStatusEnum;
import com.common.model.enums.MenuTypeEnum;
import com.common.model.vo.RoleListVo;
import com.common.response.ResponseCodeEnum;
import com.common.service.IRoleMenuService;
import com.common.service.IRoleService;
import com.common.service.IUserRoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import com.common.mapper.RoleMenuMapper;
import com.common.mapper.RoleMapper;
import com.common.model.entity.RoleMenu;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements IRoleService {

    private final RoleMenuMapper roleMenuMapper;

    private final IUserRoleService userRoleService;

    private final IRoleMenuService roleMenuService;

    @Override
    public Map<String, Object> queryRoleListByPage(Integer pageNo, Integer pageSize, SearchRoleDto roleDto) {
        Page<Role> pageInfo = new Page<Role>(pageNo, pageSize);
        QueryWrapper<Role> wrapper = new QueryWrapper<>();
        wrapper.like(StringUtils.isNotBlank(roleDto.getName()),"name",roleDto.getName().trim());
        wrapper.like(StringUtils.isNotBlank(roleDto.getCode()),"code",roleDto.getCode().trim());
        wrapper.eq(null != roleDto.getStatus(),"status",roleDto.getStatus());
        wrapper.orderByDesc("create_time");
        Page<Role> userPage = baseMapper.selectPage(pageInfo, wrapper);
        if(userPage != null){
            List<Role> userList = userPage.getRecords();
            List<RoleListVo> list = new ArrayList<RoleListVo>();
            if(!CollectionUtils.isEmpty(userList)){
                userList.stream().forEach(role ->{
                    RoleListVo roleListVo = new RoleListVo();
                    BeanUtils.copyProperties(role,roleListVo);
                    if(role.getStatus().intValue() == MenuStatusEnum.OPEN.getCode()){
                        roleListVo.setChecked(true);
                    }else{
                        roleListVo.setChecked(false);
                    }
                    list.add(roleListVo);
                });
            }
            Map<String,Object> map = new HashMap<>();
            map.put("data", list);
            map.put("total", userPage.getTotal());
            return map;
        }
        return null;
    }

    @Override
    public List<String> queryRoleList() {
        List<String> list = new ArrayList<>();
        List<Role> roleList = baseMapper.selectList(null);
        if(!CollectionUtils.isEmpty(roleList)){
            roleList.stream().forEach(role -> {
                list.add(role.getName());
            });
            return list;
        }
        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void savePermission(Integer roleId, List<Integer> menuIds) {
        //分配前将之前所拥有的菜单id删除再重新保存
        QueryWrapper<RoleMenu> wrapper = new QueryWrapper<>();
        wrapper.eq("role_id", roleId);
        List<Integer> roleMenuIds = roleMenuMapper.selectList(wrapper).stream().map(RoleMenu::getId).collect(Collectors.toList());
        if(!CollectionUtils.isEmpty(roleMenuIds)){
            roleMenuMapper.deleteBatchIds(roleMenuIds);
        }
        //保存新的菜单id
        if(!CollectionUtils.isEmpty(menuIds)){
            menuIds.stream().forEach(menuId ->{
                RoleMenu roleMenu = new RoleMenu();
                roleMenu.setRoleId(roleId);
                roleMenu.setMenuId(menuId);
                roleMenuMapper.insert(roleMenu);
            });
        }
    }

    @Override
    public void saveRole(Role role) throws SystemException {
        log.info("正在新增{}角色...",role.getName());
        //角色名称和标识唯一
        QueryWrapper<Role> wrapper = new QueryWrapper<>();
        wrapper.eq("name",role.getName());
        Role selectOne = baseMapper.selectOne(wrapper);
        if(null != selectOne){
            log.error("角色名称{}已存在，新增失败...", role.getName());
            throw new SystemException(ResponseCodeEnum.ROLE_NAME_EXITS);
        }
        QueryWrapper<Role> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("code",role.getCode());
        Role selectTwo = baseMapper.selectOne(wrapper);
        if(null != selectTwo){
            log.error("角色标识{}已存在，新增失败...", role.getName());
            throw new SystemException(ResponseCodeEnum.ROLE_CODE_EXITS);
        }
        //新增角色
        baseMapper.insert(role);
    }

    @Override
    public void updateRole(Role role) throws SystemException {
        Role oldRole = baseMapper.selectById(role.getId());
        log.info("正在修改{}角色...",oldRole.getName());
        //角色名称和标识唯一
        QueryWrapper<Role> wrapper = new QueryWrapper<>();
        wrapper.eq("name",role.getName());
        Role selectOne = baseMapper.selectOne(wrapper);
        if((null != selectOne) && (oldRole.getId().intValue() != selectOne.getId())){
            log.error("角色名称{}已存在，修改失败...", role.getName());
            throw new SystemException(ResponseCodeEnum.ROLE_NAME_EXITS);
        }
        QueryWrapper<Role> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("code",role.getCode());
        Role selectTwo = baseMapper.selectOne(queryWrapper);
        if((null != selectTwo) && (oldRole.getId().intValue() != selectTwo.getId())){
            log.error("角色标识{}已存在，修改失败...", role.getName());
            throw new SystemException(ResponseCodeEnum.ROLE_CODE_EXITS);
        }
        //修改角色
        BeanUtils.copyProperties(role,oldRole);
        baseMapper.updateById(oldRole);
        log.info("修改后的角色{}",oldRole);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRole(Integer id) throws SystemException {
        Role role = baseMapper.selectById(id);
        if(null != role){
            log.info("正在进行删除{}角色操作...",role.getName());
            //管理员角色无法进行删除
            if(role.getCode().equalsIgnoreCase("admin")){
                log.error("管理员角色无权删除！");
                throw new SystemException(ResponseCodeEnum.INSUFFICIENT_AUTHORITY);
            }
            //删除角色前将用户角色关联表和角色菜单关联表中的数据删除
            //删除用户角色关联表数据
            userRoleService.removeByRoleId(id);
            //删除角色菜单关联表数据
            roleMenuService.removeByRoleId(id);
            //最后删除角色
            baseMapper.deleteById(id);
            log.info("角色{}删除成功！",role.getName());
        }
    }

    @Override
    public void updateRoleStatus(Integer id) throws SystemException {
        Role role = baseMapper.selectById(id);
        log.info("正在修改角色{}的状态...",role.getName());
        //管理员角色无权修改
        if("admin".equalsIgnoreCase(role.getCode())) {
            throw new SystemException(ResponseCodeEnum.INSUFFICIENT_AUTHORITY);
        }
        if(role.getStatus().intValue() == MenuStatusEnum.OPEN.getCode()) {
            role.setStatus(MenuStatusEnum.CLOSE.getCode());
        }else{
            role.setStatus(MenuStatusEnum.OPEN.getCode());
        }
        baseMapper.updateById(role);
        log.info("角色{}的状态修改成功",role.getName());
    }
}
