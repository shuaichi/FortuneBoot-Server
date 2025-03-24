package com.fortuneboot.repository.fortune;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fortuneboot.domain.entity.fortune.FortuneAccountEntity;
import com.fortuneboot.domain.vo.fortune.include.FortuneAssetsLiabilitiesVo;
import com.fortuneboot.domain.vo.fortune.include.FortunePieVo;

import java.util.List;

/**
 * 账户
 *
 * @Author work.chi.zhang@gmail.com
 * @Date 2024/06/03 23:02
 */
public interface FortuneAccountRepository extends IService<FortuneAccountEntity> {

    /**
     * 查询启用的账户
     *
     * @param groupId
     * @return
     */
    List<FortuneAccountEntity> getEnableAccountList(Long groupId);

    /**
     * 根据idList批量查询
     *
     * @param accountIdList
     * @return
     */
    List<FortuneAccountEntity> getByIds(List<Long> accountIdList);

    /**
     * 统计总资产
     *
     * @param groupId
     * @return
     */
    List<FortunePieVo> getTotalAssets(Long groupId);

    /**
     * 统计总负债
     *
     * @param groupId
     * @return
     */
    List<FortunePieVo> getTotalLiabilities(Long groupId);

    /**
     * 统计资产负债
     *
     * @param groupId
     * @return
     */
    FortuneAssetsLiabilitiesVo getFortuneAssetsLiabilities(Long groupId);
}
