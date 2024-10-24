package com.fortuneboot.domain.dto;

//import com.fortuneboot.domain.common.cache.CacheCenter;
import com.fortuneboot.domain.entity.system.SysNoticeEntity;

import java.util.Date;

import com.fortuneboot.domain.entity.system.SysUserEntity;
import com.fortuneboot.domain.registry.CacheRegistry;
import lombok.Data;

/**
 * @author valarchie
 */
@Data
public class NoticeDTO {

    public NoticeDTO(SysNoticeEntity entity) {
        if (entity != null) {
            this.noticeId = entity.getNoticeId() + "";
            this.noticeTitle = entity.getNoticeTitle();
            this.noticeType = entity.getNoticeType();
            this.noticeContent = entity.getNoticeContent();
            this.status = entity.getStatus();
            this.createTime = entity.getCreateTime();

            SysUserEntity cacheUser = CacheRegistry.getUserById(entity.getCreatorId());
            if (cacheUser != null) {
                //创建者
                this.creatorName = cacheUser.getUsername();
            }
        }
    }

    private String noticeId;

    private String noticeTitle;

    private Integer noticeType;

    private String noticeContent;

    private Integer status;

    private Date createTime;

    private String creatorName;

}
