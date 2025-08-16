package com.fortuneboot.repository.fortune;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fortuneboot.domain.entity.fortune.FortuneCurrencyEntity;

import java.util.List;

/**
 * 货币
 *
 * @Author work.chi.zhang@gmail.com
 * @Date 2024/6/5 22:48
 **/
public interface FortuneCurrencyRepo extends IService<FortuneCurrencyEntity> {

    /**
     * 查询全部货币
     *
     * @return
     */
    List<FortuneCurrencyEntity> getAll();
}
