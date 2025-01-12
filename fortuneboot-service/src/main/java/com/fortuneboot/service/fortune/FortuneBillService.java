package com.fortuneboot.service.fortune;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fortuneboot.domain.command.fortune.FortuneBillAddCommand;
import com.fortuneboot.domain.entity.fortune.FortuneBillEntity;
import com.fortuneboot.domain.query.fortune.FortuneBillQuery;
import com.fortuneboot.factory.fortune.FortuneAccountFactory;
import com.fortuneboot.factory.fortune.FortuneBillFactory;
import com.fortuneboot.factory.fortune.FortuneTagFactory;
import com.fortuneboot.factory.fortune.model.FortuneAccountModel;
import com.fortuneboot.factory.fortune.model.FortuneBillModel;
import com.fortuneboot.repository.fortune.FortuneAccountRepository;
import com.fortuneboot.repository.fortune.FortuneBillRepository;
import com.fortuneboot.repository.fortune.FortuneTagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * @Author work.chi.zhang@gmail.com
 * @Date 2025/1/12 22:42
 **/
@Slf4j
@Service
@RequiredArgsConstructor
public class FortuneBillService {

    private final FortuneBillRepository fortuneBillRepository;

    private final FortuneBillFactory fortuneBillFactory;

    private final FortuneAccountRepository fortuneAccountRepository;

    private final FortuneAccountFactory fortuneAccountFactory;

    private final FortuneTagRepository fortuneTagRepository;

    private final FortuneTagFactory fortuneTagFactory;

    public IPage<FortuneBillEntity> getPage( FortuneBillQuery query) {
        return fortuneBillRepository.page(query.toPage(),query.addQueryCondition());
    }

    @Transactional(rollbackFor = Exception.class)
    public void add(FortuneBillAddCommand addCommand) {
        FortuneBillModel fortuneBillModel = fortuneBillFactory.create();
        fortuneBillModel.loadAddCommand(addCommand);
        fortuneBillModel.checkBookId(fortuneBillModel.getBookId());
        if (Objects.nonNull(addCommand.getAccountId())){
            FortuneAccountModel fortuneAccountModel = fortuneAccountFactory.loadById(addCommand.getAccountId());
            fortuneAccountModel.setBalance(fortuneAccountModel.getBalance().subtract(addCommand.getAmount()));
            fortuneAccountModel.updateById();
        }
        if (CollectionUtils.isEmpty(addCommand.getTagList())){

        }
        fortuneBillModel.insert();
    }
}
