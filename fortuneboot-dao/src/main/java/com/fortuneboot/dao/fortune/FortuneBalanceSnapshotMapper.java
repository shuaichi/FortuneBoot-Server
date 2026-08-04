package com.fortuneboot.dao.fortune;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fortuneboot.domain.entity.fortune.FortuneBalanceSnapshotEntity;
import com.fortuneboot.domain.vo.fortune.include.FortuneLineVo;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 账户余额快照Mapper
 *
 * @author zhangchi118
 * @date 2026/8/4 20:08
 **/
public interface FortuneBalanceSnapshotMapper extends BaseMapper<FortuneBalanceSnapshotEntity> {

    @Delete("DELETE FROM fortune_balance_snapshot WHERE snapshot_date = #{snapshotDate}")
    void deleteBySnapshotDate(@Param("snapshotDate") java.time.LocalDate snapshotDate);

    List<FortuneLineVo> getNetAssetsTrend(@Param("groupId") Long groupId, @Param("periodType") Integer periodType);

    List<FortuneLineVo> getAccountBalanceTrend(@Param("accountId") Long accountId, @Param("periodType") Integer periodType);
}
