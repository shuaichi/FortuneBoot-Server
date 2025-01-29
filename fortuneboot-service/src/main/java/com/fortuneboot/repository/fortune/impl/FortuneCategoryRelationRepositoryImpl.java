package com.fortuneboot.repository.fortune.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fortuneboot.dao.fortune.FortuneCategoryRelationMapper;
import com.fortuneboot.domain.entity.fortune.FortuneCategoryRelationEntity;
import com.fortuneboot.repository.fortune.FortuneCategoryRelationRepository;
import org.springframework.stereotype.Service;

/**
 * 分类账单关系
 *
 * @author zhangchi118
 * @date 2025/1/29 23:31
 **/
@Service
public class FortuneCategoryRelationRepositoryImpl extends ServiceImpl<FortuneCategoryRelationMapper, FortuneCategoryRelationEntity> implements FortuneCategoryRelationRepository {

}
