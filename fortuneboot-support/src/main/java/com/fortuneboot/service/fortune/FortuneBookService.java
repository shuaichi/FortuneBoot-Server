package com.fortuneboot.service.fortune;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fortuneboot.domain.bo.fortune.*;
import com.fortuneboot.domain.bo.fortune.tenplate.BookTemplateBo;
import com.fortuneboot.domain.bo.fortune.tenplate.CategoryTemplateBo;
import com.fortuneboot.domain.bo.fortune.tenplate.PayeeTemplateBo;
import com.fortuneboot.domain.bo.fortune.tenplate.TagTemplateBo;
import com.fortuneboot.domain.command.fortune.*;
import com.fortuneboot.domain.entity.fortune.FortuneBookEntity;
import com.fortuneboot.domain.query.fortune.FortuneBookQuery;
import com.fortuneboot.factory.fortune.factory.FortuneBookFactory;
import com.fortuneboot.factory.fortune.factory.FortuneGroupFactory;
import com.fortuneboot.factory.fortune.model.FortuneBookModel;
import com.fortuneboot.factory.fortune.model.FortuneCategoryModel;
import com.fortuneboot.factory.fortune.model.FortuneGroupModel;
import com.fortuneboot.factory.fortune.model.FortuneTagModel;
import com.fortuneboot.repository.fortune.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;

/**
 * 账本service
 *
 * @author zhangchi118
 * @date 2024/11/29 15:50
 **/
@Slf4j
@Service
@RequiredArgsConstructor
public class FortuneBookService {

    private final FortuneBookFactory fortuneBookFactory;

    private final FortuneBookRepo fortuneBookRepo;

    private final FortuneGroupFactory fortuneGroupFactory;

    private final ApplicationScopeBo applicationScopeBo;

    private final FortuneTagService fortuneTagService;

    private final FortuneTagRepo fortuneTagRepo;

    private final FortuneCategoryService fortuneCategoryService;

    private final FortunePayeeService fortunePayeeService;

    private final FortuneCategoryRepo fortuneCategoryRepo;

    private final FortunePayeeRepo fortunePayeeRepo;

    private final FortuneBillService fortuneBillService;

    public IPage<FortuneBookEntity> getPage(FortuneBookQuery query) {
        return fortuneBookRepo.page(query.toPage(), query.addQueryCondition());
    }

    public List<FortuneBookEntity> getEnableBookList(Long groupId) {
        return fortuneBookRepo.getEnableBookList(groupId);
    }

    @Transactional(rollbackFor = Exception.class)
    public FortuneBookModel add(FortuneBookAddCommand bookAddCommand) {
        // 创建账本模型并插入
        FortuneBookModel fortuneBookModel = fortuneBookFactory.create();
        fortuneBookModel.loadAddCommand(bookAddCommand);
        fortuneBookModel.setEnable(Boolean.TRUE);

        // 校验是否使用模板
        if (Objects.isNull(bookAddCommand.getBookTemplate())) {
            fortuneBookModel.insert();
            return fortuneBookModel;
        }
        // 获取模板并提前处理备注
        BookTemplateBo bookTemplateBo = getBookTemplate(bookAddCommand);
        if (Objects.nonNull(bookTemplateBo) && StringUtils.isBlank(fortuneBookModel.getRemark())) {
            fortuneBookModel.setRemark(bookTemplateBo.getRemark());
        }
        fortuneBookModel.insert();

        // 处理模板数据（当模板存在时）
        if (Objects.nonNull(bookTemplateBo)) {
            processTagTemplates(fortuneBookModel, bookTemplateBo.getTagList());
            processCategoryTemplates(fortuneBookModel, bookTemplateBo.getCategoryList());
            processPayeeTemplates(fortuneBookModel, bookTemplateBo.getPayeeList());
        }

        return fortuneBookModel;
    }

    private BookTemplateBo getBookTemplate(FortuneBookAddCommand command) {
        return applicationScopeBo.getBookTemplateBoList().stream()
                .filter(item -> Objects.equals(item.getBookTemplateId(), command.getBookTemplate()))
                .findFirst()
                .orElse(null);
    }

    private void processTagTemplates(FortuneBookModel book, List<TagTemplateBo> tagTemplates) {
        Map<String, Long> nameToIdMap = new HashMap<>();
        tagTemplates.forEach(template -> {
            FortuneTagAddCommand command = new FortuneTagAddCommand();
            BeanUtil.copyProperties(template, command);
            command.setEnable(Boolean.TRUE);
            command.setBookId(book.getBookId());
            resolveParentId(command, template.getParentId(), nameToIdMap,
                    id -> getTagNameById(tagTemplates, id));

            FortuneTagModel model = fortuneTagService.add(command);
            nameToIdMap.put(template.getTagName(), model.getTagId());
        });
    }

    private void processCategoryTemplates(FortuneBookModel book, List<CategoryTemplateBo> categoryTemplates) {
        Map<String, Long> nameToIdMap = new HashMap<>();
        categoryTemplates.forEach(template -> {
            FortuneCategoryAddCommand command = new FortuneCategoryAddCommand();
            BeanUtil.copyProperties(template, command);
            command.setEnable(Boolean.TRUE);
            command.setBookId(book.getBookId());
            resolveParentId(command, template.getParentId(), nameToIdMap,
                    id -> getCategoryNameById(categoryTemplates, id));

            FortuneCategoryModel model = fortuneCategoryService.add(command);
            nameToIdMap.put(template.getCategoryName(), model.getCategoryId());
        });
    }

    private void processPayeeTemplates(FortuneBookModel book, List<PayeeTemplateBo> payeeTemplates) {
        payeeTemplates.forEach(template -> {
            FortunePayeeAddCommand command = new FortunePayeeAddCommand();
            BeanUtil.copyProperties(template, command);
            command.setEnable(Boolean.TRUE);
            command.setBookId(book.getBookId());
            fortunePayeeService.add(command);
        });
    }

    private void resolveParentId(Object command, Long originalParentId,
                                 Map<String, Long> nameToIdMap,
                                 Function<Long, String> nameResolver) {
        try {
            if (Objects.isNull(originalParentId)) {
                command.getClass().getMethod("setParentId", Long.class).invoke(command, -1L);
            } else {
                String parentName = nameResolver.apply(originalParentId);
                Long newParentId = nameToIdMap.getOrDefault(parentName, -1L);
                command.getClass().getMethod("setParentId", Long.class).invoke(command, newParentId);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error setting parent ID", e);
        }
    }

    private String getTagNameById(List<TagTemplateBo> tags, Long id) {
        return tags.stream()
                .filter(t -> Objects.equals(t.getTagId(), id))
                .findFirst()
                .map(TagTemplateBo::getTagName)
                .orElse(null);
    }

    private String getCategoryNameById(List<CategoryTemplateBo> categories, Long id) {
        return categories.stream()
                .filter(c -> Objects.equals(c.getCategoryId(), id))
                .findFirst()
                .map(CategoryTemplateBo::getCategoryName)
                .orElse(null);
    }

    public void modify(FortuneBookModifyCommand bookModifyCommand) {
        FortuneBookModel fortuneBookModel = fortuneBookFactory.loadById(bookModifyCommand.getBookId());
        fortuneBookModel.loadModifyCommand(bookModifyCommand);
        fortuneBookModel.checkNotInRecycleBin();
        fortuneBookModel.updateById();
    }

    @Transactional(rollbackFor = Exception.class)
    public void remove(Long bookId) {
        FortuneBookModel fortuneBookModel = fortuneBookFactory.loadById(bookId);
        fortuneBookModel.deleteById();
        fortuneTagRepo.removeByBookId(bookId);
        fortuneCategoryRepo.removeByBookId(bookId);
        fortunePayeeRepo.removeByBookId(bookId);
        fortuneBillService.removeByBookId(bookId);
    }

    public void moveToRecycleBin(Long bookId) {
        FortuneBookModel fortuneBookModel = fortuneBookFactory.loadById(bookId);
        fortuneBookModel.checkRemoveDefault(fortuneGroupFactory.loadById(fortuneBookModel.getGroupId()));
        fortuneBookModel.setRecycleBin(Boolean.TRUE);
        fortuneBookModel.updateById();
    }

    public void putBack(Long bookId) {
        FortuneBookModel fortuneBookModel = fortuneBookFactory.loadById(bookId);
        fortuneBookModel.setRecycleBin(Boolean.FALSE);
        fortuneBookModel.updateById();
    }

    public List<FortuneBookEntity> getByGroupId(Long groupId) {
        return fortuneBookRepo.getByGroupId(groupId);
    }

    public void enable(Long bookId) {
        FortuneBookModel fortuneBookModel = fortuneBookFactory.loadById(bookId);
        fortuneBookModel.setEnable(Boolean.TRUE);
        fortuneBookModel.updateById();
    }

    public void disable(Long bookId) {
        FortuneBookModel fortuneBookModel = fortuneBookFactory.loadById(bookId);
        FortuneGroupModel fortuneGroupModel = fortuneGroupFactory.loadById(fortuneBookModel.getGroupId());
        fortuneBookModel.checkDisableDefault(fortuneGroupModel);
        fortuneBookModel.setEnable(Boolean.FALSE);
        fortuneBookModel.updateById();
    }

    @Transactional(rollbackFor = Exception.class)
    public void removeByGroupId(Long groupId) {
        List<FortuneBookEntity> bookList = this.getByGroupId(groupId);
        List<Long> bookIds = bookList.stream().map(FortuneBookEntity::getBookId).toList();
        fortuneBookRepo.removeBatchByIds(bookIds);
        fortuneTagRepo.removeByBookIds(bookIds);
        fortuneCategoryRepo.removeByBookIds(bookIds);
        fortunePayeeRepo.removeByBookIds(bookIds);
        fortuneBillService.removeByBookIds(bookIds);
    }

    public FortuneBookEntity getBookById(Long bookId) {
        return fortuneBookRepo.getById(bookId);
    }

}
