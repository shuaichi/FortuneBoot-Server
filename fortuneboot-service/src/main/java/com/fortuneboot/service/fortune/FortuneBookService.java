package com.fortuneboot.service.fortune;

import com.fortuneboot.domain.command.fortune.FortuneBookAddCommand;
import com.fortuneboot.factory.fortune.FortuneBookFactory;
import com.fortuneboot.factory.fortune.model.FortuneBookModel;
import com.fortuneboot.repository.fortune.FortuneBookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 账本service
 *
 * @author zhangchi118
 * @date 2024/11/29 15:50
 **/
@Slf4j
@Service
@RequiredArgsConstructor
public class FortuneBookService {

    private final FortuneBookFactory fortuneBookFactory;

    private final FortuneBookRepository fortuneBookRepository;

    public void addFortuneBook(FortuneBookAddCommand bookAddCommand) {
        FortuneBookModel fortuneBookModel = fortuneBookFactory.create();
        fortuneBookModel.loadAddCommand(bookAddCommand);
        fortuneBookModel.insert();
    }
}
