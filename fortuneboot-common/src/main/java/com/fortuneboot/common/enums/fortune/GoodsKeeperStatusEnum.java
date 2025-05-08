package com.fortuneboot.common.enums.fortune;

import com.fortuneboot.common.enums.BasicEnum;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Objects;

/**
 * @author zhangchi118
 * @date 2025/5/6 15:22
 **/
@Getter
public enum GoodsKeeperStatusEnum implements BasicEnum<Integer> {
    ACTIVE(1,"在役"),
    RETIRED(2,"退役"),
    SOLD(3,"出售"),
    ;
    private final Integer value;

    private final String description;

    GoodsKeeperStatusEnum(Integer value, String description) {
        this.value = value;
        this.description = description;
    }

    public static Boolean contains(Integer value){
        return Arrays.stream(values())
                .anyMatch(e -> Objects.equals(e.value, value));
    }

    public static String getDescByIndex(Integer status) {
        for (GoodsKeeperStatusEnum goodsKeeperStatusEnum : GoodsKeeperStatusEnum.values()) {
            if (Objects.equals(goodsKeeperStatusEnum.getValue(), status)) {
                return goodsKeeperStatusEnum.getDescription();
            }
        }
        return StringUtils.EMPTY;
    }
}
