package com.fortuneboot.service.permission;

import cn.hutool.core.util.ObjectUtil;
import com.fortuneboot.common.enums.fortune.RoleTypeEnum;
import com.fortuneboot.domain.entity.fortune.FortuneBookEntity;
import com.fortuneboot.domain.entity.fortune.FortuneUserGroupRelationEntity;
import com.fortuneboot.infrastructure.user.AuthenticationUtils;
import com.fortuneboot.infrastructure.user.web.SystemLoginUser;
import com.fortuneboot.repository.fortune.FortuneBookRepository;
import com.fortuneboot.repository.fortune.FortuneGroupRepository;
import com.fortuneboot.repository.fortune.FortuneUserGroupRelationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @Author work.chi.zhang@gmail.com
 * @Date 2024/6/9 17:27
 **/
@Service("fortune")
@RequiredArgsConstructor
public class FortunePermissionService {

    private final FortuneGroupRepository fortuneGroupRepository;

    private final FortuneUserGroupRelationRepository fortuneUserGroupRelationRepository;

    private final FortuneBookRepository fortuneBookRepository;

    /**
     * 验证是否是登录用户
     *
     * @param userId 用户id
     * @return 用户是否是登录用户
     */
    public Boolean has(Long userId) {
        SystemLoginUser loginUser = AuthenticationUtils.getSystemLoginUser();
        return ObjectUtil.equals(loginUser.getUserId(), userId);
    }

    public Boolean groupVisitorPermission(Long groupId) {
        SystemLoginUser loginUser = AuthenticationUtils.getSystemLoginUser();
        List<FortuneUserGroupRelationEntity> userGroupRelation = fortuneUserGroupRelationRepository.getByGroupId(groupId);
        // 未根据分组id查到分组关系，说明没有权限
        if (CollectionUtils.isEmpty(userGroupRelation)) {
            return Boolean.FALSE;
        }
        for (FortuneUserGroupRelationEntity userGroupRelationEntity : userGroupRelation) {
            // 遍历分组关系，如果查到，说明有权限
            if (loginUser.getUserId().equals(userGroupRelationEntity.getUserId())) {
                return Boolean.TRUE;
            }
        }
        // 遍历未查到，说明没有权限
        return Boolean.FALSE;
    }

    public Boolean groupOwnerPermission(Long groupId) {
        SystemLoginUser loginUser = AuthenticationUtils.getSystemLoginUser();
        List<FortuneUserGroupRelationEntity> userGroupRelation = fortuneUserGroupRelationRepository.getByGroupId(groupId);
        // 未根据分组id查到分组关系，说明没有权限
        if (CollectionUtils.isEmpty(userGroupRelation)) {
            return Boolean.FALSE;
        }
        for (FortuneUserGroupRelationEntity userGroupRelationEntity : userGroupRelation) {
            // 遍历分组关系，如果查到，说明有权限
            if (ObjectUtil.equals(loginUser.getUserId(), userGroupRelationEntity.getUserId())
                    && ObjectUtil.equal(userGroupRelationEntity.getRoleType(), RoleTypeEnum.OWNER.getValue())) {
                return Boolean.TRUE;
            }
        }
        // 遍历未查到，说明没有权限
        return Boolean.FALSE;
    }

    public Boolean groupOwnerPermissionByRelationId(Long relationId) {
        FortuneUserGroupRelationEntity userGroupRelation = fortuneUserGroupRelationRepository.getById(relationId);
        return this.groupOwnerPermission(userGroupRelation.getGroupId());
    }

    public Boolean bookOwnerPermission(Long bookId) {
        FortuneBookEntity book = fortuneBookRepository.getById(bookId);
        // 未查找账本，则说明没权限
        if (ObjectUtil.isEmpty(book)) {
            return Boolean.FALSE;
        }
        return this.groupOwnerPermission(book.getGroupId());
    }
}
