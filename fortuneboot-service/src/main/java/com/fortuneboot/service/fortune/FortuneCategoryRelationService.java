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

    private final FortuneCategoryRelationRepository fortuneCategoryRelationRepository;

    private final FortuneCategoryRelationFactory fortuneCategoryRelationFactory;

    private final FortuneCategoryFactory fortuneCategoryFactory;


    public void add(FortuneCategoryRelationAddCommand addCommand) {
        FortuneCategoryRelationModel fortuneCategoryRelationModel = fortuneCategoryRelationFactory.create();
        fortuneCategoryRelationModel.loadAddCommand(addCommand);
        // 校验category是否存在，loadById中自带校验
        fortuneCategoryFactory.loadById(addCommand.getCategoryId());
        fortuneCategoryRelationModel.insert();
    }

    public void removeByBillId(Long billId) {
        List<FortuneCategoryRelationEntity> list = fortuneCategoryRelationRepository.getByBillId(billId);
        List<Long> ids = list.stream().map(FortuneCategoryRelationEntity::getCategoryId).toList();
        fortuneCategoryRelationRepository.removeBatchByIds(ids);
    }

    @Transactional(rollbackFor = Exception.class)
    public void batchAdd(List<FortuneCategoryRelationAddCommand> commands) {
        // mybatis-plus 的saveBatch底层是for循环一条一条插入的，故这里直接调用 add 方法也一样.
        commands.forEach(this::add);
    }
}
