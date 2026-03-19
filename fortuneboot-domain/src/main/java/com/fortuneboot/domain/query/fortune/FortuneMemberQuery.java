package com.fortuneboot.domain.query.fortune;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fortuneboot.common.core.page.AbstractLambdaPageQuery;
import com.fortuneboot.common.utils.mybatis.WrapperUtil;
import com.fortuneboot.domain.entity.fortune.FortuneMemberEntity;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

/**
 *
 * @author zhangchi118
 * @date 2026/3/19 09:26
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class FortuneMemberQuery extends AbstractLambdaPageQuery<FortuneMemberEntity> {

    @NotNull
    @Positive
    private Long bookId;

    private String memberName;
    private Boolean enable;

    @NotNull
    private Boolean recycleBin;

    @Override
    public LambdaQueryWrapper<FortuneMemberEntity> addQueryCondition() {
        LambdaQueryWrapper<FortuneMemberEntity> queryWrapper = WrapperUtil.getLambdaQueryWrapper(FortuneMemberEntity.class);
        queryWrapper.eq(FortuneMemberEntity::getBookId, bookId)
                .eq(FortuneMemberEntity::getRecycleBin, recycleBin)
                .eq(Objects.nonNull(enable), FortuneMemberEntity::getEnable, enable)
                .like(StringUtils.isNotBlank(memberName), FortuneMemberEntity::getMemberName, memberName)
                .orderByAsc(FortuneMemberEntity::getSort);
        return queryWrapper;
    }
}