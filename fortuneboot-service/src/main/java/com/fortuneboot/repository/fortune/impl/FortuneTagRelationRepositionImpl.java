package com.fortuneboot.repository.fortune.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fortuneboot.dao.fortune.FortuneTagRelationMapper;
import com.fortuneboot.domain.entity.fortune.FortuneTagRelationEntity;
import com.fortuneboot.repository.fortune.FortuneTagRelationRepository;
import org.springframework.stereotype.Service;

/**
 * 交易标签和账单的关系表
 *
 * @Author work.chi.zhang@gmail.com
 * @Date 2024/6/5 23:37
 **/
@Service
public class FortuneTagRelationRepositionImpl extends ServiceImpl<FortuneTagRelationMapper, FortuneTagRelationEntity> implements FortuneTagRelationRepository {
}
