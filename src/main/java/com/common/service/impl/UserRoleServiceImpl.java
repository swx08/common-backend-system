package com.common.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.common.model.entity.UserRole;
import com.common.mapper.UserRoleMapper;
import com.common.service.IUserRoleService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
public class UserRoleServiceImpl extends ServiceImpl<UserRoleMapper, UserRole> implements IUserRoleService {

    @Override
    public void removeByRoleId(Integer id) {
        log.info("正在删除id为{}的角色的用户关联数据...",id);
        QueryWrapper<UserRole> wrapper = new QueryWrapper<>();
        wrapper.eq("role_id", id);
        if(baseMapper.delete(wrapper) > 0){
            log.info("删除成功！");
        }else{
            log.warn("此角色无关联用户数据！");
        }
    }
}
