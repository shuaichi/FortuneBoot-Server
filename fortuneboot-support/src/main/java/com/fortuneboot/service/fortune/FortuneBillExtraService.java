package com.fortuneboot.service.fortune;

import com.fortuneboot.domain.command.fortune.FortuneBillExtraAddCommand;
import com.fortuneboot.domain.entity.fortune.FortuneBillExtraEntity;
import com.fortuneboot.factory.fortune.factory.FortuneBillExtraFactory;
import com.fortuneboot.factory.fortune.model.FortuneBillExtraModel;
import com.fortuneboot.repository.fortune.FortuneBillExtraRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * 账单附加费用（手续费/优惠）服务
 *
 * @author zhangchi118
 **/
@Slf4j
@Service
@RequiredArgsConstructor
public class FortuneBillExtraService {

    private final FortuneBillExtraRepo fortuneBillExtraRepo;

    private final FortuneBillExtraFactory fortuneBillExtraFactory;

    /**
     * 批量新增账单附加费用
     *
     * @param billId   账单id，用于回填 command.billId
     * @param commands 新增命令列表
     */
    @Transactional(rollbackFor = Exception.class)
    public void batchAdd(Long billId, List<FortuneBillExtraAddCommand> commands) {
        if (CollectionUtils.isEmpty(commands)) {
            return;
        }
        List<FortuneBillExtraEntity> entityList = commands.stream().map(command -> {
            FortuneBillExtraModel model = fortuneBillExtraFactory.create();
            model.loadAddCommand(command);
            if (Objects.nonNull(billId)) {
                model.setBillId(billId);
            }
            return (FortuneBillExtraEntity) model;
        }).toList();
        fortuneBillExtraRepo.saveBatch(entityList, 1000);
    }

    /**
     * 根据账单id物理删除附加费用
     *
     * @param billId 账单id
     */
    public void phyRemoveByBillId(Long billId) {
        if (Objects.isNull(billId)) {
            return;
        }
        fortuneBillExtraRepo.phyRemoveByBillId(billId);
    }
}