package com.fortuneboot.factory.fortune.factory;

import com.fortuneboot.common.exception.ApiException;
import com.fortuneboot.common.exception.error.ErrorCode;
import com.fortuneboot.domain.entity.fortune.FortuneFileEntity;
import com.fortuneboot.factory.fortune.model.FortuneFileModel;
import com.fortuneboot.repository.fortune.FortuneFileRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;

/**
 * 附件工厂
 *
 * @author zhangchi118
 * @date 2025/2/27 16:56
 **/
@Component
@RequiredArgsConstructor
public class FortuneFileFactory {

    private final FortuneFileRepo fortuneFileRepo;

    private FortuneFileModel create() {
        return new FortuneFileModel(fortuneFileRepo);
    }


    public FortuneFileModel loadById(Long fileId) {
        FortuneFileEntity entity = fortuneFileRepo.getById(fileId);
        if (Objects.isNull(entity)) {
            throw new ApiException(ErrorCode.Business.COMMON_OBJECT_NOT_FOUND, fileId, "附件");
        }
        return new FortuneFileModel(entity, fortuneFileRepo);
    }

    public List<FortuneFileModel> createByMultipartFileList(Long billId, List<MultipartFile> fileList) {
        return fileList.stream().map(item -> {
            FortuneFileModel fortuneFileModel = this.create();
            fortuneFileModel.loadByMultipartFile(billId, item);
            return fortuneFileModel;
        }).toList();
    }
}
