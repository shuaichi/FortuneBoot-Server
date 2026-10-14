package com.fortuneboot.system.repository;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.fortuneboot.domain.entity.system.SysUserEntity;
import com.fortuneboot.repository.system.impl.SysUserRepoImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

@DisplayName("系统用户仓库空集合查询契约")
class SysUserRepoImplEmptyCollectionTest {

    private SysUserRepoImpl repo;

    @BeforeEach
    void setUp() {
        repo = spy(SysUserRepoImpl.class);
    }

    @Test
    @DisplayName("空用户ID集合返回空列表且不查询")
    void getUsersByIds_withEmptyIds_returnsEmptyWithoutQuery() {
        lenient().doReturn(List.of()).when(repo).list(anyUserQuery());

        assertThat(repo.getUsersByIds(List.of())).isEmpty();

        verify(repo, never()).list(anyUserQuery());
    }

    private static Wrapper<SysUserEntity> anyUserQuery() {
        return any();
    }
}
