package com.fortuneboot.domain.vo.fortune;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fortuneboot.common.enums.fortune.GoodsKeeperStatusEnum;
import com.fortuneboot.domain.entity.fortune.FortuneGoodsKeeperEntity;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import org.springframework.beans.BeanUtils;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * 归物
 *
 * @author zhangchi118
 * @date 2025/5/6 14:43
 **/
@Data
public class FortuneGoodsKeeperVo {

    public FortuneGoodsKeeperVo(FortuneGoodsKeeperEntity entity) {
        if (Objects.nonNull(entity)) {
            BeanUtils.copyProperties(entity, this);
        }
        dailyAverageCost = this.calculateDailyAverageCost();
    }

    /**
     * 计算平均使用成本
     */
    private BigDecimal calculateDailyAverageCost() {
        BigDecimal between;
        if (useByTimes) {
            between = new BigDecimal(usageNum);
        } else {
            if (Objects.equals(status, GoodsKeeperStatusEnum.ACTIVE.getValue())) {
                between = BigDecimal.valueOf(LocalDate.now().toEpochDay() - purchaseDate.toEpochDay() + 1);
            } else {
                between = BigDecimal.valueOf(retiredDate.toEpochDay() - purchaseDate.toEpochDay() + 1);
            }
        }
        // 计算公式：价格 / 使用次数(时间间隔)
        return this.getPrice().divide(between, 2, RoundingMode.HALF_UP);
    }

    /**
     * 账本id
     */
    private Long bookId;

    /**
     * 物品名称
     */
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
    @Positive
    private Long usageNum;

    /**
     * 状态
     *
     * @see com.fortuneboot.common.enums.fortune.GoodsKeeperStatusEnum
     */
    private Integer status;

    /**
     * 退役时间
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
    private String remark;

    /**
     * 附件
     */
    private List<MultipartFile> fileList;

    /**
     * 日均成本
     */
    private BigDecimal dailyAverageCost;
}
