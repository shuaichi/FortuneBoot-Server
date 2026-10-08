package com.fortuneboot.system.user;

import com.fortuneboot.common.enums.fortune.RoleTypeEnum;
import com.fortuneboot.common.exception.ApiException;
import com.fortuneboot.common.exception.error.ErrorCode;
import com.fortuneboot.domain.command.user.DeleteAccountCommand;
import com.fortuneboot.domain.entity.fortune.FortuneUserGroupRelationEntity;
import com.fortuneboot.factory.system.factory.UserModelFactory;
import com.fortuneboot.factory.system.model.UserModel;
import com.fortuneboot.infrastructure.cache.mem.InMemoryCacheTemplate;
import com.fortuneboot.infrastructure.user.web.SystemLoginUser;
import com.fortuneboot.repository.fortune.FortuneUserGroupRelationRepo;
import com.fortuneboot.repository.system.SysConfigRepo;
import com.fortuneboot.repository.system.SysRoleRepo;
import com.fortuneboot.repository.system.SysUserRepo;
import com.fortuneboot.service.cache.CacheCenter;
import com.fortuneboot.service.cache.CacheService;
import com.fortuneboot.service.fortune.FortuneGroupService;
import com.fortuneboot.service.login.LoginService;
import com.fortuneboot.service.system.UserApplicationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserApplicationServiceTest {

    private final SysUserRepo userRepository = mock(SysUserRepo.class);
    private final SysRoleRepo roleRepository = mock(SysRoleRepo.class);
    private final UserModelFactory userModelFactory = mock(UserModelFactory.class);
    private final SysConfigRepo sysConfigRepo = mock(SysConfigRepo.class);
    private final ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);
    private final CacheService cacheService = mock(CacheService.class);
    private final FortuneUserGroupRelationRepo relationRepo = mock(FortuneUserGroupRelationRepo.class);
    private final FortuneGroupService fortuneGroupService = mock(FortuneGroupService.class);
    private final LoginService loginService = mock(LoginService.class);

    private final UserApplicationService userApplicationService = new UserApplicationService(
            userRepository, roleRepository, userModelFactory, sysConfigRepo,
            eventPublisher, cacheService, relationRepo, fortuneGroupService, loginService);

    private final UserModel userModel = mock(UserModel.class);

    @AfterEach
    void cleanCacheCenter() {
        CacheCenter.userCache = null;
    }

    @Test
    void testDeleteAccountBySelfWhenPasswordWrong() {
        when(loginService.decryptPassword("encrypted-wrong-password")).thenReturn("wrong-password");
        doThrow(new ApiException(ErrorCode.Business.USER_PASSWORD_IS_NOT_CORRECT))
                .when(userModel).checkPassword("wrong-password");
        when(userModelFactory.loadById(1L)).thenReturn(userModel);

        SystemLoginUser loginUser = buildLoginUser(1L);
        DeleteAccountCommand command = buildCommand("encrypted-wrong-password");

        ApiException exception = Assertions.assertThrows(ApiException.class,
                () -> userApplicationService.deleteAccountBySelf(loginUser, command));

        Assertions.assertEquals(ErrorCode.Business.USER_PASSWORD_IS_NOT_CORRECT, exception.getErrorCode());
        verify(userModel, never()).deleteById();
        verify(relationRepo, never()).getByUserId(anyLong());
    }

    @Test
    void testDeleteAccountBySelfWhenAdmin() {
        when(loginService.decryptPassword("admin123")).thenReturn("admin-password");
        doThrow(new ApiException(ErrorCode.Business.USER_ADMIN_CAN_NOT_BE_DELETE))
                .when(userModel).checkSelfDeletionAllowed();
        when(userModelFactory.loadById(1L)).thenReturn(userModel);

        SystemLoginUser loginUser = buildLoginUser(1L);

        ApiException exception = Assertions.assertThrows(ApiException.class,
                () -> userApplicationService.deleteAccountBySelf(loginUser, buildCommand("admin123")));

        Assertions.assertEquals(ErrorCode.Business.USER_ADMIN_CAN_NOT_BE_DELETE, exception.getErrorCode());
        verify(userModel, never()).deleteById();
        verify(relationRepo, never()).getByUserId(anyLong());
    }

    @Test
    void testDeleteAccountBySelfWhenSoleGroupOwner() {
        when(userModelFactory.loadById(2L)).thenReturn(userModel);
        when(loginService.decryptPassword("user123")).thenReturn("user-plain-password");
        FortuneUserGroupRelationEntity ownRelation = buildRelation(10L, 100L, 2L, RoleTypeEnum.OWNER.getValue());
        when(relationRepo.getByUserId(2L)).thenReturn(List.of(ownRelation));
        when(relationRepo.getByGroupId(100L)).thenReturn(List.of(ownRelation));
        CacheCenter.userCache = mock(InMemoryCacheTemplate.class);

        userApplicationService.deleteAccountBySelf(buildLoginUser(2L), buildCommand("user123"));

        verify(fortuneGroupService).remove(100L);
        verify(relationRepo, never()).removeById(any(Long.class));
        verify(userModel).anonymizeForDeletion();
        verify(userModel).updateById();
        verify(userModel).deleteById();
        verify(cacheService).removeLoginUserByUserId(2L);
        verify(CacheCenter.userCache).delete(2L);
    }

    @Test
    void testDeleteAccountBySelfWhenSharedGroup() {
        when(userModelFactory.loadById(2L)).thenReturn(userModel);
        when(loginService.decryptPassword("user123")).thenReturn("user-plain-password");
        FortuneUserGroupRelationEntity ownRelation = buildRelation(10L, 100L, 2L, RoleTypeEnum.OWNER.getValue());
        FortuneUserGroupRelationEntity otherRelation = buildRelation(11L, 100L, 3L, RoleTypeEnum.ACTOR.getValue());
        when(relationRepo.getByUserId(2L)).thenReturn(List.of(ownRelation));
        when(relationRepo.getByGroupId(100L)).thenReturn(List.of(ownRelation, otherRelation));
        CacheCenter.userCache = mock(InMemoryCacheTemplate.class);

        userApplicationService.deleteAccountBySelf(buildLoginUser(2L), buildCommand("user123"));

        verify(fortuneGroupService, never()).remove(anyLong());
        Assertions.assertEquals(RoleTypeEnum.OWNER.getValue(), otherRelation.getRoleType());
        verify(relationRepo).updateById(otherRelation);
        verify(relationRepo).removeById(10L);
        verify(userModel).deleteById();
        verify(cacheService).removeLoginUserByUserId(2L);
    }

    private SystemLoginUser buildLoginUser(Long userId) {
        SystemLoginUser loginUser = new SystemLoginUser();
        loginUser.setUserId(userId);
        return loginUser;
    }

    private DeleteAccountCommand buildCommand(String password) {
        DeleteAccountCommand command = new DeleteAccountCommand();
        command.setPassword(password);
        return command;
    }

    private FortuneUserGroupRelationEntity buildRelation(Long relationId, Long groupId, Long userId, Integer roleType) {
        FortuneUserGroupRelationEntity relation = new FortuneUserGroupRelationEntity();
        relation.setUserGroupRelationId(relationId);
        relation.setGroupId(groupId);
        relation.setUserId(userId);
        relation.setRoleType(roleType);
        return relation;
    }

}
