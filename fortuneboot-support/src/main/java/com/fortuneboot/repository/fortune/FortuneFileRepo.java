package com.fortuneboot.repository.fortune;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fortuneboot.domain.entity.fortune.FortuneFileEntity;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * 账单文件
 *
 * @Author work.chi.zhang@gmail.com
 * @Date 2024/6/5 23:02
 **/
public interface FortuneFileRepo extends IService<FortuneFileEntity> {

    /**
     * 根据id查询文件
     *
     * @param billId
     * @return
     */
    List<FortuneFileEntity> getByBillId(Long billId);

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
     * 批量判定哪些账单存在有效附件（逻辑未删除）
     * 仅返回存在附件的账单ID集合
     *
     * @param billIds 当前页账单ID集合
     * @return 存在附件的账单ID集合（去重）
     */
    Set<Long> findBillIdsWithFiles(Collection<Long> billIds);
}
