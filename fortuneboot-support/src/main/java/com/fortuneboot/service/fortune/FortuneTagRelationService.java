package com.fortuneboot.service.fortune;

import cn.hutool.core.map.MapUtil;
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
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

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
        fortuneTagFactory.loadById(addCommand.getTagId());
        fortuneTagRelationModel.insert();
    }

    @Transactional(rollbackFor = Exception.class)
    public void batchAdd(List<FortuneTagRelationAddCommand> commands) {
        // mybatis-plus 的saveBatch底层是for循环一条一条插入的，故这里直接调用 add 方法也一样.
        commands.forEach(this::add);
    }
}
