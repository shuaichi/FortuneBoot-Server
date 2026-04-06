package com.fortuneboot.dao.fortune;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fortuneboot.domain.entity.fortune.FortuneMemberRelationEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 *
 * @author zhangchi118
 * @date 2026/3/19 09:29
 **/
public interface FortuneMemberRelationMapper extends BaseMapper<FortuneMemberRelationEntity> {
    void phyDeleteByIds(@Param("ids") List<Long> ids);
}
