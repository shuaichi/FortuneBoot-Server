package com.fortuneboot.repository.fortune.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fortuneboot.dao.fortune.FortuneGroupMapper;
import com.fortuneboot.domain.entity.fortune.FortuneGroupEntity;
import com.fortuneboot.repository.fortune.FortuneGroupRepository;
import org.springframework.stereotype.Service;

/**
 * 分组
 *
 * @Author work.chi.zhang@gmail.com
 * @Date 2024/6/5 23:12
 **/
@Service
public class FortuneGroupRepositoryImpl extends ServiceImpl<FortuneGroupMapper, FortuneGroupEntity> implements FortuneGroupRepository {
}
