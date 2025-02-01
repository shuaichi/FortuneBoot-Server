package com.fortuneboot.repository.fortune;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fortuneboot.domain.entity.fortune.FortuneBookEntity;

import java.util.List;

/**
 * 账本
 *
 * @Author work.chi.zhang@gmail.com
 * @Date 2024/6/4 23:13
 **/
public interface FortuneBookRepository extends IService<FortuneBookEntity> {

    /**
     * 根据分组id查询
     * @return
     */
    List<FortuneBookEntity> getByGroupId(Long groupId);
}
