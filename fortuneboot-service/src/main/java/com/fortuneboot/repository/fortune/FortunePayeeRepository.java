package com.fortuneboot.repository.fortune;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fortuneboot.common.enums.fortune.BillTypeEnum;
import com.fortuneboot.domain.entity.fortune.FortunePayeeEntity;

import java.util.List;

/**
 * 交易对象
 *
 * @Author work.chi.zhang@gmail.com
 * @Date 2024/6/5 23:19
 **/
public interface FortunePayeeRepository extends IService<FortunePayeeEntity> {

    /**
     * 根据账本id和名称查询
     *
     * @return
     */
    FortunePayeeEntity getByBookIdAndName(Long bookId, String payeeName);

    /**
     * 启用的交易对象
     *
     * @param bookId
     * @param billTypeEnum
     * @return
     */
    List<FortunePayeeEntity> getEnablePayeeList(Long bookId, BillTypeEnum billTypeEnum);

    /**
     * 根据idList批量查询
     *
     * @param payeeIdList
     * @return
     */
    List<FortunePayeeEntity> getByIdList(List<Long> payeeIdList);

    /**
     * 根据账本id查询
     *
     * @param bookId
     * @return
     */
    void removeByBookId(Long bookId);

    /**
     * 根据账本idList批量查询
     * @param bookIds
     */
    void removeByBookIds(List<Long> bookIds);
}
