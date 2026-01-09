package com.fortuneboot.integrationTest.db;

import com.fortuneboot.integrationTest.IntegrationTestApplication;
import com.fortuneboot.repository.system.SysRoleRepo;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@Slf4j
@SpringBootTest(classes = IntegrationTestApplication.class)
@ExtendWith(SpringExtension.class)
@AllArgsConstructor
class SysRoleRepoImplTest {

    private final SysRoleRepo roleService;


    @Test
    @Rollback
    void testIsRoleNameDuplicated() {
        boolean addWithSame = roleService.isRoleNameDuplicated(null, "超级管理员");
        boolean updateWithSame = roleService.isRoleNameDuplicated(1L, "超级管理员");
        boolean addWithoutSame = roleService.isRoleNameDuplicated(null, "超级管理员1");

        Assertions.assertTrue(addWithSame);
        Assertions.assertFalse(updateWithSame);
        Assertions.assertFalse(addWithoutSame);
    }


    @Test
    @Rollback
    void testIsRoleKeyDuplicated() {
        boolean addWithSame = roleService.isRoleKeyDuplicated(null, "admin");
        boolean updateWithSame = roleService.isRoleKeyDuplicated(1L, "admin");
        boolean addWithoutSame = roleService.isRoleKeyDuplicated(null, "admin1");

        Assertions.assertTrue(addWithSame);
        Assertions.assertFalse(updateWithSame);
        Assertions.assertFalse(addWithoutSame);
    }


    @Test
    @Rollback
    void testIsAssignedToUsers() {
        boolean assignedRole = roleService.isAssignedToUsers(1L);
        boolean unassignedRole = roleService.isAssignedToUsers(3L);

        Assertions.assertTrue(assignedRole);
        Assertions.assertFalse(unassignedRole);
    }

}
