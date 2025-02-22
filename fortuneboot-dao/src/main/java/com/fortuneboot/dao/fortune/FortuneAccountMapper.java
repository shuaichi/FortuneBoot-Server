package com.fortuneboot.dao.fortune;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fortuneboot.domain.entity.fortune.FortuneAccountEntity;
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

    @Select("SELECT \n" +
            "    account_name AS name,\n" +
            "    balance AS `value`,\n" +
            "    ROUND((balance / SUM(balance) OVER ()) * 100, 2) AS percent\n" +
            "FROM \n" +
            "    fortune_account \n" +
            "WHERE \n" +
            "    group_id = ${groupId}\n" +
            "    AND balance > 0\n" +
            "   AND deleted = FALSE;")
    List<FortunePieVo> getTotalAssets(Long groupId);
}
