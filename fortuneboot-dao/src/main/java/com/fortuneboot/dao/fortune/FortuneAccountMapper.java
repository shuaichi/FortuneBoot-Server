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

}
