package com.fortuneboot.service.fortune;

import com.fortuneboot.common.utils.mybatis.WrapperUtil;
import com.fortuneboot.domain.command.fortune.FortuneTagRelationAddCommand;
import com.fortuneboot.domain.entity.fortune.FortuneTagRelationEntity;
import com.fortuneboot.factory.fortune.FortuneBillFactory;
import com.fortuneboot.factory.fortune.FortuneTagFactory;
import com.fortuneboot.factory.fortune.FortuneTagRelationFactory;
import com.fortuneboot.factory.fortune.model.FortuneBillModel;
import com.fortuneboot.factory.fortune.model.FortuneTagModel;
import com.fortuneboot.factory.fortune.model.FortuneTagRelationModel;
import com.fortuneboot.repository.fortune.FortuneTagRelationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author work.chi.zhang@gmail.com
 * @Date 2025/1/13 00:11
 **/
@Slf4j
@Service
@RequiredArgsConstructor
public class FortuneTagRelationService {

    private final FortuneTagRelationFactory fortuneTagRelationFactory;

    private final FortuneTagFactory fortuneTagFactory;

    private final FortuneTagRelationRepository fortuneTagRelationRepository;

    public void add(FortuneTagRelationAddCommand addCommand) {
        FortuneTagRelationModel fortuneTagRelationModel = fortuneTagRelationFactory.create();
        fortuneTagRelationModel.loadAddCommand(addCommand);
        FortuneTagModel fortuneTagModel = fortuneTagFactory.loadById(addCommand.getTagId());
        fortuneTagRelationModel.checkTagIdExist(fortuneTagModel);
        fortuneTagRelationModel.insert();
    }

    public void removeByBillId(Long billId) {
        List<FortuneTagRelationEntity> list = fortuneTagRelationRepository.getByBillId(billId);
        List<Long> ids = list.stream().map(FortuneTagRelationEntity::getTagId).toList();
        fortuneTagRelationRepository.removeBatchByIds(ids);
    }
}
