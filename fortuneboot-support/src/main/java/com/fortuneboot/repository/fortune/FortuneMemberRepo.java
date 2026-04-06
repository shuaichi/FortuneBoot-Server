package com.fortuneboot.repository.fortune;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fortuneboot.domain.entity.fortune.FortuneMemberEntity;

import java.util.List;

/**
 *
 * @author zhangchi118
 * @date 2026/3/19 09:29
 **/
public interface FortuneMemberRepo extends IService<FortuneMemberEntity> {
    FortuneMemberEntity getByBookIdAndName(Long bookId, String memberName);

    List<FortuneMemberEntity> getEnableMemberList(Long bookId);

    List<FortuneMemberEntity> getByIds(List<Long> memberIds);

    void removeByBookId(Long bookId);

    void removeByBookIds(List<Long> bookIds);
}