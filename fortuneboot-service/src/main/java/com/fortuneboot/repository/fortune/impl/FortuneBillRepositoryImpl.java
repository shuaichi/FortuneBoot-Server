package com.fortuneboot.repository.fortune.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fortuneboot.dao.fortune.FortuneBillMapper;
import com.fortuneboot.domain.entity.fortune.FortuneBillEntity;
import com.fortuneboot.repository.fortune.FortuneBillRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * @Author work.chi.zhang@gmail.com
 * @Date 2024/6/3 23:30
 **/
@Service
@AllArgsConstructor
public class FortuneBillRepositoryImpl extends ServiceImpl<FortuneBillMapper, FortuneBillEntity> implements FortuneBillRepository {

    private final FortuneBillMapper fortuneBillMapper;

    @Override
    public IPage<FortuneBillEntity> getPage(Page<FortuneBillEntity> page, LambdaQueryWrapper<FortuneBillEntity> wrapper) {
        return fortuneBillMapper.getPage(page,wrapper);
    }
}
