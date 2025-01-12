package com.fortuneboot.service.fortune;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fortuneboot.domain.command.fortune.FortuneAccountAddCommand;
import com.fortuneboot.domain.entity.fortune.FortuneAccountEntity;
import com.fortuneboot.domain.query.fortune.FortuneAccountQuery;
import com.fortuneboot.factory.fortune.FortuneAccountFactory;
import com.fortuneboot.factory.fortune.model.FortuneAccountModel;
import com.fortuneboot.repository.fortune.FortuneAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @Author work.chi.zhang@gmail.com
 * @Date 2025/1/10 22:48
 **/
@Slf4j
@Service
@RequiredArgsConstructor
public class FortuneAccountService {

    private final FortuneAccountRepository fortuneAccountRepository;

    private final FortuneAccountFactory fortuneAccountFactory;

    public IPage<FortuneAccountEntity> getPage(FortuneAccountQuery query) {
        return fortuneAccountRepository.page(query.toPage(), query.addQueryCondition());
    }

    public void add(FortuneAccountAddCommand addCommand) {
        FortuneAccountModel fortuneAccountModel = fortuneAccountFactory.create();
        fortuneAccountModel.loadAddCommand(addCommand);
        fortuneAccountModel.checkAccountType();
        fortuneAccountModel.insert();
    }
}
