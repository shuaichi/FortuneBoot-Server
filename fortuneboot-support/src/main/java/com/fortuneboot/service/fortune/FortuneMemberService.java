package com.fortuneboot.service.fortune;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fortuneboot.common.core.page.PageDTO;
import com.fortuneboot.common.exception.ApiException;
import com.fortuneboot.common.exception.error.ErrorCode;
import com.fortuneboot.domain.command.fortune.FortuneMemberAddCommand;
import com.fortuneboot.domain.command.fortune.FortuneMemberModifyCommand;
import com.fortuneboot.domain.entity.fortune.FortuneMemberEntity;
import com.fortuneboot.domain.query.fortune.FortuneMemberQuery;
import com.fortuneboot.domain.vo.fortune.FortuneMemberVo;
import com.fortuneboot.factory.fortune.factory.FortuneMemberFactory;
import com.fortuneboot.factory.fortune.model.FortuneMemberModel;
import com.fortuneboot.repository.fortune.FortuneMemberRelationRepo;
import com.fortuneboot.repository.fortune.FortuneMemberRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author zhangchi118
 * @date 2026/3/19 09:34
 **/

@Service
@RequiredArgsConstructor
public class FortuneMemberService {

    private final FortuneMemberRepo fortuneMemberRepo;
    private final FortuneMemberFactory fortuneMemberFactory;
    private final FortuneMemberRelationRepo fortuneMemberRelationRepo;

    public PageDTO<FortuneMemberVo> getPage(FortuneMemberQuery query) {
        IPage<FortuneMemberEntity> page = fortuneMemberRepo.page(query.toPage(), query.addQueryCondition());
        List<FortuneMemberVo> records = page.getRecords().stream().map(FortuneMemberVo::new).collect(Collectors.toList());
        return new PageDTO<>(records, page.getTotal());
    }

    public List<FortuneMemberVo> getEnableList(Long bookId) {
        return fortuneMemberRepo.getEnableMemberList(bookId).stream()
                .map(FortuneMemberVo::new).collect(Collectors.toList());
    }

    public void add(FortuneMemberAddCommand addCommand) {
        FortuneMemberModel model = fortuneMemberFactory.create();
        model.loadAddCommand(addCommand);
        model.checkMemberExist();
        model.insert();
    }

    public void modify(FortuneMemberModifyCommand modifyCommand) {
        FortuneMemberModel model = fortuneMemberFactory.loadById(modifyCommand.getMemberId());
        model.checkBookId(modifyCommand.getBookId()); // 修复：防越权鉴权
        model.loadModifyCommand(modifyCommand);
        model.checkMemberExist();
        model.updateById();
    }

    public void moveToRecycleBin(Long bookId, Long memberId) {
        FortuneMemberModel model = fortuneMemberFactory.loadById(memberId);
        model.checkBookId(bookId);
        model.setRecycleBin(Boolean.TRUE);
        model.updateById();
    }

    public void remove(Long bookId, Long memberId) {
        Boolean used = fortuneMemberRelationRepo.existByMemberId(memberId);
        if (used) {
            throw new ApiException(ErrorCode.Business.COMMON_UNSUPPORTED_OPERATION, "成员已被账单使用，无法删除");
        }
        FortuneMemberModel model = fortuneMemberFactory.loadById(memberId);
        model.checkBookId(bookId);
        model.deleteById();
    }

    public void putBack(Long bookId, Long memberId) {
        FortuneMemberModel model = fortuneMemberFactory.loadById(memberId);
        model.checkBookId(bookId);
        model.setRecycleBin(Boolean.FALSE);
        model.updateById();
    }

    public void enable(Long bookId, Long memberId) {
        FortuneMemberModel model = fortuneMemberFactory.loadById(memberId);
        model.checkBookId(bookId);
        model.setEnable(Boolean.TRUE);
        model.updateById();
    }

    public void disable(Long bookId, Long memberId) {
        FortuneMemberModel model = fortuneMemberFactory.loadById(memberId);
        model.checkBookId(bookId);
        model.setEnable(Boolean.FALSE);
        model.updateById();
    }
}
