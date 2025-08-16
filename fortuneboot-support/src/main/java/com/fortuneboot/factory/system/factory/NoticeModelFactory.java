package com.fortuneboot.factory.system.factory;

import com.fortuneboot.common.exception.ApiException;
import com.fortuneboot.common.exception.error.ErrorCode;
import com.fortuneboot.domain.entity.system.SysNoticeEntity;
import com.fortuneboot.repository.system.SysNoticeRepository;
import com.fortuneboot.factory.system.model.NoticeModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 公告模型工厂
 * @author valarchie
 */
@Component
@RequiredArgsConstructor
public class NoticeModelFactory {

    private final SysNoticeRepository noticeRepository;

    public NoticeModel loadById(Long noticeId) {
        SysNoticeEntity byId = noticeRepository.getById(noticeId);

        if (byId == null) {
            throw new ApiException(ErrorCode.Business.COMMON_OBJECT_NOT_FOUND, noticeId, "通知公告");
        }

        return new NoticeModel(byId);
    }

    public NoticeModel create() {
        return new NoticeModel();
    }


}
