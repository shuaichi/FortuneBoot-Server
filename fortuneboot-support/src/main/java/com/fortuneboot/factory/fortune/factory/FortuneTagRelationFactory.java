package com.fortuneboot.factory.fortune.factory;

import com.fortuneboot.factory.fortune.model.FortuneTagRelationModel;
import com.fortuneboot.repository.fortune.FortuneTagRelationRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 标签关系工厂
 *
 * @author zhangchi118
 * @date 2025/1/29 20:11
 **/
@Component
@RequiredArgsConstructor
public class FortuneTagRelationFactory {

    private final FortuneTagRelationRepo fortuneTagRelationRepo;

    public FortuneTagRelationModel create() {
        return new FortuneTagRelationModel(fortuneTagRelationRepo);
    }
}
