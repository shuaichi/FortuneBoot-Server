package com.fortuneboot.service.system;

import com.fortuneboot.common.core.page.PageDTO;
import com.fortuneboot.domain.common.command.BulkOperationCommand;
import com.fortuneboot.domain.command.system.NoticeAddCommand;
import com.fortuneboot.domain.command.system.NoticeUpdateCommand;
import com.fortuneboot.domain.dto.NoticeDTO;
import com.fortuneboot.factory.system.model.NoticeModel;
import com.fortuneboot.factory.system.NoticeModelFactory;
import com.fortuneboot.domain.query.system.NoticeQuery;
import com.fortuneboot.domain.entity.system.SysNoticeEntity;
import com.fortuneboot.repository.system.SysNoticeRepository;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.util.List;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * @author valarchie
 */
@Service
@RequiredArgsConstructor
public class NoticeApplicationService {

    private final SysNoticeRepository noticeRepository;

    private final NoticeModelFactory noticeModelFactory;

    public PageDTO<NoticeDTO> getNoticeList(NoticeQuery query) {
        Page<SysNoticeEntity> page = noticeRepository.getNoticeList(query.toPage(), query.toQueryWrapper());
        List<NoticeDTO> records = page.getRecords().stream().map(NoticeDTO::new).collect(Collectors.toList());
        return new PageDTO<>(records, page.getTotal());
    }


    public NoticeDTO getNoticeInfo(Long id) {
        NoticeModel noticeModel = noticeModelFactory.loadById(id);
        return new NoticeDTO(noticeModel);
    }


    public void addNotice(NoticeAddCommand addCommand) {
        NoticeModel noticeModel = noticeModelFactory.create();
        noticeModel.loadAddCommand(addCommand);

        noticeModel.checkFields();

        noticeModel.insert();
    }


    public void updateNotice(NoticeUpdateCommand updateCommand) {
        NoticeModel noticeModel = noticeModelFactory.loadById(updateCommand.getNoticeId());
        noticeModel.loadUpdateCommand(updateCommand);

        noticeModel.checkFields();

        noticeModel.updateById();
    }

    public void deleteNotice(BulkOperationCommand<Integer> deleteCommand) {
        noticeRepository.removeBatchByIds(deleteCommand.getIds());
    }




}
