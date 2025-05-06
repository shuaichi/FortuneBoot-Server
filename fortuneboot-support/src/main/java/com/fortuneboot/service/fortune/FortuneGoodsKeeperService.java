package com.fortuneboot.service.fortune;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fortuneboot.common.core.page.PageDTO;
import com.fortuneboot.domain.command.fortune.FortuneGoodsKeeperAddCommand;
import com.fortuneboot.domain.command.fortune.FortuneGoodsKeeperModifyCommand;
import com.fortuneboot.domain.entity.fortune.FortuneGoodsKeeperEntity;
import com.fortuneboot.domain.query.fortune.FortuneGoodsKeeperQuery;
import com.fortuneboot.domain.vo.fortune.FortuneGoodsKeeperVo;
import com.fortuneboot.factory.fortune.FortuneGoodsKeeperFactory;
import com.fortuneboot.factory.fortune.model.FortuneGoodsKeeperModel;
import com.fortuneboot.repository.fortune.FortuneGoodsKeeperRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

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

    public PageDTO<FortuneGoodsKeeperVo> getPage(FortuneGoodsKeeperQuery query) {
        IPage<FortuneGoodsKeeperEntity> page = fortuneGoodsKeeperRepository.page(query.toPage(), query.addQueryCondition());
        List<FortuneGoodsKeeperVo> records = page.getRecords().stream().map(FortuneGoodsKeeperVo::new).toList();
        return new PageDTO<>(records, page.getTotal());
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
