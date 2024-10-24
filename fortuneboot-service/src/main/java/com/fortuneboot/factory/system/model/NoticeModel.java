package com.fortuneboot.factory.system.model;

import cn.hutool.core.bean.BeanUtil;
import com.fortuneboot.common.enums.common.NoticeTypeEnum;
import com.fortuneboot.common.enums.common.StatusEnum;
import com.fortuneboot.common.enums.BasicEnumUtil;
import com.fortuneboot.domain.command.system.NoticeAddCommand;
import com.fortuneboot.domain.command.system.NoticeUpdateCommand;
import com.fortuneboot.domain.entity.system.SysNoticeEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @author valarchie
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class NoticeModel extends SysNoticeEntity {

    public NoticeModel(SysNoticeEntity entity) {
        if (entity != null) {
            BeanUtil.copyProperties(entity, this);
        }
    }

    public void loadAddCommand(NoticeAddCommand command) {
        if (command != null) {
            BeanUtil.copyProperties(command, this, "noticeId");
        }
    }

    public void loadUpdateCommand(NoticeUpdateCommand command) {
        if (command != null) {
            loadAddCommand(command);
        }
    }

    public void checkFields() {
        BasicEnumUtil.fromValue(NoticeTypeEnum.class, getNoticeType());
        BasicEnumUtil.fromValue(StatusEnum.class, getStatus());
    }

}
