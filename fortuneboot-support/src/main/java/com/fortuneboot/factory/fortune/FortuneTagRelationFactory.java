package com.fortuneboot.factory.fortune;

import com.fortuneboot.factory.fortune.model.FortuneTagRelationModel;
import com.fortuneboot.repository.fortune.FortuneTagRelationRepository;
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

    private final FortuneTagRelationRepository fortuneTagRelationRepository;

    public FortuneTagRelationModel create() {
        return new FortuneTagRelationModel(fortuneTagRelationRepository);
    }
}
