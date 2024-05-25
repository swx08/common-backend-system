package com.common.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.model.dto.SearchRoleDto;
import com.common.model.entity.Role;
import com.common.service.IRoleService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.StringUtils;
import com.common.mapper.RoleMenuMapper;
import com.common.mapper.RoleMapper;
import com.common.model.entity.RoleMenu;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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
@Service
@RequiredArgsConstructor
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements IRoleService {

    private final RoleMenuMapper roleMenuMapper;

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
            if(!CollectionUtils.isEmpty(userList)){
                Map<String,Object> map = new HashMap<>();
                map.put("data", userList);
                map.put("total", userPage.getTotal());
                return map;
            }
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
}
