package com.fortuneboot.dao.fortune;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fortuneboot.domain.entity.fortune.FortuneAccountEntity;
import com.fortuneboot.domain.vo.fortune.include.FortuneAssetsLiabilitiesVo;
import com.fortuneboot.domain.vo.fortune.include.FortunePieVo;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 账户Dao
 *
 * @Author work.chi.zhang@gmail.com
 * @Date 2024/06/03 22:59
 */
public interface FortuneAccountMapper extends BaseMapper<FortuneAccountEntity> {

    @Select("""
            SELECT \n
                account_name AS name,\n
                balance AS `value`,\n
                ROUND((balance / SUM(balance) OVER ()) * 100, 2) AS percent\n
            FROM \n
                fortune_account \n
            WHERE \n
                group_id = ${groupId}\n
                AND balance > 0\n
                AND deleted = FALSE;
            """)
    List<FortunePieVo> getTotalAssets(Long groupId);

    @Select("""
            SELECT \n
                account_name AS name,\n
                balance AS `value`,\n
                ROUND((balance / SUM(balance) OVER ()) * 100, 2) AS percent\n
            FROM \n
                fortune_account \n
            WHERE \n
                group_id = ${groupId}\n
                AND balance < 0\n
                AND deleted = FALSE;
            """)
    List<FortunePieVo> getTotalLiabilities(Long groupId);

    @Select("""
            SELECT
                COALESCE(SUM(IF(balance > 0, balance, 0)), 0) AS totalAssets,
                COALESCE(ABS(SUM(IF(balance < 0, balance, 0))), 0) AS totalLiabilities,
                COALESCE(SUM(IF(balance > 0, balance, 0)) + SUM(IF(balance < 0, balance, 0)), 0) AS netAssets
            FROM
                fortune_account
            WHERE deleted = FALSE
                AND group_id = ${groupId};
            """)
    FortuneAssetsLiabilitiesVo getFortuneAssetsLiabilities(Long groupId);
}
