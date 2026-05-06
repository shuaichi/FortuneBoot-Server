package com.fortuneboot.repository.fortune;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fortuneboot.domain.entity.fortune.FortuneBillExtraEntity;

import java.util.List;
import java.util.Map;

/**
 * 账单附加费用
 *
 * @author zhangchi118
 **/
public interface FortuneBillExtraRepo extends IService<FortuneBillExtraEntity> {

    /**
     * 根据账单id查询
     *
     * @param billId 账单id
     * @return 附加费用列表
     */
    List<FortuneBillExtraEntity> getByBillId(Long billId);

    /**
     * 根据账单id列表批量查询
     *
     * @param billIdList 账单id列表
     * @return 按账单id分组的附加费用
     */
    Map<Long, List<FortuneBillExtraEntity>> getByBillIdList(List<Long> billIdList);

    /**
     * 根据账单id物理删除
     *
     * @param billId 账单id
     */
    void phyRemoveByBillId(Long billId);

    /**
     * 根据账单id列表逻辑删除
     *
     * @param billIds 账单id列表
     */
    void removeByBillIds(List<Long> billIds);
}