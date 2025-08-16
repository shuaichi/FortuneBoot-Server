package com.fortuneboot.repository.fortune;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fortuneboot.domain.entity.fortune.FortuneTagRelationEntity;

import java.util.List;
import java.util.Map;

/**
 * 交易标签和账单的关系表
 *
 * @Author work.chi.zhang@gmail.com
 * @Date 2024/6/5 23:36
 **/
public interface FortuneTagRelationRepo extends IService<FortuneTagRelationEntity> {

    /**
     * 根据账单id批量查询
     *
     * @param billId
     * @return
     */
    List<FortuneTagRelationEntity> getByBillId(Long billId);

    /**
     * 根据账单idList批量查询
     *
     * @param billIdList
     * @return
     */
    Map<Long, List<FortuneTagRelationEntity>> getByBillIdList(List<Long> billIdList);

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
     * 根据账单id批量删除
     *
     * @param billIds
     */
    void removeByBillIds(List<Long> billIds);

    /**
     * 根据标签id查询标签是否被使用
     *
     * @param tagId
     * @return
     */
    Boolean existByTagId(Long tagId);
}
