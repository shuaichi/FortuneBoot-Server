package com.fortuneboot.service.fortune;

import com.fortuneboot.domain.command.fortune.FortuneMemberRelationAddCommand;
import com.fortuneboot.domain.entity.fortune.FortuneMemberRelationEntity;
import com.fortuneboot.factory.fortune.factory.FortuneMemberFactory;
import com.fortuneboot.factory.fortune.factory.FortuneMemberRelationFactory;
import com.fortuneboot.factory.fortune.model.FortuneMemberRelationModel;
import com.fortuneboot.repository.fortune.FortuneMemberRelationRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 *
 * @author zhangchi118
 * @date 2026/3/19 09:34
 **/
@Service
@RequiredArgsConstructor
public class FortuneMemberRelationService {
    private final FortuneMemberRelationFactory fortuneMemberRelationFactory;
    private final FortuneMemberFactory fortuneMemberFactory;
    private final FortuneMemberRelationRepo fortuneMemberRelationRepo;

    @Transactional(rollbackFor = Exception.class)
    public void batchAdd(List<FortuneMemberRelationAddCommand> commands) {
        List<FortuneMemberRelationEntity> saveList = commands.stream().map(command -> {
            FortuneMemberRelationModel model = fortuneMemberRelationFactory.create();
            model.loadAddCommand(command);
            fortuneMemberFactory.loadById(command.getMemberId()); // 校验存在
            return (FortuneMemberRelationEntity) model;
        }).toList();
        fortuneMemberRelationRepo.saveBatch(saveList);
    }
}
