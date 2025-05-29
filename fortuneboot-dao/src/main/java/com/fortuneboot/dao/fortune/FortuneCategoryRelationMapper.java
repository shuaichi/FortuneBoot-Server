package com.fortuneboot.dao.fortune;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fortuneboot.domain.entity.fortune.FortuneCategoryRelationEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 分类账单关系
 *
 * @author zhangchi118
 * @date 2025/1/29 23:29
 **/
public interface FortuneCategoryRelationMapper extends BaseMapper<FortuneCategoryRelationEntity> {

    /**
     * 根据id物理删除
     *
     * @param ids
     */
    void phyDeleteByIds(@Param("ids") List<Long> ids);
}
