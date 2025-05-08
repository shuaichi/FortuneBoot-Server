package com.fortuneboot.domain.vo.fortune;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fortuneboot.common.enums.fortune.GoodsKeeperStatusEnum;
import com.fortuneboot.domain.entity.fortune.FortuneGoodsKeeperEntity;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
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
        this.holdingTime = this.calculateHoldingTime();
        this.dailyAverageCost = this.calculateDailyAverageCost();
        this.useByTimesDesc = this.useByTimes ? "是" : "否";
        this.statusDesc = GoodsKeeperStatusEnum.getDescByIndex(this.status);
        this.isOverWarranty = this.calculateIsOverWarranty();
    }

    private Long calculateHoldingTime() {
        if (Objects.equals(this.status, GoodsKeeperStatusEnum.ACTIVE.getValue())) {
            return LocalDate.now().toEpochDay() - this.purchaseDate.toEpochDay() + 1;
        } else {
            return this.retiredDate.toEpochDay() - this.purchaseDate.toEpochDay() + 1;
        }
    }

    /**
     * 计算平均使用成本
     */
    private BigDecimal calculateDailyAverageCost() {
        BigDecimal between;
        if (this.useByTimes) {
            between = new BigDecimal(this.usageNum);
        } else {
            this.usageNum = null;
            between = new BigDecimal(this.holdingTime);
        }
        // 计算公式：价格 / 使用次数(时间间隔)
        return this.getPrice().divide(between, 2, RoundingMode.HALF_UP);
    }

    /**
     * 是否过保
     *
     * @return
     */
    private String calculateIsOverWarranty() {
        if (Objects.nonNull(this.warrantyDate)) {
            return LocalDate.now().isAfter(this.warrantyDate) ? "已过保" : "未过保";
        } else {
            return StringUtils.EMPTY;
        }
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
     * 分类名称
     */
    private String categoryName;

    /**
     * 标签id
     */
    private Long tagId;

    /**
     * 标签名称
     */
    private String tagName;

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
     * 持有时间(天)
     */
    private Long holdingTime;

    /**
     * 保修日期
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate warrantyDate;

    /**
     * 是否过保
     */
    private String isOverWarranty;

    /**
     * 按次使用
     */
    private Boolean useByTimes;

    /**
     * 按次使用描述
     */
    private String useByTimesDesc;

    /**
     * 使用次数
     */
    private Long usageNum;

    /**
     * 状态
     *
     * @see com.fortuneboot.common.enums.fortune.GoodsKeeperStatusEnum
     */
    private Integer status;

    /**
     * 状态描述
     */
    private String statusDesc;

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
