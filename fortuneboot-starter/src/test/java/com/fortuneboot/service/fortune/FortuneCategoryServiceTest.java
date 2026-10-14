package com.fortuneboot.service.fortune;

import com.fortuneboot.domain.entity.fortune.FortuneCategoryEntity;
import com.fortuneboot.factory.fortune.factory.FortuneCategoryFactory;
import com.fortuneboot.repository.fortune.FortuneCategoryRelationRepo;
import com.fortuneboot.repository.fortune.FortuneCategoryRepo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("分类服务按流水类型映射可用分类")
class FortuneCategoryServiceTest {

    private final FortuneCategoryRepo categoryRepo = mock(FortuneCategoryRepo.class);
    private final FortuneCategoryRelationRepo relationRepo = mock(FortuneCategoryRelationRepo.class);
    private final FortuneCategoryService service =
            new FortuneCategoryService(categoryRepo, mock(FortuneCategoryFactory.class), relationRepo);

    @Test
    @DisplayName("7查支出分类，8查收入分类，null查全部，9—12不提供分类")
    void getEnableCategoryList_mapsBillTypeToCategoryType() {
        FortuneCategoryEntity category = new FortuneCategoryEntity();
        when(categoryRepo.getEnableCategoryList(eq(901L), any())).thenReturn(List.of(category));

        assertThat(service.getEnableCategoryList(901L, 7)).containsExactly(category);
        verify(categoryRepo).getEnableCategoryList(901L, 1);

        assertThat(service.getEnableCategoryList(901L, 8)).containsExactly(category);
        verify(categoryRepo).getEnableCategoryList(901L, 2);

        assertThat(service.getEnableCategoryList(901L, null)).containsExactly(category);
        verify(categoryRepo).getEnableCategoryList(901L, null);

        for (int billType : List.of(9, 10, 11, 12)) {
            assertThat(service.getEnableCategoryList(901L, billType)).isEmpty();
        }
        verify(categoryRepo, never()).getEnableCategoryList(eq(901L), eq(9));
        verify(categoryRepo, never()).getEnableCategoryList(eq(901L), eq(10));
        verify(categoryRepo, never()).getEnableCategoryList(eq(901L), eq(11));
        verify(categoryRepo, never()).getEnableCategoryList(eq(901L), eq(12));
    }
}
