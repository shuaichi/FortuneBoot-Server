package com.fortuneboot.dao.fortune;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fortuneboot.domain.entity.fortune.FortuneFinanceOrderEntity;
import com.fortuneboot.domain.vo.fortune.include.FinanceProfitQuery;
import com.fortuneboot.domain.vo.fortune.include.FinanceProfitVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 单据表（报销单、借出单、借入单）
 *
 * @Author work.chi.zhang@gmail.com
 * @Date 2024/8/16 18:06
 */
public interface FortuneFinanceOrderMapper extends BaseMapper<FortuneFinanceOrderEntity> {

    List<FinanceProfitVo> getFinanceProfit(@Param("bookId") Long bookId, @Param("query") FinanceProfitQuery query);
}
