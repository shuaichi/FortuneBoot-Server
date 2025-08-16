package com.fortuneboot.service.fortune;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fortuneboot.factory.fortune.factory.FortuneFileFactory;
import com.fortuneboot.factory.fortune.model.FortuneFileModel;
import com.fortuneboot.repository.fortune.FortuneFileRepo;
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

    private final FortuneFileRepo fortuneFileRepo;

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
        fortuneFileRepo.phyRemoveByBillId(billId);
    }

}
