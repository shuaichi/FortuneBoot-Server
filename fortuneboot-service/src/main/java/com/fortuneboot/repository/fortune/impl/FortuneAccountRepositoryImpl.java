package com.fortuneboot.repository.fortune.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fortuneboot.dao.fortune.FortuneAccountMapper;
import com.fortuneboot.domain.entity.fortune.FortuneAccountEntity;
import com.fortuneboot.repository.fortune.FortuneAccountRepository;
import org.springframework.stereotype.Service;

/**
 * 账户
 *
 * @Author work.chi.zhang@gmail.com
 * @Date 2024/6/3 23:04
 **/
@Service
public class FortuneAccountRepositoryImpl extends ServiceImpl<FortuneAccountMapper, FortuneAccountEntity> implements FortuneAccountRepository {
}
