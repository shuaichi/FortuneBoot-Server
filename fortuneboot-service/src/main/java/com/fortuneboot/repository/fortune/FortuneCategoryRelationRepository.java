package com.fortuneboot.repository.fortune;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fortuneboot.domain.entity.fortune.FortuneCategoryRelationEntity;

import java.util.List;

/**
 * 分类账单关系
 *
 * @author zhangchi118
 * @date 2025/1/29 23:30
 **/
public interface FortuneCategoryRelationRepository extends IService<FortuneCategoryRelationEntity> {

    /**
     * 根据账单id批量查询
     *
     * @param billId
     * @return
     */
    List<FortuneCategoryRelationEntity> getByBillId(Long billId);
}
