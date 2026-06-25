package com.fortuneboot.service.fortune.importer;

import com.fortuneboot.common.exception.ApiException;
import com.fortuneboot.domain.entity.fortune.*;
import com.fortuneboot.factory.fortune.model.FortuneBookModel;
import com.fortuneboot.repository.fortune.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FortuneBillImportContextLoaderTest {

    private final FortuneAccountRepo accountRepo = mock(FortuneAccountRepo.class);
    private final FortuneCategoryRepo categoryRepo = mock(FortuneCategoryRepo.class);
    private final FortuneTagRepo tagRepo = mock(FortuneTagRepo.class);
    private final FortunePayeeRepo payeeRepo = mock(FortunePayeeRepo.class);
    private final FortuneMemberRepo memberRepo = mock(FortuneMemberRepo.class);
    private final FortuneBillImportContextLoader loader = new FortuneBillImportContextLoader(accountRepo, categoryRepo, tagRepo, payeeRepo, memberRepo);

    @Test
    @DisplayName("按账本和分组加载上下文")
    void load_validBook_buildsContext() {
        FortuneBookModel book = bookModel();
        FortuneAccountEntity account = account("现金", 1L);
        FortuneCategoryEntity category = category("餐饮", 1L);
        FortuneTagEntity tag = tag("外卖", 1L);
        FortunePayeeEntity payee = payee("京东", 1L);
        FortuneMemberEntity member = member("张三", 1L);
        when(accountRepo.getEnableAccountList(2L)).thenReturn(List.of(account));
        when(categoryRepo.getEnableCategoryList(eq(1L), any())).thenReturn(List.of(category));
        when(tagRepo.getEnableTagList(eq(1L), any())).thenReturn(List.of(tag));
        when(payeeRepo.getEnablePayeeList(eq(1L), any())).thenReturn(List.of(payee));
        when(memberRepo.getEnableMemberList(1L)).thenReturn(List.of(member));

        FortuneBillImportContext context = loader.load(book);

        assertThat(context.getAccountMap()).containsEntry("现金", account);
        assertThat(context.getCategoryMap().values()).allSatisfy(map -> assertThat(map).containsEntry("餐饮", category));
        assertThat(context.getTagMap().values()).allSatisfy(map -> assertThat(map).containsEntry("外卖", tag));
        assertThat(context.getPayeeMap().values()).allSatisfy(map -> assertThat(map).containsEntry("京东", payee));
        assertThat(context.getMemberMap()).containsEntry("张三", member);
    }

    @Test
    @DisplayName("上下文加载发现重名账户时失败")
    void load_duplicateAccount_throws() {
        when(accountRepo.getEnableAccountList(2L)).thenReturn(List.of(account("现金", 1L), account("现金", 2L)));

        assertThatThrownBy(() -> loader.load(bookModel())).isInstanceOf(ApiException.class)
                .hasMessageContaining("账户存在重名");
    }

    private FortuneBookModel bookModel() {
        FortuneBookEntity book = new FortuneBookEntity();
        book.setBookId(1L);
        book.setGroupId(2L);
        return new FortuneBookModel(book, null);
    }

    private FortuneAccountEntity account(String name, Long id) {
        FortuneAccountEntity entity = new FortuneAccountEntity();
        entity.setAccountName(name);
        entity.setAccountId(id);
        return entity;
    }

    private FortuneCategoryEntity category(String name, Long id) {
        FortuneCategoryEntity entity = new FortuneCategoryEntity();
        entity.setCategoryName(name);
        entity.setCategoryId(id);
        return entity;
    }

    private FortuneTagEntity tag(String name, Long id) {
        FortuneTagEntity entity = new FortuneTagEntity();
        entity.setTagName(name);
        entity.setTagId(id);
        return entity;
    }

    private FortunePayeeEntity payee(String name, Long id) {
        FortunePayeeEntity entity = new FortunePayeeEntity();
        entity.setPayeeName(name);
        entity.setPayeeId(id);
        return entity;
    }

    private FortuneMemberEntity member(String name, Long id) {
        FortuneMemberEntity entity = new FortuneMemberEntity();
        entity.setMemberName(name);
        entity.setMemberId(id);
        return entity;
    }
}
