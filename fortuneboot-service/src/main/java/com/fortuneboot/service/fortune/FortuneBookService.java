package com.fortuneboot.service.fortune;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fortuneboot.domain.bo.fortune.*;
import com.fortuneboot.domain.command.fortune.*;
import com.fortuneboot.domain.entity.fortune.FortuneBookEntity;
import com.fortuneboot.domain.query.fortune.FortuneBookQuery;
import com.fortuneboot.factory.fortune.FortuneBookFactory;
import com.fortuneboot.factory.fortune.FortuneGroupFactory;
import com.fortuneboot.factory.fortune.model.FortuneBookModel;
import com.fortuneboot.factory.fortune.model.FortuneCategoryModel;
import com.fortuneboot.factory.fortune.model.FortuneTagModel;
import com.fortuneboot.repository.fortune.FortuneBookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    private final FortuneBookRepository fortuneBookRepository;

    private final FortuneGroupFactory fortuneGroupFactory;

    private final ApplicationScopeBo applicationScopeBo;

    private final FortuneTagService fortuneTagService;

    private final FortuneCategoryService fortuneCategoryService;

    private final FortunePayeeService fortunePayeeService;

    public IPage<FortuneBookEntity> getPage(FortuneBookQuery query) {
        return fortuneBookRepository.page(query.toPage(), query.addQueryCondition());
    }
    @Transactional(rollbackFor = Exception.class)
    public FortuneBookModel add(FortuneBookAddCommand bookAddCommand) {
        // 创建账本模型并插入
        FortuneBookModel fortuneBookModel = fortuneBookFactory.create();
        fortuneBookModel.loadAddCommand(bookAddCommand);
        fortuneBookModel.setEnable(Boolean.TRUE);

        // 获取模板并提前处理备注
        BookTemplateBo bookTemplateBo = getBookTemplate(bookAddCommand);
        if (bookTemplateBo != null && StringUtils.isBlank(fortuneBookModel.getRemark())) {
            fortuneBookModel.setRemark(bookTemplateBo.getRemark());
        }
        fortuneBookModel.insert();

        // 处理模板数据（当模板存在时）
        if (bookTemplateBo != null) {
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
        sortByHierarchy(tagTemplates, TagTemplateBo::getParentId).forEach(template -> {
            FortuneTagAddCommand command = new FortuneTagAddCommand();
            BeanUtil.copyProperties(template, command);
            command.setEnable(true);
            command.setBookId(book.getBookId());
            resolveParentId(command, template.getParentId(), nameToIdMap,
                    id -> getTagNameById(tagTemplates, id));

            FortuneTagModel model = fortuneTagService.add(command);
            nameToIdMap.put(template.getTagName(), model.getTagId());
        });
    }

    private void processCategoryTemplates(FortuneBookModel book, List<CategoryTemplateBo> categoryTemplates) {
        Map<String, Long> nameToIdMap = new HashMap<>();
        sortByHierarchy(categoryTemplates, CategoryTemplateBo::getParentId).forEach(template -> {
            FortuneCategoryAddCommand command = new FortuneCategoryAddCommand();
            BeanUtil.copyProperties(template, command);
            command.setEnable(true);
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
            command.setEnable(true);
            command.setBookId(book.getBookId());
            fortunePayeeService.add(command);
        });
    }

    private <T> void resolveParentId(Object command, Long originalParentId,
                                     Map<String, Long> nameToIdMap,
                                     Function<Long, String> nameResolver) {
        try {
            if (originalParentId == null) {
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

    private <T> List<T> sortByHierarchy(List<T> items, Function<T, Long> parentIdExtractor) {
        Map<Long, List<T>> parentIdToChildren = new HashMap<>();
        List<T> roots = new ArrayList<>();

        // 构建父子关系映射
        Map<Long, T> idMap = items.stream().collect(Collectors.toMap(
                item -> parentIdExtractor.apply(item) != null ? ((TagTemplateBo)item).getTagId() : ((CategoryTemplateBo)item).getCategoryId(),
                Function.identity()
        ));

        for (T item : items) {
            Long parentId = parentIdExtractor.apply(item);
            if (parentId == null || !idMap.containsKey(parentId)) {
                roots.add(item);
            } else {
                parentIdToChildren.computeIfAbsent(parentId, k -> new ArrayList<>()).add(item);
            }
        }

        // 按层级排序
        List<T> sorted = new ArrayList<>();
        Queue<T> queue = new LinkedList<>(roots);
        while (!queue.isEmpty()) {
            T current = queue.poll();
            sorted.add(current);
            Long currentId = current instanceof TagTemplateBo
                    ? ((TagTemplateBo) current).getTagId()
                    : ((CategoryTemplateBo) current).getCategoryId();
            parentIdToChildren.getOrDefault(currentId, Collections.emptyList())
                    .forEach(queue::offer);
        }
        return sorted;
    }

    public void modify(FortuneBookModifyCommand bookModifyCommand) {
        FortuneBookModel fortuneBookModel = fortuneBookFactory.loadById(bookModifyCommand.getBookId());
        fortuneBookModel.loadModifyCommand(bookModifyCommand);
        fortuneBookModel.checkNotInRecycleBin();
        fortuneBookModel.updateById();
    }

    public void remove(Long groupId, Long bookId) {
        FortuneBookModel fortuneBookModel = fortuneBookFactory.loadById(bookId);
        fortuneBookModel.checkGroupId(groupId);
        fortuneBookModel.deleteById();
    }

    public void moveToRecycleBin(Long groupId, Long bookId) {
        FortuneBookModel fortuneBookModel = fortuneBookFactory.loadById(bookId);
        fortuneBookModel.checkGroupId(groupId);
        fortuneBookModel.checkDefault(fortuneGroupFactory.loadById(groupId));
        fortuneBookModel.setRecycleBin(Boolean.TRUE);
        fortuneBookModel.updateById();
    }

    public void putBack(Long groupId, Long bookId) {
        FortuneBookModel fortuneBookModel = fortuneBookFactory.loadById(bookId);
        fortuneBookModel.checkGroupId(groupId);
        fortuneBookModel.setRecycleBin(Boolean.FALSE);
        fortuneBookModel.updateById();
    }

    public List<FortuneBookEntity> getByIds(List<Long> bookIdList) {
        return fortuneBookRepository.listByIds(bookIdList);
    }

    public List<FortuneBookEntity> getByGroupId(Long groupId) {
        return fortuneBookRepository.getByGroupId(groupId);
    }
}
