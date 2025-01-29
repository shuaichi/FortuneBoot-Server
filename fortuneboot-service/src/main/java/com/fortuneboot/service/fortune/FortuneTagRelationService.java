package com.fortuneboot.service.fortune;

import com.fortuneboot.domain.command.fortune.FortuneTagRelationAddCommand;
import com.fortuneboot.factory.fortune.FortuneBillFactory;
import com.fortuneboot.factory.fortune.FortuneTagFactory;
import com.fortuneboot.factory.fortune.FortuneTagRelationFactory;
import com.fortuneboot.factory.fortune.model.FortuneBillModel;
import com.fortuneboot.factory.fortune.model.FortuneTagModel;
import com.fortuneboot.factory.fortune.model.FortuneTagRelationModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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

    public void add(FortuneTagRelationAddCommand addCommand) {
        FortuneTagRelationModel fortuneTagRelationModel = fortuneTagRelationFactory.create();
        fortuneTagRelationModel.loadAddCommand(addCommand);
        FortuneTagModel fortuneTagModel = fortuneTagFactory.loadById(addCommand.getTagId());
        fortuneTagRelationModel.checkTagIdExist(fortuneTagModel);
        fortuneTagRelationModel.insert();
    }
}
