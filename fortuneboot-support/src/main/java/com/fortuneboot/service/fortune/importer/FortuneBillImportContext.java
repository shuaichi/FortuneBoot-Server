package com.fortuneboot.service.fortune.importer;

import com.fortuneboot.domain.entity.fortune.*;
import com.fortuneboot.factory.fortune.model.FortuneBookModel;
import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zhangchi118
 */
@Data
class FortuneBillImportContext {

    private FortuneBookModel bookModel;

    private Map<String, FortuneAccountEntity> accountMap = new HashMap<>();

    private Map<Integer, Map<String, FortuneCategoryEntity>> categoryMap = new HashMap<>();

    private Map<Integer, Map<String, FortuneTagEntity>> tagMap = new HashMap<>();

    private Map<Integer, Map<String, FortunePayeeEntity>> payeeMap = new HashMap<>();

    private Map<String, FortuneMemberEntity> memberMap = new HashMap<>();

    private Map<Long, FortuneAccountEntity> accountIdMap = new HashMap<>();

    FortuneCategoryEntity getCategory(Integer billType, String name) {
        return categoryMap.getOrDefault(billType, Map.of()).get(name);
    }

    FortuneTagEntity getTag(Integer billType, String name) {
        return tagMap.getOrDefault(billType, Map.of()).get(name);
    }

    FortunePayeeEntity getPayee(Integer billType, String name) {
        return payeeMap.getOrDefault(billType, Map.of()).get(name);
    }

    void putCategories(Integer billType, Map<String, FortuneCategoryEntity> categories) {
        categoryMap.put(billType, categories);
    }

    void putTags(Integer billType, Map<String, FortuneTagEntity> tags) {
        tagMap.put(billType, tags);
    }

    void putPayees(Integer billType, Map<String, FortunePayeeEntity> payees) {
        payeeMap.put(billType, payees);
    }
}
