package com.fortuneboot.service.fortune;

import com.fortuneboot.domain.command.fortune.FortuneCategoryRelationAddCommand;
import com.fortuneboot.domain.entity.fortune.FortuneCategoryRelationEntity;
import com.fortuneboot.factory.fortune.factory.FortuneCategoryFactory;
import com.fortuneboot.factory.fortune.factory.FortuneCategoryRelationFactory;
import com.fortuneboot.factory.fortune.model.FortuneCategoryRelationModel;
import com.fortuneboot.repository.fortune.FortuneCategoryRelationRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    private final FortuneCategoryRelationRepo fortuneCategoryRelationRepository;

    private final FortuneCategoryRelationFactory fortuneCategoryRelationFactory;

    private final FortuneCategoryFactory fortuneCategoryFactory;


    public void add(FortuneCategoryRelationAddCommand addCommand) {
        FortuneCategoryRelationModel fortuneCategoryRelationModel = fortuneCategoryRelationFactory.create();
        fortuneCategoryRelationModel.loadAddCommand(addCommand);
        // 校验category是否存在，loadById中自带校验
        fortuneCategoryFactory.loadById(addCommand.getCategoryId());
        fortuneCategoryRelationModel.insert();
    }

    @Transactional(rollbackFor = Exception.class)
    public void batchAdd(List<FortuneCategoryRelationAddCommand> commands) {
        List<Long> ids = commands.stream().map(FortuneCategoryRelationAddCommand::getCategoryId).toList();
        // 校验category是否存在，loadByIds中自带校验
        fortuneCategoryFactory.loadByIds(ids);

        List<FortuneCategoryRelationEntity> entityList = commands.stream().map(command -> {
            FortuneCategoryRelationModel fortuneCategoryRelationModel = fortuneCategoryRelationFactory.create();
            fortuneCategoryRelationModel.loadAddCommand(command);
            return (FortuneCategoryRelationEntity) fortuneCategoryRelationModel;
        }).toList();

        fortuneCategoryRelationRepository.saveBatch(entityList,1000);
    }
}
