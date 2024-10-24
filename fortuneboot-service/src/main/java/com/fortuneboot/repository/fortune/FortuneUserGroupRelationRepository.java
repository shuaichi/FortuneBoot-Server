package com.fortuneboot.repository.fortune;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fortuneboot.domain.entity.fortune.FortuneUserGroupRelationEntity;

import java.util.List;

/**
 * 用户/分组关系
 *
 * @Author work.chi.zhang@gmail.com
 * @Date 2024/6/5 23:47
 **/
public interface FortuneUserGroupRelationRepository extends IService<FortuneUserGroupRelationEntity> {

    /**
     * 通过分组ID查询
     *
     * @param groupId
     * @return
     */
    List<FortuneUserGroupRelationEntity> getByGroupId(Long groupId);

    /**
     * 通过分组ID删除
     *
     * @param groupId
     */
    void removeByGroupId(Long groupId);

    /**
     * 通过用户ID查询
     *
     * @param
     * @return
     */
    List<FortuneUserGroupRelationEntity> getByUserId();
}
