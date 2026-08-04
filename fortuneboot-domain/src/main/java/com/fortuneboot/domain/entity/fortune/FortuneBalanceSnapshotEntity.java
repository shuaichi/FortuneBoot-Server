package com.fortuneboot.domain.entity.fortune;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fortuneboot.common.core.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 账户余额快照
 *
 * @author zhangchi118
 * @date 2026/8/4 19:43
 **/
@Data
@TableName("fortune_balance_snapshot")
@EqualsAndHashCode(callSuper = true)
public class FortuneBalanceSnapshotEntity extends BaseEntity<FortuneBalanceSnapshotEntity> {

    @Schema(description = "主键")
    @TableId(value = "snapshot_id", type = IdType.AUTO)
    private Long snapshotId;

    @Schema(description = "分组id")
    @TableField("group_id")
    private Long groupId;

    @Schema(description = "账本id")
    @TableField("book_id")
    private Long bookId;

    @Schema(description = "账户id")
    @TableField("account_id")
    private Long accountId;

    @Schema(description = "快照日期")
    @TableField("snapshot_date")
    private LocalDate snapshotDate;

    @Schema(description = "币种")
    @TableField("currency_code")
    private String currencyCode;

    @Schema(description = "账户余额")
    @TableField("balance")
    private BigDecimal balance;

    @Schema(description = "转换后余额")
    @TableField("converted_balance")
    private BigDecimal convertedBalance;

    @Schema(description = "总资产")
    @TableField("total_assets")
    private BigDecimal totalAssets;

    @Schema(description = "总负债")
    @TableField("total_liabilities")
    private BigDecimal totalLiabilities;

    @Schema(description = "净资产")
    @TableField("net_assets")
    private BigDecimal netAssets;
}
