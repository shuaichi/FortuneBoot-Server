package com.fortuneboot.repository.fortune;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fortuneboot.domain.entity.fortune.FortuneTagEntity;

import java.util.List;

/**
 * 交易标签
 *
 * @Author work.chi.zhang@gmail.com
 * @Date 2024/6/5 23:31
 **/
public interface FortuneTagRepository extends IService<FortuneTagEntity> {
    /**
     * 根据账本id和名称查询
     *
     * @return
     */
    FortuneTagEntity getByBookIdAndName(Long bookId, String tagName);

    /**
     * 查询启用的标签
     *
     * @param bookId
     * @param billType
     * @return
     */
    List<FortuneTagEntity> getEnableTagList(Long bookId, Integer billType);

    /**
     * 根据id批量查询
     *
     * @param tagIds
     * @return
     */
    List<FortuneTagEntity> getByIds(List<Long> tagIds);
}
