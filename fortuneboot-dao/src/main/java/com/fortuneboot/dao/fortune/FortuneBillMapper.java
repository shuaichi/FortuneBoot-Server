package com.fortuneboot.dao.fortune;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fortuneboot.domain.entity.fortune.FortuneBillEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 账单流水Mapper
 *
 * @Author work.chi.zhang@gmail.com
 * @Date 2024/6/3 23:27
 **/
public interface FortuneBillMapper extends BaseMapper<FortuneBillEntity> {

    @Select("SELECT\n" +
            "    bill.*\n" +
            "FROM fortune_bill AS bill\n" +
            "LEFT JOIN fortune_category_relation AS fcr ON bill.bill_id = fcr.bill_id\n" +
            "LEFT JOIN fortune_tag_relation AS ftr ON bill.bill_id = ftr.bill_id\n" +
            " ${ew.customSqlSegment}" +
            "GROUP BY bill.bill_id ")
    IPage<FortuneBillEntity> getPage(Page<FortuneBillEntity> page, @Param(Constants.WRAPPER) Wrapper<FortuneBillEntity> wrapper);
}
