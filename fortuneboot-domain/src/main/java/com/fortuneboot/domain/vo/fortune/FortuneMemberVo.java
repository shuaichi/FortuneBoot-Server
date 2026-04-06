package com.fortuneboot.domain.vo.fortune;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.fortuneboot.domain.entity.fortune.FortuneMemberEntity;
import lombok.Data;

/**
 *
 * @author zhangchi118
 * @date 2026/3/19 09:26
 **/
@Data
public class FortuneMemberVo {

    public FortuneMemberVo(FortuneMemberEntity entity) {
        if (ObjectUtil.isNotEmpty(entity)) {
            BeanUtil.copyProperties(entity, this);
        }
    }

    private Long memberId;
    private Long bookId;
    private String memberName;
    private Boolean enable;
    private Integer sort;
    private String remark;
}
