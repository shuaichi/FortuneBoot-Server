package com.fortuneboot.service.fortune;

import com.fortuneboot.domain.command.fortune.FortuneTagRelationAddCommand;
import com.fortuneboot.factory.fortune.factory.FortuneTagFactory;
import com.fortuneboot.factory.fortune.factory.FortuneTagRelationFactory;
import com.fortuneboot.factory.fortune.model.FortuneTagRelationModel;
import com.fortuneboot.repository.fortune.FortuneTagRelationRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    private final FortuneTagRelationRepo fortuneTagRelationRepo;

    public void add(FortuneTagRelationAddCommand addCommand) {
        FortuneTagRelationModel fortuneTagRelationModel = fortuneTagRelationFactory.create();
        fortuneTagRelationModel.loadAddCommand(addCommand);
        fortuneTagFactory.loadById(addCommand.getTagId());
        fortuneTagRelationModel.insert();
    }

    @Transactional(rollbackFor = Exception.class)
    public void batchAdd(List<FortuneTagRelationAddCommand> commands) {
        // mybatis-plus 的saveBatch底层是for循环一条一条插入的，故这里直接调用 add 方法也一样.
        commands.forEach(this::add);
    }
}
