package com.fortuneboot.repository.fortune;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fortuneboot.domain.entity.fortune.FortuneCategoryEntity;

import java.util.List;

/**
 * 分类
 *
 * @Author work.chi.zhang@gmail.com
 * @Date 2024/6/4 23:26
 **/
public interface FortuneCategoryRepository extends IService<FortuneCategoryEntity> {

    /**
     * 批量查询
     *
     * @param categoryIds
     * @return
     */
    List<FortuneCategoryEntity> getByIds(List<Long> categoryIds);

    /**
     * 查询启用的账本
     *
     * @param bookId
     * @param billType
     * @return
     */
    List<FortuneCategoryEntity> getEnableCategoryList(Long bookId, Integer billType);

    /**
     * 根据账本id批量查询
     *
     * @param bookId
     * @return
     */
    void removeByBookId(Long bookId);

    /**
     * 根据账本id批量删除
     *
     * @param bookIds
     */
    void removeByBookIds(List<Long> bookIds);

    /**
     * 根据父级编码查询
     *
     * @param parentId
     * @return
     */
    List<FortuneCategoryEntity> getByParentId(Long parentId);
}
