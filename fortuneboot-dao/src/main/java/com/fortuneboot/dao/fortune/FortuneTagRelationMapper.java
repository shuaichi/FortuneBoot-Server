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

    @Delete("DELETE FROM fortune_tag_relation WHERE tag_relation_id IN #{ids}")
    void phyDeleteByBillId(List<Long> ids);
}
