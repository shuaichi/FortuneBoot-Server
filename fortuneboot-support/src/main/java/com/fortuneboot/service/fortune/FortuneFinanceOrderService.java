package com.fortuneboot.service.fortune;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fortuneboot.common.enums.fortune.FinanceOrderStatusEnum;
import com.fortuneboot.domain.command.fortune.FortuneFinanceOrderAddCommand;
import com.fortuneboot.domain.command.fortune.FortuneFinanceOrderModifyCommand;
import com.fortuneboot.domain.entity.fortune.FortuneFinanceOrderEntity;
import com.fortuneboot.domain.query.fortune.FortuneFinanceOrderQuery;
import com.fortuneboot.factory.fortune.factory.FortuneFinanceOrderFactory;
import com.fortuneboot.factory.fortune.model.FortuneFinanceOrderModel;
import com.fortuneboot.repository.fortune.FortuneBillRepo;
import com.fortuneboot.repository.fortune.FortuneFinanceOrderRepo;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 单据
 *
 * @author work.chi.zhang@gmail.com
 * @date 2025/8/16 18:19
 **/
@Service
@AllArgsConstructor
public class FortuneFinanceOrderService {

    private final FortuneFinanceOrderFactory fortuneFinanceOrderFactory;

    private final FortuneFinanceOrderRepo fortuneFinanceOrderRepo;

    private final FortuneBillRepo fortuneBillRepo;

    public void add(FortuneFinanceOrderAddCommand command) {
        FortuneFinanceOrderModel model = fortuneFinanceOrderFactory.create();
        model.loadAddCommand(command);
        model.insert();
    }

    public void modify(FortuneFinanceOrderModifyCommand command) {
        FortuneFinanceOrderModel model = fortuneFinanceOrderFactory.loadById(command.getOrderId());
        model.loadModifyCommand(command);
        model.checkModifyStatus();
        model.checkBookId(command.getBookId());
        model.updateById();
    }

    public void using(Long bookId, Long orderId) {
        FortuneFinanceOrderModel model = fortuneFinanceOrderFactory.loadById(orderId);
        model.checkBookId(bookId);
        model.checkUsingStatus();
        model.setStatus(FinanceOrderStatusEnum.USING.getValue());
        model.updateById();
    }

    public void close(Long bookId, Long orderId) {
        FortuneFinanceOrderModel model = fortuneFinanceOrderFactory.loadById(orderId);
        model.checkBookId(bookId);
        model.checkCloseStatus();
        model.setCloseTime(LocalDateTime.now());
        model.setStatus(FinanceOrderStatusEnum.CLOSE.getValue());
        model.updateById();
    }

    public void remove(Long bookId, Long orderId) {
        FortuneFinanceOrderModel model = fortuneFinanceOrderFactory.loadById(orderId);
        Boolean existed = fortuneBillRepo.existByOrderId(orderId);
        model.checkBillExist(existed);
        model.checkBookId(bookId);
        model.deleteById();
    }

    public IPage<FortuneFinanceOrderEntity> getPage(FortuneFinanceOrderQuery query) {
        return fortuneFinanceOrderRepo.page(query.toPage(), query.addQueryCondition());
    }

    public void reopen(Long bookId, Long orderId) {
        FortuneFinanceOrderModel model = fortuneFinanceOrderFactory.loadById(orderId);
        model.checkBookId(bookId);
        model.checkReopenStatus();
        model.setStatus(FinanceOrderStatusEnum.USING.getValue());
        model.updateById();
    }
}
