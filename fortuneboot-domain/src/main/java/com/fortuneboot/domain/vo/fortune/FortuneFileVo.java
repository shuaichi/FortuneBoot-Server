package com.fortuneboot.domain.vo.fortune;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.fortuneboot.domain.entity.fortune.FortuneFileEntity;
import lombok.Data;

/**
 * 文件
 *
 * @Author work.chi.zhang@gmail.com
 * @Date 2025/2/27 22:03
 **/
@Data
public class FortuneFileVo {
    public FortuneFileVo(FortuneFileEntity entity) {
        if (ObjectUtil.isNotEmpty(entity)) {
            BeanUtil.copyProperties(entity, this);
        }
    }

    /**
     * id
     */
    private Long fileId;

    /**
     * 文件类型
     */
    private String contentType;

    /**
     * 数据
     */
    private byte[] fileData;

    /**
     * 大小
     */
    private Long size;

    /**
     * 原始名称
     */
    private String originalName;

    /**
     * 账单id
     */
    private Long billId;
}
