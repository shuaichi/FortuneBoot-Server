package com.fortuneboot.service.fortune;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fortuneboot.common.exception.ApiException;
import com.fortuneboot.common.exception.error.ErrorCode;
import com.fortuneboot.domain.command.fortune.FortuneFinanceOrderAddCommand;
import com.fortuneboot.domain.command.fortune.FortuneFinanceOrderModifyCommand;
import com.fortuneboot.domain.command.fortune.order.FinanceOrderCreateCommand;
import com.fortuneboot.domain.command.fortune.order.FinanceOrderMetadataCommand;
import com.fortuneboot.domain.command.fortune.order.FinanceOrderTransitionCommand;
import com.fortuneboot.domain.entity.fortune.FortuneFinanceOrderEntity;
import com.fortuneboot.domain.query.fortune.FortuneFinanceOrderQuery;
import com.fortuneboot.domain.rule.fortune.order.FinanceOrderFieldRule;
import com.fortuneboot.repository.fortune.FortuneFinanceOrderRepo;
import com.fortuneboot.service.fortune.order.FinanceOrderRequestService;
import org.apache.commons.lang3.StringUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.UUID;
import java.util.List;

/**
 * 单据
 *
 * @author work.chi.zhang@gmail.com
 * @date 2025/8/16 18:19
 **/
@Service
@AllArgsConstructor
public class FortuneFinanceOrderService {

    private final FinanceOrderRequestService financeOrderRequestService;

    private final FortuneFinanceOrderRepo fortuneFinanceOrderRepo;

    public void add(FortuneFinanceOrderAddCommand command) {
        FinanceOrderCreateCommand createCommand = new FinanceOrderCreateCommand();
        createCommand.setBookId(command.getBookId());
        createCommand.setRequestId(UUID.randomUUID().toString());
        createCommand.setTitle(command.getTitle());
        createCommand.setType(command.getType());
        createCommand.setCounterpartyName(FinanceOrderFieldRule.DEFAULT_COUNTERPARTY_NAME);
        createCommand.setRemark(command.getRemark());
        financeOrderRequestService.create(createCommand);
    }

    public void modify(FortuneFinanceOrderModifyCommand command) {
        // 先加载旧单据并校验归属，再组装元数据命令；禁止先覆盖bookId导致校验失效
        FortuneFinanceOrderEntity order = loadOrder(command.getOrderId());
        if (!Objects.equals(order.getBookId(), command.getBookId())) {
            throw new ApiException(ErrorCode.Business.ORDER_BOOK_NOT_MATCH);
        }
        if (command.getType() == null || !Objects.equals(order.getType(), command.getType())) {
            throw new ApiException(ErrorCode.Business.ORDER_TYPE_MISMATCH);
        }
        FinanceOrderMetadataCommand metadataCommand = new FinanceOrderMetadataCommand();
        metadataCommand.setBookId(order.getBookId());
        metadataCommand.setRequestId(UUID.randomUUID().toString());
        metadataCommand.setOrderId(order.getOrderId());
        metadataCommand.setExpectedVersion(order.getVersion());
        metadataCommand.setTitle(command.getTitle());
        metadataCommand.setType(order.getType());
        metadataCommand.setCounterpartyId(order.getCounterpartyId());
        metadataCommand.setCounterpartyName(StringUtils.defaultIfBlank(order.getCounterpartyName(),
                FinanceOrderFieldRule.DEFAULT_COUNTERPARTY_NAME));
        metadataCommand.setDueDate(order.getDueDate());
        metadataCommand.setRemark(command.getRemark() == null ? order.getRemark() : command.getRemark());
        financeOrderRequestService.modify(metadataCommand);
    }

    public void using(Long bookId, Long orderId) {
        financeOrderRequestService.legacyUsing(bookId, orderId);
    }

    public void close(Long bookId, Long orderId) {
        transition(bookId, orderId, FinanceOrderTransitionCommand.Action.CLOSE);
    }

    public void remove(Long bookId, Long orderId) {
        transition(bookId, orderId, FinanceOrderTransitionCommand.Action.REMOVE);
    }

    public IPage<FortuneFinanceOrderEntity> getPage(FortuneFinanceOrderQuery query) {
        return fortuneFinanceOrderRepo.page(query.toPage(), query.addQueryCondition());
    }

    public void reopen(Long bookId, Long orderId) {
        transition(bookId, orderId, FinanceOrderTransitionCommand.Action.REOPEN);
    }

    public List<FortuneFinanceOrderEntity> getUsingFinanceOrderList(Long bookId) {
        return fortuneFinanceOrderRepo.getUsingFinanceOrderList(bookId);
    }

    private void transition(Long bookId, Long orderId, FinanceOrderTransitionCommand.Action action) {
        FortuneFinanceOrderEntity order = loadOrder(orderId);
        if (!Objects.equals(order.getBookId(), bookId)) {
            throw new ApiException(ErrorCode.Business.ORDER_BOOK_NOT_MATCH);
        }
        FinanceOrderTransitionCommand command = new FinanceOrderTransitionCommand();
        command.setBookId(bookId);
        command.setOrderId(orderId);
        command.setAction(action);
        command.setRequestId(UUID.randomUUID().toString());
        command.setExpectedVersion(order.getVersion());
        financeOrderRequestService.transition(command);
    }

    private FortuneFinanceOrderEntity loadOrder(Long orderId) {
        FortuneFinanceOrderEntity order = fortuneFinanceOrderRepo.getById(orderId);
        if (order == null) {
            throw new ApiException(ErrorCode.Business.COMMON_OBJECT_NOT_FOUND, orderId, "单据");
        }
        return order;
    }
}
