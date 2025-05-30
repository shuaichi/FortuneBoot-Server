package com.fortuneboot.dao.fortune;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fortuneboot.domain.entity.fortune.FortuneFileEntity;
import org.apache.ibatis.annotations.Delete;

import java.util.List;

/**
 * 账单文件
 *
 * @Author work.chi.zhang@gmail.com
 * @Date 2024/6/5 23:02
 **/
public interface FortuneFileMapper extends BaseMapper<FortuneFileEntity> {

    /**
     * 根据id物理删除
     *
     * @param ids
     */
    void phyDeleteByIds(List<Long> ids);
}
