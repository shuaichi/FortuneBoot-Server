package com.fortuneboot.domain.vo.fortune.include;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 统计报表导出查询
 *
 * @author zhangchi118
 * @date 2026/8/4 19:44
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class ReportExportQuery extends BillIncludeQuery {

    /**
     * 报表类型：1-月报，2-年报，3-自定义
     */
    private Integer reportType;
}
