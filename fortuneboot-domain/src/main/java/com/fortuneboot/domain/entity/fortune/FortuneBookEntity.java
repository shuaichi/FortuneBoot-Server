package com.fortuneboot.domain.entity.fortune;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fortuneboot.common.core.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 账本表
 *
 * @Author work.chi.zhang@gmail.com
 * @Date 2024/6/4 22:52
 **/
@Data
@TableName("fortune_book")
@EqualsAndHashCode(callSuper = true)
public class FortuneBookEntity extends BaseEntity<FortuneBookEntity> {

    @Schema(description = "主键")
    @TableId(value = "book_id", type = IdType.AUTO)
    private Long bookId;

    @Schema(description = "所属组id")
    @TableField("group_id")
    private Long groupId;

    @Schema(description = "账本名称")
    @TableField("book_name")
    private String bookName;

    @Schema(description = "默认币种")
    @TableField("default_currency")
    private String defaultCurrency;

    @Schema(description = "默认支出账户ID")
    @TableField("default_expense_account_id")
    private Long defaultExpenseAccountId;

    @Schema(description = "默认收入账户ID")
    @TableField("default_income_account_id")
    private Long defaultIncomeAccountId;

    @Schema(description = "默认转出账户ID")
    @TableField("default_transfer_out_account_id")
    private Long defaultTransferOutAccountId;

    @Schema(description = "默认转入账户ID")
    @TableField("default_transfer_in_account_id")
    private Long defaultTransferInAccountId;

    @Schema(description = "顺序")
    @TableField("sequence")
    private Integer sequence;

    @Schema(description = "备注")
    @TableField("remark")
    private String remark;

    @Schema(description = "是否启用")
    @TableField("enable")
    private Boolean enable;

    @Schema(description = "回收站")
    @TableField("recycle_bin")
    private Boolean recycleBin;
}
