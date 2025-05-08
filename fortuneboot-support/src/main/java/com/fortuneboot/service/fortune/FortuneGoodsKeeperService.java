package com.fortuneboot.service.fortune;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fortuneboot.common.core.page.PageDTO;
import com.fortuneboot.domain.command.fortune.FortuneGoodsKeeperAddCommand;
import com.fortuneboot.domain.command.fortune.FortuneGoodsKeeperModifyCommand;
import com.fortuneboot.domain.entity.fortune.FortuneCategoryEntity;
import com.fortuneboot.domain.entity.fortune.FortuneGoodsKeeperEntity;
import com.fortuneboot.domain.entity.fortune.FortuneTagEntity;
import com.fortuneboot.domain.query.fortune.FortuneGoodsKeeperQuery;
import com.fortuneboot.domain.vo.fortune.FortuneGoodsKeeperVo;
import com.fortuneboot.factory.fortune.FortuneGoodsKeeperFactory;
import com.fortuneboot.factory.fortune.model.FortuneGoodsKeeperModel;
import com.fortuneboot.repository.fortune.FortuneCategoryRepository;
import com.fortuneboot.repository.fortune.FortuneGoodsKeeperRepository;
import com.fortuneboot.repository.fortune.FortuneTagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
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

    private final FortuneGoodsKeeperRepository fortuneGoodsKeeperRepository;

    private final FortuneCategoryRepository fortuneCategoryRepository;

    private final FortuneTagRepository fortuneTagRepository;

    public PageDTO<FortuneGoodsKeeperVo> getPage(FortuneGoodsKeeperQuery query) {
        IPage<FortuneGoodsKeeperEntity> page = fortuneGoodsKeeperRepository.page(query.toPage(), query.addQueryCondition());
        List<FortuneGoodsKeeperVo> records = page.getRecords().stream().map(FortuneGoodsKeeperVo::new).toList();
        this.fillCategory(records);
        this.fillTag(records);
        return new PageDTO<>(records, page.getTotal());
    }

    private void fillCategory(List<FortuneGoodsKeeperVo> dataList){
        List<Long> idList = dataList.stream().map(FortuneGoodsKeeperVo::getCategoryId).toList();
        List<FortuneCategoryEntity> categoryEntityList = fortuneCategoryRepository.getByIds(idList);
        Map<Long, String> map = categoryEntityList.stream()
                .collect(Collectors.toMap(FortuneCategoryEntity::getCategoryId, FortuneCategoryEntity::getCategoryName));
        dataList.forEach(data->{
            data.setCategoryName(map.get(data.getCategoryId()));
        });
    }

    private void fillTag(List<FortuneGoodsKeeperVo> dataList){
        List<Long> idList = dataList.stream().map(FortuneGoodsKeeperVo::getTagId).toList();
        List<FortuneTagEntity> tagEntityList = fortuneTagRepository.getByIds(idList);
        Map<Long, String> map = tagEntityList.stream()
                .collect(Collectors.toMap(FortuneTagEntity::getTagId, FortuneTagEntity::getTagName));
        dataList.forEach(data->{
            data.setTagName(map.get(data.getTagId()));
        });
    }

    public void add(FortuneGoodsKeeperAddCommand addCommand) {
        FortuneGoodsKeeperModel model = fortuneGoodsKeeperFactory.create();
        model.checkStatus(addCommand.getStatus());
        model.loadAddCommand(addCommand);
        model.insert();
    }

    public void modify(FortuneGoodsKeeperModifyCommand modifyCommand) {
        FortuneGoodsKeeperModel model = fortuneGoodsKeeperFactory.loadById(modifyCommand.getGoodsKeeperId());
        model.checkBookId(modifyCommand.getBookId());
        model.checkStatus(modifyCommand.getStatus());
        model.loadModifyCommand(modifyCommand);
        model.updateById();
    }

    public void remove(Long bookId, Long goodsKeeperId) {
        FortuneGoodsKeeperModel model = fortuneGoodsKeeperFactory.loadById(goodsKeeperId);
        model.checkBookId(bookId);
        model.deleteById();
    }

}
