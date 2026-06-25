package com.fortuneboot.service.fortune.importer;

import com.fortuneboot.common.enums.fortune.BillTypeEnum;
import com.fortuneboot.common.exception.ApiException;
import com.fortuneboot.common.exception.error.ErrorCode;
import com.fortuneboot.domain.entity.fortune.*;
import com.fortuneboot.factory.fortune.model.FortuneBookModel;
import com.fortuneboot.repository.fortune.*;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author zhangchi118
 */
@Component
@RequiredArgsConstructor
class FortuneBillImportContextLoader {

    private static final List<Integer> SUPPORTED_BILL_TYPES = List.of(
            BillTypeEnum.EXPENSE.getValue(),
            BillTypeEnum.INCOME.getValue(),
            BillTypeEnum.TRANSFER.getValue(),
            BillTypeEnum.ADVANCE.getValue(),
            BillTypeEnum.REIMBURSE.getValue()
    );

    private final FortuneAccountRepo fortuneAccountRepo;
    private final FortuneCategoryRepo fortuneCategoryRepo;
    private final FortuneTagRepo fortuneTagRepo;
    private final FortunePayeeRepo fortunePayeeRepo;
    private final FortuneMemberRepo fortuneMemberRepo;

    FortuneBillImportContext load(FortuneBookModel bookModel) {
        FortuneBillImportContext context = new FortuneBillImportContext();
        context.setBookModel(bookModel);
        List<FortuneAccountEntity> accounts = fortuneAccountRepo.getEnableAccountList(bookModel.getGroupId());
        context.setAccountMap(toUniqueMap(accounts, FortuneAccountEntity::getAccountName, "账户"));
        context.setAccountIdMap(accounts.stream().collect(Collectors.toMap(FortuneAccountEntity::getAccountId, Function.identity())));
        for (Integer billType : SUPPORTED_BILL_TYPES) {
            String billTypeName = BillTypeEnum.getDescByValue(billType);
            context.putCategories(billType, toUniqueMap(fortuneCategoryRepo.getEnableCategoryList(bookModel.getBookId(), billType), FortuneCategoryEntity::getCategoryName, billTypeName + "分类"));
            context.putTags(billType, toUniqueMap(fortuneTagRepo.getEnableTagList(bookModel.getBookId(), billType), FortuneTagEntity::getTagName, billTypeName + "标签"));
            context.putPayees(billType, toUniqueMap(fortunePayeeRepo.getEnablePayeeList(bookModel.getBookId(), billType), FortunePayeeEntity::getPayeeName, billTypeName + "交易对象"));
        }
        List<FortuneMemberEntity> members = fortuneMemberRepo.getEnableMemberList(bookModel.getBookId());
        context.setMemberMap(toUniqueMap(members, FortuneMemberEntity::getMemberName, "成员"));
        return context;
    }

    private <T> Map<String, T> toUniqueMap(List<T> list, Function<T, String> keyMapper, String name) {
        Map<String, T> map = new HashMap<>();
        Set<String> duplicateNames = new HashSet<>();
        for (T item : list) {
            String key = keyMapper.apply(item);
            if (map.containsKey(key)) {
                duplicateNames.add(key);
                continue;
            }
            map.put(key, item);
        }
        if (CollectionUtils.isNotEmpty(duplicateNames)) {
            throw new ApiException(ErrorCode.Business.UPLOAD_IMPORT_EXCEL_FAILED, name + "存在重名，请先调整名称后再导入：" + duplicateNames);
        }
        return map;
    }
}
