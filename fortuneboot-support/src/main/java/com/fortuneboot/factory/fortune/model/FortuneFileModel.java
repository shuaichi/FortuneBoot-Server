package com.fortuneboot.factory.fortune.model;

import cn.hutool.core.bean.BeanUtil;
import com.fortuneboot.common.constant.Constants;
import com.fortuneboot.common.exception.ApiException;
import com.fortuneboot.common.exception.error.ErrorCode;
import com.fortuneboot.domain.command.fortune.FortuneFileAddCommand;
import com.fortuneboot.domain.entity.fortune.FortuneFileEntity;
import com.fortuneboot.repository.fortune.FortuneFileRepo;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Objects;

/**
 * @author zhangchi118
 * @date 2025/2/27 16:56
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class FortuneFileModel extends FortuneFileEntity {

    private FortuneFileRepo fortuneFileRepo;

    /**
     * 默认的文件名最大长度 127
     */
    public static final int MAX_FILE_NAME_LENGTH = 127;

    /**
     * 文件大小限制
     */
    public static final long MAX_FILE_SIZE = 5 * Constants.MB;

    public FortuneFileModel(FortuneFileRepo fortuneFileRepo) {
        this.fortuneFileRepo = fortuneFileRepo;
    }

    public FortuneFileModel(FortuneFileEntity entity, FortuneFileRepo fortuneFileRepo) {
        if (Objects.nonNull(entity)) {
            BeanUtil.copyProperties(entity, this);
        }
        this.fortuneFileRepo = fortuneFileRepo;
    }

    public void loadAddCommand(FortuneFileAddCommand command) {
        if (Objects.nonNull(command)) {
            BeanUtil.copyProperties(command, this, "categoryId");
        }
    }

    public void loadByMultipartFile(Long billId, MultipartFile file) {
        if (Objects.nonNull(file)) {
            this.setBillId(billId);
            this.setOriginalName(file.getOriginalFilename());
            if (Objects.requireNonNull(file.getOriginalFilename()).length() > MAX_FILE_NAME_LENGTH) {
                throw new ApiException(ErrorCode.Business.UPLOAD_FILE_NAME_EXCEED_MAX_LENGTH, MAX_FILE_NAME_LENGTH);
            }
            this.setContentType(file.getContentType());
            if (file.getSize() <= 0) {
                throw new ApiException(ErrorCode.Business.UPLOAD_FILE_IS_EMPTY);
            }
            if (file.getSize() > MAX_FILE_SIZE){
                throw new ApiException(ErrorCode.Business.UPLOAD_FILE_SIZE_EXCEED_MAX_SIZE, MAX_FILE_SIZE / Constants.MB);
            }
            this.setSize(file.getSize());
            try {
                this.setFileData(file.getBytes());
            } catch (IOException e) {
                throw new ApiException(ErrorCode.Business.UPLOAD_FILE_FAILED, e.getMessage());
            }
        }
    }
}
