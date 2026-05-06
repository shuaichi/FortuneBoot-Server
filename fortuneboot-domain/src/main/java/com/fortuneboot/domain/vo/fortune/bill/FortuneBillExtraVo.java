package com.fortuneboot.domain.vo.fortune.bill;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.fortuneboot.domain.entity.fortune.FortuneBillExtraEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 账单附加费用（手续费/优惠）VO
 *
 * @author zhangchi118
 **/
@Data
public class FortuneBillExtraVo {

    public FortuneBillExtraVo() {
    }

    public FortuneBillExtraVo(FortuneBillExtraEntity entity) {
        if (ObjectUtil.isNotEmpty(entity)) {
            BeanUtil.copyProperties(entity, this);
        }
    }

    @Schema(description = "主键")
    private Long extraId;

    @Schema(description = "关联账单id")
    private Long billId;

    /**
     * 1-手续费，2-优惠
     */
    @Schema(description = "附加类型：1-手续费，2-优惠")
    private Integer extraType;

    @Schema(description = "金额")
    private BigDecimal amount;

    /**
     * 1-转出账户(from)，2-转入账户(to)
     */
    @Schema(description = "账户方向：1-转出账户(from)，2-转入账户(to)")
    private Integer accountSide;

    @Schema(description = "关联分类id（可为空）")
    private Long categoryId;

    @Schema(description = "备注")
    private String remark;
}