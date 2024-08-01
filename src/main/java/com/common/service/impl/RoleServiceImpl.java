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
import com.common.response.ResultData;
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
        QueryWrapper<Role> wrapper = buildQueryWrapper(roleDto);
        Page<Role> userPage = baseMapper.selectPage(pageInfo, wrapper);

        List<RoleListVo> list = convertToListVo(userPage.getRecords());

        Map<String, Object> map = new HashMap<>();
        map.put("data", list);
        map.put("total", userPage.getTotal());
        return map;
    }

    private QueryWrapper<Role> buildQueryWrapper(SearchRoleDto roleDto) {
        QueryWrapper<Role> wrapper = new QueryWrapper<>();
        wrapper.like(StringUtils.isNotBlank(roleDto.getName()), "name", roleDto.getName().trim());
        wrapper.like(StringUtils.isNotBlank(roleDto.getCode()), "code", roleDto.getCode().trim());
        wrapper.eq(null != roleDto.getStatus(), "status", roleDto.getStatus());
        wrapper.orderByDesc("create_time");
        return wrapper;
    }

    private List<RoleListVo> convertToListVo(List<Role> userList) {
        List<RoleListVo> list = new ArrayList<>();
        if (!CollectionUtils.isEmpty(userList)) {
            userList.forEach(role -> {
                RoleListVo roleListVo = new RoleListVo();
                BeanUtils.copyProperties(role, roleListVo);
                if (null != role.getStatus() && role.getStatus().intValue() == MenuStatusEnum.OPEN.getCode()) {
                    roleListVo.setChecked(true);
                } else {
                    roleListVo.setChecked(false);
                }
                list.add(roleListVo);
            });
        }
        return list;
    }


    @Override
    public List<String> queryRoleList() {
        List<String> list = new ArrayList<>();
        QueryWrapper<Role> wrapper = new QueryWrapper<>();
        wrapper.eq("status",MenuStatusEnum.OPEN.getCode());
        wrapper.orderByDesc("create_time");
        List<Role> roleList = baseMapper.selectList(wrapper);
        if(!CollectionUtils.isEmpty(roleList)){
            roleList.forEach(role -> {
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
            menuIds.forEach(menuId ->{
                RoleMenu roleMenu = new RoleMenu();
                roleMenu.setRoleId(roleId);
                roleMenu.setMenuId(menuId);
                roleMenuMapper.insert(roleMenu);
            });
        }
    }

    @Override
    public ResultData saveRole(Role role) throws SystemException {
        String roleName = role.getName();
        log.info("正在新增{}角色...", roleName);

        // 角色名称和标识唯一性检查
        verifyRoleNameAndCodeUnique(role);

        // 新增角色
        if(baseMapper.insert(role) > 0){
            log.info("角色{}新增成功！", roleName);
            return ResultData.success();
        }else{
            log.error("角色{}新增失败...", roleName);
            return ResultData.fail(1023, "角色新增失败！");
        }
    }

    private void verifyRoleNameAndCodeUnique(Role role) throws SystemException {
        // 角色名称唯一性检查
        QueryWrapper<Role> wrapperName = new QueryWrapper<>();
        wrapperName.eq("name", role.getName());
        Role selectOne = baseMapper.selectOne(wrapperName);
        if (null != selectOne) {
            log.error("角色名称{}已存在，新增失败...", role.getName());
            throw new SystemException(ResponseCodeEnum.ROLE_NAME_EXITS);
        }

        // 角色标识唯一性检查
        wrapperName.clear();
        wrapperName.eq("code", role.getCode());
        selectOne = baseMapper.selectOne(wrapperName);
        if (null != selectOne) {
            log.error("角色标识{}已存在，新增失败...", role.getCode());
            throw new SystemException(ResponseCodeEnum.ROLE_CODE_EXITS);
        }
    }


    @Override
    public ResultData updateRole(Role role) throws SystemException {
        // 查询旧角色信息
        Role oldRole = baseMapper.selectById(role.getId());
        if (oldRole == null) {
            log.error("角色不存在，无法修改。");
            return ResultData.fail(1024, "角色不存在，无法修改。");
        }

        log.info("正在修改{}角色...", oldRole.getName());

        // 角色名称唯一性和标识唯一性检查
        verifyRoleNameAndCodeUniqueWithUpdate(oldRole,role);

        // 修改角色信息
        BeanUtils.copyProperties(role, oldRole);
        int updateResult = baseMapper.updateById(oldRole);

        if (updateResult > 0) {
            log.info("角色{}修改成功！", oldRole.getName());
            return ResultData.success();
        } else {
            log.error("角色{}修改失败...", oldRole.getName());
            return ResultData.fail(1023, "角色修改失败！");
        }
    }

    private void verifyRoleNameAndCodeUniqueWithUpdate(Role oldRole,Role role) throws SystemException {
        QueryWrapper<Role> wrapper = new QueryWrapper<>();
        wrapper.eq("name", role.getName());
        // 排除自身
        wrapper.ne("id", oldRole.getId());
        Role selectOne = baseMapper.selectOne(wrapper);
        if (selectOne != null) {
            log.error("角色名称{}已存在，修改失败...", role.getName());
            throw new SystemException(ResponseCodeEnum.ROLE_NAME_EXITS);
        }

        // 角色标识唯一性检查
        wrapper.clear();
        wrapper.eq("code", role.getCode());
        // 排除自身
        wrapper.ne("id", oldRole.getId());
        selectOne = baseMapper.selectOne(wrapper);
        if (selectOne != null) {
            log.error("角色标识{}已存在，修改失败...", role.getCode());
            throw new SystemException(ResponseCodeEnum.ROLE_CODE_EXITS);
        }
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultData deleteRole(Integer id) throws SystemException {
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
            if(baseMapper.deleteById(id) > 0) {
                log.info("角色{}删除成功！",role.getName());
                return ResultData.success();
            }else{
                log.error("角色{}删除失败...",role.getName());
                return ResultData.fail(1023, "角色删除失败！");
            }
        }
        return ResultData.fail(1027, "角色不存在，删除失败！");
    }

    @Override
    public ResultData updateRoleStatus(Integer id) throws SystemException {
        // 查询角色信息
        Role role = baseMapper.selectById(id);
        if (role == null) {
            log.error("角色不存在，无法修改状态.");
            throw new SystemException(ResponseCodeEnum.ROLE_NOT_EXITS);
        }

        // 检查管理员角色权限
        if ("admin".equalsIgnoreCase(role.getCode())) {
            log.error("管理员角色无权修改状态.");
            throw new SystemException(ResponseCodeEnum.INSUFFICIENT_AUTHORITY);
        }

        // 更新角色状态
        int currentStatusCode = role.getStatus().intValue();
        int newStatusCode;
        if (currentStatusCode == MenuStatusEnum.OPEN.getCode()) {
            newStatusCode = MenuStatusEnum.CLOSE.getCode();
        } else {
            newStatusCode = MenuStatusEnum.OPEN.getCode();
        }
        role.setStatus(newStatusCode);
        if(baseMapper.updateById(role) > 0) {
            log.info("角色{}的状态修改成功", role.getName());
            return ResultData.success();
        }else {
            log.error("角色{}的状态修改失败", role.getName());
            return ResultData.fail(1023, "角色状态修改失败！");
        }
    }

}
