package com.fortuneboot.service.fortune.importer;

import com.fortuneboot.domain.command.fortune.FortuneBillAddCommand;
import com.fortuneboot.domain.command.fortune.FortuneBillExtraAddCommand;
import com.fortuneboot.domain.dto.fortune.CategoryAmountDTO;
import com.fortuneboot.domain.entity.fortune.FortuneBillExtraEntity;
import com.fortuneboot.domain.entity.fortune.FortuneCategoryRelationEntity;
import com.fortuneboot.domain.entity.fortune.FortuneMemberRelationEntity;
import com.fortuneboot.domain.entity.fortune.FortuneTagRelationEntity;
import com.fortuneboot.repository.fortune.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class FortuneBillImportRelationBatchTest {

    private final FortuneCategoryRelationRepo categoryRelationRepo = mock(FortuneCategoryRelationRepo.class);
    private final FortuneTagRelationRepo tagRelationRepo = mock(FortuneTagRelationRepo.class);
    private final FortuneMemberRelationRepo memberRelationRepo = mock(FortuneMemberRelationRepo.class);
    private final FortuneBillExtraRepo extraRepo = mock(FortuneBillExtraRepo.class);
    private final FortuneBillImportRelationBatch relationBatch = new FortuneBillImportRelationBatch(categoryRelationRepo, tagRelationRepo, memberRelationRepo, extraRepo);

    @Test
    @DisplayName("收集账单关系并批量保存")
    void collectAndSave_savesAllRelationTypes() {
        FortuneBillImportRelationBatch.Relations relations = relationBatch.create();
        FortuneBillAddCommand command = command();

        relationBatch.collect(relations, command, 99L);
        relationBatch.save(relations);

        ArgumentCaptor<List<FortuneCategoryRelationEntity>> categoryCaptor = ArgumentCaptor.captor();
        ArgumentCaptor<List<FortuneTagRelationEntity>> tagCaptor = ArgumentCaptor.captor();
        ArgumentCaptor<List<FortuneMemberRelationEntity>> memberCaptor = ArgumentCaptor.captor();
        ArgumentCaptor<List<FortuneBillExtraEntity>> extraCaptor = ArgumentCaptor.captor();
        verify(categoryRelationRepo).saveBatch(categoryCaptor.capture(), eq(1000));
        verify(tagRelationRepo).saveBatch(tagCaptor.capture(), eq(1000));
        verify(memberRelationRepo).saveBatch(memberCaptor.capture(), eq(1000));
        verify(extraRepo).saveBatch(extraCaptor.capture(), eq(1000));
        assertThat(categoryCaptor.getValue()).singleElement().satisfies(relation -> {
            assertThat(relation.getBillId()).isEqualTo(99L);
            assertThat(relation.getCategoryId()).isEqualTo(1L);
            assertThat(relation.getAmount()).isEqualByComparingTo("12.50");
        });
        assertThat(tagCaptor.getValue()).singleElement().extracting(FortuneTagRelationEntity::getTagId).isEqualTo(2L);
        assertThat(memberCaptor.getValue()).singleElement().extracting(FortuneMemberRelationEntity::getMemberId).isEqualTo(3L);
        assertThat(extraCaptor.getValue()).singleElement().satisfies(extra -> {
            assertThat(extra.getBillId()).isEqualTo(99L);
            assertThat(extra.getExtraType()).isEqualTo(1);
        });
    }

    private FortuneBillAddCommand command() {
        FortuneBillAddCommand command = new FortuneBillAddCommand();
        command.setCategoryAmountPair(List.of(new CategoryAmountDTO(1L, new BigDecimal("12.50"))));
        command.setTagIdList(List.of(2L));
        command.setMemberIdList(List.of(3L));
        FortuneBillExtraAddCommand extra = new FortuneBillExtraAddCommand();
        extra.setExtraType(1);
        extra.setAmount(BigDecimal.ONE);
        extra.setAccountSide(1);
        command.setExtras(List.of(extra));
        return command;
    }
}
