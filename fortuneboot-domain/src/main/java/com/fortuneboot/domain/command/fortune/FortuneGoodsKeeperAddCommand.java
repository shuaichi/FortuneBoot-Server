package com.fortuneboot.domain.command.fortune;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 新建物品
 *
 * @author zhangchi118
 * @date 2025/5/6 11:05
 **/
@Data
public class FortuneGoodsKeeperAddCommand {

    /**
     * 账本id
     */
    @NotNull
    @Positive
    private Long bookId;

    /**
     * 物品名称
     */
    @NotBlank(message = "物品名称不能为空")
    @Size(max = 50, message = "物品名称长度不能超过50个字符")
    private String goodsName;

    /**
     * 分类id
     */
    private Long categoryId;

    /**
     * 标签id
     */
    private Long tagId;

    /**
     * 价格
     */
    @NotNull
    private BigDecimal price;

    /**
     * 购买日期
     */
    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate purchaseDate;

    /**
     * 保修日期
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate warrantyDate;

    /**
     * 按次使用
     */
    private Boolean useByTimes;

    /**
     * 使用次数
     */
    private Long usageNum;

    /**
     * 状态
     */
    @NotNull
    @Positive
    private Integer status;

    /**
     * 退役日期
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate retiredDate;

    /**
     * 出售价格
     */
    private Integer soldPrice;

    /**
     * 备注
     */
    @Size(max = 512,message = "备注长度不能超过512个字符")
    private String remark;

    /**
     * 附件
     */
    private List<MultipartFile> fileList;
}
