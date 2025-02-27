package com.fortuneboot.domain.command.fortune;

import lombok.Data;

/**
 * @author zhangchi118
 * @date 2025/2/27 17:01
 **/
@Data
public class FortuneFileAddCommand {

    /**
     * 文件id
     */
    private Long fileId;

    /**
     * 账单id
     */
    private Long billId;

    /**
     * 媒体类型
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

}
