package com.fortuneboot.repository.fortune;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fortuneboot.domain.entity.fortune.FortuneCategoryRelationEntity;

import java.util.List;
import java.util.Map;

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

    /**
     * 根据账单idList批量查询
     *
     * @param billIdList
     * @return
     */
    Map<Long, List<FortuneCategoryRelationEntity>> getByBillIdList(List<Long> billIdList);

    /**
     * 根据账单idList批量删除
     *
     * @param billIds
     */
    void removeByBillIds(List<Long> billIds);

    /**
     * 根据账单id删除
     *
     * @param billId
     */
    void removeByBillId(Long billId);

    /**
     * 根据账单id物理删除
     *
     * @param billId
     */
    void phyRemoveByBillId(Long billId);

    /**
     * 根据分类id查询是否分类已被使用
     *
     * @param categoryId
     * @return
     */
    Boolean existByCategoryId(Long categoryId);
}
