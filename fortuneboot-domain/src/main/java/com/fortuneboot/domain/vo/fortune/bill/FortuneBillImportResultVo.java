package com.fortuneboot.domain.vo.fortune.bill;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author zhangchi118
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FortuneBillImportResultVo {

    private Integer totalCount;

    private Integer successCount;

    private Integer failCount;

    private List<Long> billIdList;

    private String message;
}
