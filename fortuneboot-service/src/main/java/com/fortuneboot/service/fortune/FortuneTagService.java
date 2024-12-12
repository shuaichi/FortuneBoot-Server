package com.fortuneboot.service.fortune;

import com.fortuneboot.domain.entity.fortune.FortuneTagEntity;
import com.fortuneboot.domain.query.fortune.FortuneTagQuery;
import com.fortuneboot.factory.fortune.FortuneTagFactory;
import com.fortuneboot.repository.fortune.FortuneTagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 账本标签服务
 *
 * @author zhangchi118
 * @date 2024/12/11 16:13
 **/
@Slf4j
@Service
@RequiredArgsConstructor
public class FortuneTagService {

    private final FortuneTagRepository fortuneTagRepository;

    private final FortuneTagFactory fortuneTagFactory;

    public List<FortuneTagEntity> getTagList(FortuneTagQuery query) {
        return fortuneTagRepository.list(query.addQueryCondition());
    }
}
