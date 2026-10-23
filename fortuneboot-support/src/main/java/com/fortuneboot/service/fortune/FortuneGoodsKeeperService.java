package com.fortuneboot.service.fortune;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fortuneboot.common.core.page.PageDTO;
import com.fortuneboot.common.enums.common.DeleteEnum;
import com.fortuneboot.domain.command.fortune.FortuneGoodsKeeperAddCommand;
import com.fortuneboot.domain.command.fortune.FortuneGoodsKeeperModifyCommand;
import com.fortuneboot.domain.entity.fortune.FortuneCategoryEntity;
import com.fortuneboot.domain.entity.fortune.FortuneGoodsKeeperEntity;
import com.fortuneboot.domain.entity.fortune.FortuneTagEntity;
import com.fortuneboot.domain.query.fortune.FortuneGoodsKeeperQuery;
import com.fortuneboot.domain.vo.fortune.FortuneGoodsKeeperOptionVo;
import com.fortuneboot.domain.vo.fortune.FortuneGoodsKeeperStatisticsVo;
import com.fortuneboot.domain.vo.fortune.FortuneGoodsKeeperVo;
import com.fortuneboot.factory.fortune.factory.FortuneGoodsKeeperFactory;
import com.fortuneboot.factory.fortune.model.FortuneGoodsKeeperModel;
import com.fortuneboot.repository.fortune.FortuneCategoryRepo;
import com.fortuneboot.repository.fortune.FortuneGoodsKeeperRepo;
import com.fortuneboot.repository.fortune.FortuneTagRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 归物
 *
 * @author zhangchi118
 * @date 2025/5/6 10:58
 **/
@Slf4j
@Service
@RequiredArgsConstructor
public class FortuneGoodsKeeperService {

    private final FortuneGoodsKeeperFactory fortuneGoodsKeeperFactory;

    private final FortuneGoodsKeeperRepo fortuneGoodsKeeperRepo;

    private final FortuneCategoryRepo fortuneCategoryRepo;

    private final FortuneTagRepo fortuneTagRepo;

    public PageDTO<FortuneGoodsKeeperVo> getPage(FortuneGoodsKeeperQuery query) {
        IPage<FortuneGoodsKeeperEntity> page = fortuneGoodsKeeperRepo.page(query.toPage(), query.addQueryCondition());
        List<FortuneGoodsKeeperVo> records = page.getRecords().stream().map(FortuneGoodsKeeperVo::new).toList();
        this.fillCategory(records);
        this.fillTag(records);
        return new PageDTO<>(records, page.getTotal());
    }

    private void fillCategory(List<FortuneGoodsKeeperVo> dataList) {
        List<Long> idList = dataList.stream().map(FortuneGoodsKeeperVo::getCategoryId).toList();
        if (CollectionUtils.isEmpty(idList)) {
            return;
        }
        List<FortuneCategoryEntity> categoryEntityList = fortuneCategoryRepo.getByIds(idList);
        Map<Long, String> map = categoryEntityList.stream()
                .collect(Collectors.toMap(FortuneCategoryEntity::getCategoryId, FortuneCategoryEntity::getCategoryName));
        dataList.forEach(data -> data.setCategoryName(map.get(data.getCategoryId())));
    }

    private void fillTag(List<FortuneGoodsKeeperVo> dataList) {
        List<Long> idList = dataList.stream().map(FortuneGoodsKeeperVo::getTagId).toList();
        if (CollectionUtils.isEmpty(idList)) {
            return;
        }
        List<FortuneTagEntity> tagEntityList = fortuneTagRepo.getByIds(idList);
        Map<Long, String> map = tagEntityList.stream()
                .collect(Collectors.toMap(FortuneTagEntity::getTagId, FortuneTagEntity::getTagName));
        dataList.forEach(data -> data.setTagName(map.get(data.getTagId())));
    }

    public void add(FortuneGoodsKeeperAddCommand addCommand) {
        FortuneGoodsKeeperModel model = fortuneGoodsKeeperFactory.create();
        model.checkStatus(addCommand.getStatus());
        model.checkRetiredDate(addCommand.getStatus(), addCommand.getRetiredDate());
        model.loadAddCommand(addCommand);
        model.insert();
    }

    public void modify(FortuneGoodsKeeperModifyCommand modifyCommand) {
        FortuneGoodsKeeperModel model = fortuneGoodsKeeperFactory.loadById(modifyCommand.getGoodsKeeperId());
        model.checkBookId(modifyCommand.getBookId());
        model.checkStatus(modifyCommand.getStatus());
        model.checkRetiredDate(modifyCommand.getStatus(), modifyCommand.getRetiredDate());
        model.loadModifyCommand(modifyCommand);
        model.updateById();
    }

    public void remove(Long bookId, Long goodsKeeperId) {
        FortuneGoodsKeeperModel model = fortuneGoodsKeeperFactory.loadById(goodsKeeperId);
        model.checkBookId(bookId);
        model.deleteById();
    }

    public FortuneGoodsKeeperStatisticsVo getGoodsKeeperStatistics(Long bookId) {
        return fortuneGoodsKeeperRepo.getGoodsKeeperStatistics(bookId);
    }

    /**
     * 重新计算并写回某个归物的「关联账单累计净成本」。
     * 支出账单(amount, bill_type=1)累加，收入账单(amount, bill_type=2)冲减。
     * 采用「从源重算」而非增量加减，避免修改/删除场景下的漂移。
     *
     * @param goodsKeeperId 归物id，为空时直接返回
     */
    public void recalculateAccumulatedCost(Long goodsKeeperId) {
        if (Objects.isNull(goodsKeeperId)) {
            return;
        }
        BigDecimal accumulatedCost = fortuneGoodsKeeperRepo.sumLinkedBillAmount(goodsKeeperId);
        FortuneGoodsKeeperModel model = fortuneGoodsKeeperFactory.loadById(goodsKeeperId);
        model.setAccumulatedCost(accumulatedCost);
        model.updateById();
    }

    /**
     * 查询某账本下的归物下拉选项（id + 名称 + 价格 + 累计成本）。
     *
     * @param bookId 账本id
     * @return 归物选项列表
     */
    public List<FortuneGoodsKeeperOptionVo> getOptions(Long bookId) {
        List<FortuneGoodsKeeperEntity> list = fortuneGoodsKeeperRepo.lambdaQuery()
                .eq(FortuneGoodsKeeperEntity::getBookId, bookId)
                .eq(FortuneGoodsKeeperEntity::getDeleted, DeleteEnum.VALID.getValue())
                .orderByAsc(FortuneGoodsKeeperEntity::getGoodsName)
                .list();
        return list.stream().map(FortuneGoodsKeeperOptionVo::new).toList();
    }
}
