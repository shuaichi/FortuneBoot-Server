package com.fortuneboot.service.system;

import com.fortuneboot.common.core.page.PageDTO;
import com.fortuneboot.domain.common.command.BulkOperationCommand;
import com.fortuneboot.domain.dto.LoginLogDTO;
import com.fortuneboot.domain.query.system.LoginLogQuery;
import com.fortuneboot.domain.dto.OperationLogDTO;
import com.fortuneboot.domain.query.system.OperationLogQuery;
import com.fortuneboot.domain.entity.system.SysLoginInfoEntity;
import com.fortuneboot.domain.entity.system.SysOperationLogEntity;
import com.fortuneboot.repository.system.SysLoginInfoRepository;
import com.fortuneboot.repository.system.SysOperationLogRepository;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.util.List;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * @author valarchie
 */
@Service
@RequiredArgsConstructor
public class LogApplicationService {

    // TODO 命名到时候统一改成叫LoginLog
    private final SysLoginInfoRepository loginInfoRepository;

    private final SysOperationLogRepository operationLogRepository;

    public PageDTO<LoginLogDTO> getLoginInfoList(LoginLogQuery query) {
        Page<SysLoginInfoEntity> page = loginInfoRepository.page(query.toPage(), query.toQueryWrapper());
        List<LoginLogDTO> records = page.getRecords().stream().map(LoginLogDTO::new).collect(Collectors.toList());
        return new PageDTO<>(records, page.getTotal());
    }

    public void deleteLoginInfo(BulkOperationCommand<Long> deleteCommand) {
        QueryWrapper<SysLoginInfoEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("info_id", deleteCommand.getIds());
        loginInfoRepository.remove(queryWrapper);
    }

    public PageDTO<OperationLogDTO> getOperationLogList(OperationLogQuery query) {
        Page<SysOperationLogEntity> page = operationLogRepository.page(query.toPage(), query.toQueryWrapper());
        List<OperationLogDTO> records = page.getRecords().stream().map(OperationLogDTO::new).collect(Collectors.toList());
        return new PageDTO<>(records, page.getTotal());
    }

    public void deleteOperationLog(BulkOperationCommand<Long> deleteCommand) {
        operationLogRepository.removeBatchByIds(deleteCommand.getIds());
    }

}
