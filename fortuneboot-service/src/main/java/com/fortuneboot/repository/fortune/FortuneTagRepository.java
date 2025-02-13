package com.fortuneboot.repository.fortune;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fortuneboot.domain.entity.fortune.FortuneTagEntity;

import java.util.List;
import java.util.Map;

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

    /**
     * 根据账本id查询
     *
     * @param bookId
     * @return
     */
    void removeByBookId(Long bookId);

    /**
     * @param bookIds
     */
    void removeByBookIds(List<Long> bookIds);

    /**
     * 根据父级id查询子级
     *
     * @param parentId
     * @return
     */
    List<FortuneTagEntity> getByParentId(Long parentId);

    /**
     * 通过tagId查询是否已存在数据
     *
     * @param tagId
     * @return
     */
    Boolean existsByTagId(Long tagId);

    /**
     * 根据父级编码查询
     *
     * @param parentIds
     * @return
     */
    Map<Long, List<FortuneTagEntity>> getByParentIds(List<Long> parentIds);
}
