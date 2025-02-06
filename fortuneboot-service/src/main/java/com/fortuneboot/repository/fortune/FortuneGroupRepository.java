package com.fortuneboot.repository.fortune;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fortuneboot.domain.entity.fortune.FortuneGroupEntity;

import java.util.List;

/**
 * 分组
 *
 * @Author work.chi.zhang@gmail.com
 * @Date 2024/6/5 23:11
 **/
public interface FortuneGroupRepository extends IService<FortuneGroupEntity> {

    /**
     * 查询启用的分组
     *
     * @param groupIds
     * @return
     */
    List<FortuneGroupEntity> getEnableByGroupIds(List<Long> groupIds);
}
