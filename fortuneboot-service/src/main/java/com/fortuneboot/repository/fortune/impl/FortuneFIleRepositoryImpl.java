package com.fortuneboot.repository.fortune.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fortuneboot.dao.fortune.FortuneFileMapper;
import com.fortuneboot.domain.entity.fortune.FortuneFileEntity;
import com.fortuneboot.repository.fortune.FortuneFileRepository;
import org.springframework.stereotype.Service;

/**
 * 账单文件
 * @Author work.chi.zhang@gmail.com
 * @Date 2024/6/5 23:03
 **/
@Service
public class FortuneFIleRepositoryImpl extends ServiceImpl<FortuneFileMapper, FortuneFileEntity> implements FortuneFileRepository {
}
