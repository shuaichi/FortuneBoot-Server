package com.fortuneboot.repository.system.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fortuneboot.domain.entity.system.SysRoleMenuEntity;
import com.fortuneboot.dao.system.SysRoleMenuMapper;
import com.fortuneboot.repository.system.SysRoleMenuRepository;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 角色和菜单关联表 服务实现类
 * </p>
 *
 * @author valarchie
 * @since 2022-06-16
 */
@Service
public class SysRoleMenuRepositoryImpl extends ServiceImpl<SysRoleMenuMapper, SysRoleMenuEntity> implements
        SysRoleMenuRepository {

}
