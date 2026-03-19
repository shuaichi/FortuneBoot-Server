package com.fortuneboot.dao.fortune;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fortuneboot.domain.entity.fortune.FortuneAccountEntity;
import com.fortuneboot.domain.vo.fortune.include.FortuneAssetsLiabilitiesVo;
import com.fortuneboot.domain.vo.fortune.include.FortunePieVo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.util.List;

/**
 * 账户Dao
 *
 * @Author work.chi.zhang@gmail.com
 * @Date 2024/06/03 22:59
 */
public interface FortuneAccountMapper extends BaseMapper<FortuneAccountEntity> {

    @Update("UPDATE fortune_account SET balance = balance + #{amount} WHERE account_id = #{accountId}")
    int addBalanceAtomic(@Param("accountId") Long accountId, @Param("amount") BigDecimal amount);
}