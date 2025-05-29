package com.fortuneboot.dao.fortune;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fortuneboot.domain.entity.fortune.FortuneCategoryRelationEntity;
import org.apache.ibatis.annotations.Delete;

import java.util.List;

/**
 * 分类账单关系
 *
 * @author zhangchi118
 * @date 2025/1/29 23:29
 **/
public interface FortuneCategoryRelationMapper extends BaseMapper<FortuneCategoryRelationEntity> {

    /**
     * 根据id
     * @param ids
     */
    @Delete("DELETE FROM fortune_category_relation WHERE category_relation_id IN #{ids}")
    void phyDeleteByIds(List<Long> ids);
}
