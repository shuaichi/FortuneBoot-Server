package com.fortuneboot.domain.command.fortune;

import cn.hutool.core.lang.Pair;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fortuneboot.common.enums.fortune.BillTypeEnum;
import com.fortuneboot.common.serializer.CategoryAmountPairDeserializer;
import com.fortuneboot.common.serializer.CategoryAmountPairSerializer;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author work.chi.zhang@gmail.com
 * @Date 2025/1/12 23:15
 **/
@Data
public class FortuneBillAddCommand {

    /**
     * 账本id
     */
    @NotNull(message = "账本不能为空")
    @Positive
    private Long bookId;

    /**
     *标题
     */
    @NotBlank(message = "标题不能为空")
    @Size(max = 50,message = "标题长度不能超过50个字符")
    private String title;

    /**
     *交易时间
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime tradeTime;

    /**
     *账户id
     */
    private Long accountId;

    /**
     * 分类和金额
     */
    @NotNull(message = "分类和金额不能为空")
    @JsonDeserialize(using = CategoryAmountPairDeserializer.class)
    @JsonSerialize(using = CategoryAmountPairSerializer.class)
    private List<Pair<Long,BigDecimal>> categoryAmountPair;

    /**
     *金额
     */
    private BigDecimal amount;

    /**
     *汇率转换后的金额
     */
    private BigDecimal convertedAmount;

    /**
     *交易对象
     */
    private Long payeeId;

    /**
     * 流水类型
     * @see BillTypeEnum
     */
    private Integer billType;

    /**
     *转账到的账户
     */
    private Long toAccountId;

    /**
     *是否确认
     */
    private Boolean confirm;

    /**
     *是否统计
     */
    private Boolean include;

    /**
     *备注
     */
    @Size(max = 512,message = "备注长度不能超过512个字符")
    private String remark;

    /**
     * 标签id
     */
    private List<Long> tagIdList;

    /**
     * 附件
     */
    private List<MultipartFile> fileList;

}
