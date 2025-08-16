package com.fortuneboot.repository.fortune.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fortuneboot.dao.fortune.FortuneAlertMapper;
import com.fortuneboot.domain.entity.fortune.FortuneAlertEntity;
import com.fortuneboot.repository.fortune.FortuneAlertRepo;
import org.springframework.stereotype.Service;

/**
 * 提醒事项
 *
 * @Author work.chi.zhang@gmail.com
 * @Date 2024/6/4 22:50
 **/
@Service
public class FortuneAlertRepoImpl extends ServiceImpl<FortuneAlertMapper, FortuneAlertEntity> implements FortuneAlertRepo {
}
