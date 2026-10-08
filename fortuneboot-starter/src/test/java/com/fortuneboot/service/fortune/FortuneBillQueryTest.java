package com.fortuneboot.service.fortune;

import com.fortuneboot.domain.query.fortune.FortuneBillQuery;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 账单查询条件测试
 *
 * @author zhangchi118
 * @date 2026/10/8
 **/
class FortuneBillQueryTest {

    @Test
    @DisplayName("传入成员id时按成员关联表筛选")
    void addQueryCondition_memberIds_addedToWrapper() {
        FortuneBillQuery query = new FortuneBillQuery();
        query.setMemberIds(List.of(1L, 2L));

        String sqlSegment = query.addQueryCondition().getSqlSegment();

        assertThat(sqlSegment).contains("fmr.member_id IN");
    }

    @Test
    @DisplayName("未传成员id时不添加成员筛选")
    void addQueryCondition_noMemberIds_noMemberFilter() {
        FortuneBillQuery query = new FortuneBillQuery();
        query.setBookId(1L);

        String sqlSegment = query.addQueryCondition().getSqlSegment();

        assertThat(sqlSegment).doesNotContain("fmr.member_id");
    }
}
