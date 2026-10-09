package com.fortuneboot.domain.command.fortune.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 单据关联流水内部Mutation（统一写入管线使用，客户端不可直接提交）
 * <p>
 * 客户端不能指定ledgerVersion或汇总字段。
 *
 * @author work.chi.zhang@gmail.com
 * @date 2026/10/9
 */
@Data
@Builder
public class FinanceOrderEntryMutation {

    /**
     * 账本id
     */
    @NotNull(message = "账本id不能为空")
    @Positive(message = "账本id只能是正数")
    private Long bookId;

    /**
     * 单据id
     */
    @NotNull(message = "单据id不能为空")
    @Positive(message = "单据id只能是正数")
    private Long orderId;

    /**
     * 流水id，ADD时为空
     */
    private Long billId;

    /**
     * 流水操作
     */
    @NotNull(message = "流水操作不能为空")
    private Operation operation;

    /**
     * 流水内容，ADD/MODIFY必填
     */
    private FinanceOrderEntryCommand entryCommand;

    /**
     * 请求幂等ID
     */
    @NotBlank(message = "请求幂等ID不能为空")
    private String requestId;

    /**
     * 期望版本号
     */
    @NotNull(message = "期望版本号不能为空")
    @Positive(message = "期望版本号只能是正数")
    private Long expectedVersion;

    /**
     * multipart附件
     */
    private List<MultipartFile> fileList;

    /**
     * 流水操作类型
     */
    public enum Operation {
        ADD, MODIFY, CONFIRM, UNCONFIRM, VOID, RESTORE
    }
}
