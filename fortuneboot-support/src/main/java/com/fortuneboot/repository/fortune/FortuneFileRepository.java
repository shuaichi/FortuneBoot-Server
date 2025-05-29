package com.fortuneboot.repository.fortune;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fortuneboot.domain.entity.fortune.FortuneFileEntity;
import jakarta.validation.constraints.Positive;

import java.util.List;

/**
 * 账单文件
 *
 * @Author work.chi.zhang@gmail.com
 * @Date 2024/6/5 23:02
 **/
public interface FortuneFileRepository extends IService<FortuneFileEntity> {

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
}
