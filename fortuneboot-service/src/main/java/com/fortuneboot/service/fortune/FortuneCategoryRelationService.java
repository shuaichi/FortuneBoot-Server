package com.fortuneboot.service.fortune;

import com.fortuneboot.domain.command.fortune.FortuneCategoryRelationAddCommand;
import com.fortuneboot.domain.entity.fortune.FortuneCategoryRelationEntity;
import com.fortuneboot.factory.fortune.FortuneCategoryFactory;
import com.fortuneboot.factory.fortune.FortuneCategoryRelationFactory;
import com.fortuneboot.factory.fortune.model.FortuneCategoryModel;
import com.fortuneboot.factory.fortune.model.FortuneCategoryRelationModel;
import com.fortuneboot.repository.fortune.FortuneCategoryRelationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 分类账单关系
 *
 * @author zhangchi118
 * @date 2025/1/29 23:33
 **/
@Slf4j
@Service
@RequiredArgsConstructor
public class FortuneCategoryRelationService {

    private final FortuneCategoryRelationRepository fortuneCategoryRelationRepository;

    private final FortuneCategoryRelationFactory fortuneCategoryRelationFactory;

    private final FortuneCategoryFactory fortuneCategoryFactory;


    public void add(FortuneCategoryRelationAddCommand addCommand) {
        FortuneCategoryRelationModel fortuneCategoryRelationModel = fortuneCategoryRelationFactory.create();
        fortuneCategoryRelationModel.loadAddCommand(addCommand);
        FortuneCategoryModel fortuneCategoryModel = fortuneCategoryFactory.loadById(addCommand.getCategoryId());
        fortuneCategoryRelationModel.checkCategoryExist(fortuneCategoryModel);
        fortuneCategoryModel.insert();
    }

    public void removeByBillId(Long billId) {
        List<FortuneCategoryRelationEntity> list = fortuneCategoryRelationRepository.getByBillId(billId);
        List<Long> ids = list.stream().map(FortuneCategoryRelationEntity::getCategoryId).toList();
        fortuneCategoryRelationRepository.removeBatchByIds(ids);
    }
}
