package com.fortuneboot.repository.fortune;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fortuneboot.domain.entity.fortune.FortuneAccountEntity;

import java.util.List;

/**
 * 账户
 *
 * @Author work.chi.zhang@gmail.com
 * @Date 2024/06/03 23:02
 */
public interface FortuneAccountRepository extends IService<FortuneAccountEntity> {

    /**
     * 查询启用的账户
     *
     * @param groupId
     * @return
     */
    List<FortuneAccountEntity> getEnableList(Long groupId);
}
