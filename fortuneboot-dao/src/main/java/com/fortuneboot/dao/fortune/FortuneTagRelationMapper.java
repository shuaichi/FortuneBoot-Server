package com.fortuneboot.dao.fortune;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fortuneboot.domain.entity.fortune.FortuneTagRelationEntity;
import org.apache.ibatis.annotations.Delete;

import java.util.List;

/**
 * 交易标签和账单的关系表
 *
 * @Author work.chi.zhang@gmail.com
 * @Date 2024/6/5 23:36
 **/
public interface FortuneTagRelationMapper extends BaseMapper<FortuneTagRelationEntity> {

    /**
     * 物理删除
     * @param ids
     */
    void phyDeleteByIds(List<Long> ids);
}
