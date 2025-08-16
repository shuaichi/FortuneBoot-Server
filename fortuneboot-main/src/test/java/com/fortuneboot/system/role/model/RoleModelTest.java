package com.fortuneboot.system.role.model;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fortuneboot.common.exception.ApiException;
import com.fortuneboot.common.exception.error.ErrorCode.Business;
import com.fortuneboot.factory.system.factory.RoleModelFactory;
import com.fortuneboot.factory.system.model.RoleModel;
import com.fortuneboot.repository.system.SysRoleMenuRepository;
import com.fortuneboot.repository.system.SysRoleRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class RoleModelTest {

    private final SysRoleRepository roleService = mock(SysRoleRepository.class);

    private final SysRoleMenuRepository roleMenuService = mock(SysRoleMenuRepository.class);

    private final RoleModelFactory roleModelFactory = new RoleModelFactory(roleService, roleMenuService);

    private static final long ROLE_ID = 1L;


    @Test
    void testCheckRoleNameUnique() {
        RoleModel roleWithSameName = roleModelFactory.create();
        roleWithSameName.setRoleId(ROLE_ID);
        roleWithSameName.setRoleName("role 1");
        RoleModel roleWithNewName = roleModelFactory.create();
        roleWithNewName.setRoleId(ROLE_ID);
        roleWithNewName.setRoleName("role 2");

        when(roleService.isRoleNameDuplicated(ROLE_ID, "role 1")).thenReturn(true);
        when(roleService.isRoleNameDuplicated(ROLE_ID, "role 2")).thenReturn(false);

        ApiException exception = assertThrows(ApiException.class, roleWithSameName::checkRoleNameUnique);
        Assertions.assertEquals(Business.ROLE_NAME_IS_NOT_UNIQUE, exception.getErrorCode());
        Assertions.assertDoesNotThrow(roleWithNewName::checkRoleNameUnique);

    }

    @Test
    void testCheckRoleCanBeDelete() {
        RoleModel roleModel = roleModelFactory.create();
        roleModel.setRoleId(ROLE_ID);

        when(roleService.isAssignedToUsers(ROLE_ID)).thenReturn(true);

        ApiException exception = assertThrows(ApiException.class, roleModel::checkRoleCanBeDelete);
        Assertions.assertEquals(Business.ROLE_ALREADY_ASSIGN_TO_USER, exception.getErrorCode());
    }

    @Test
    void testCheckRoleKeyUnique() {
        RoleModel roleWithSameKey = roleModelFactory.create();
        roleWithSameKey.setRoleId(ROLE_ID);
        roleWithSameKey.setRoleKey("key 1");
        RoleModel roleWithNewKey = roleModelFactory.create();
        roleWithNewKey.setRoleId(ROLE_ID);
        roleWithNewKey.setRoleKey("key 2");

        when(roleService.isRoleKeyDuplicated(ROLE_ID, "key 1")).thenReturn(true);
        when(roleService.isRoleKeyDuplicated(ROLE_ID, "key 2")).thenReturn(false);

        ApiException exception = assertThrows(ApiException.class, roleWithSameKey::checkRoleKeyUnique);
        Assertions.assertEquals(Business.ROLE_KEY_IS_NOT_UNIQUE, exception.getErrorCode());
        Assertions.assertDoesNotThrow(roleWithNewKey::checkRoleKeyUnique);
    }

}
