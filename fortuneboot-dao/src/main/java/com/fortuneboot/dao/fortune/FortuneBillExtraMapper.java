package com.fortuneboot.dao.fortune;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fortuneboot.domain.entity.fortune.FortuneBillExtraEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 账单附加费用
 *
 * @author zhangchi118
 **/
public interface FortuneBillExtraMapper extends BaseMapper<FortuneBillExtraEntity> {

    /**
     * 根据id物理删除
     *
     * @param ids id列表
     */
    void phyDeleteByIds(@Param("ids") List<Long> ids);
}