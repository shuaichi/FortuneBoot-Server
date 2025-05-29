package com.fortuneboot.service.fortune;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fortuneboot.domain.entity.fortune.FortuneFileEntity;
import com.fortuneboot.factory.fortune.FortuneFileFactory;
import com.fortuneboot.factory.fortune.model.FortuneFileModel;
import com.fortuneboot.repository.fortune.FortuneFileRepository;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @author zhangchi118
 * @date 2025/2/27 16:55
 **/
@Slf4j
@Service
@RequiredArgsConstructor
public class FortuneFileService {

    private final FortuneFileRepository fortuneFileRepository;

    private final FortuneFileFactory fortuneFileFactory;

    public void batchAdd(Long billId,List<MultipartFile> fileList){
        if (CollectionUtils.isEmpty(fileList)){
            return;
        }
        List<FortuneFileModel> modelList = fortuneFileFactory.createByMultipartFileList(billId, fileList);
        modelList.forEach(Model::insert);
    }

    /**
     * 批量删除附件
     */
    public void phyRemoveByBillId(Long billId) {
        fortuneFileRepository.removeByBillId(billId);
    }

}
