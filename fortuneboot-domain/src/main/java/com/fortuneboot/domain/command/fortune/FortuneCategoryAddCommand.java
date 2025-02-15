package com.fortuneboot.domain.command.fortune;

import com.fortuneboot.common.enums.fortune.CategoryTypeEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @author zhangchi118
 * @date 2025/1/10 18:46
 **/
@Data
public class FortuneCategoryAddCommand {

    /**
     * 名称
     */
    @NotBlank(message = "分类名称不能为空")
    @Size(max = 50, message = "分类名称长度不能超过50个字符")
    private String categoryName;

    /**
     * 类型
     *
     * @see CategoryTypeEnum
     */
    @NotNull
    @Positive
    private Integer categoryType;

    /**
     * 账本id
     */
    @NotNull
    @Positive
    private Long bookId;

    /**
     * 父级id
     */
    private Long parentId;

    /**
     * 顺序
     */
    private Integer sort;

    /**
     * 备注
     */
    @Size(max = 512, message = "备注长度不能超过512个字符")
    private String remark;

    /**
     * 是否启用
     */
    private Boolean enable;

}
