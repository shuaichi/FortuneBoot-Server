package com.fortuneboot.repository.fortune.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fortuneboot.common.utils.mybatis.WrapperUtil;
import com.fortuneboot.dao.fortune.FortuneFileMapper;
import com.fortuneboot.domain.entity.fortune.FortuneFileEntity;
import com.fortuneboot.repository.fortune.FortuneFileRepository;
import lombok.AllArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 账单文件
 * @Author work.chi.zhang@gmail.com
 * @Date 2024/6/5 23:03
 **/
@Service
@AllArgsConstructor
public class FortuneFileRepositoryImpl extends ServiceImpl<FortuneFileMapper, FortuneFileEntity> implements FortuneFileRepository {


    private final FortuneFileMapper fortuneFileMapper;

    @Override
    public List<FortuneFileEntity> getByBillId(Long billId) {
        LambdaQueryWrapper<FortuneFileEntity> queryWrapper = WrapperUtil.getLambdaQueryWrapper(FortuneFileEntity.class);
        queryWrapper.eq(FortuneFileEntity::getBillId, billId);
        return this.list(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeByBillId(Long billId) {
        List<FortuneFileEntity> list = this.getByBillId(billId);
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        this.removeByIds(list);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void phyRemoveByBillId(Long billId) {
        List<FortuneFileEntity> list = this.getByBillId(billId);
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        List<Long> ids = list.stream().map(FortuneFileEntity::getFileId).toList();
        fortuneFileMapper.phyDeleteByIds(ids);
    }

}
