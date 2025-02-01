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
        FortuneBookModel fortuneBookModel = fortuneBookFactory.create();
        fortuneBookModel.loadAddCommand(bookAddCommand);
        fortuneBookModel.setEnable(Boolean.TRUE);
        fortuneBookModel.insert();
        List<BookTemplateBo> bookTemplateBoList = applicationScopeBo.getBookTemplateBoList();
        Optional<BookTemplateBo> templateBo = bookTemplateBoList.stream().filter(item -> Objects.equals(item.getBookTemplateId(), bookAddCommand.getBookTemplate())).findAny();
        if (templateBo.isEmpty()) {
            return fortuneBookModel;
        }
        BookTemplateBo bookTemplateBo = templateBo.get();
        List<TagTemplateBo> tagList = bookTemplateBo.getTagList();
        Map<Long, String> tagMap = tagList.stream().collect(Collectors.toMap(TagTemplateBo::getTagId, TagTemplateBo::getTagName));
        Map<String, Long> map = new HashMap<>();
        for (TagTemplateBo tagTemplateBo : tagList) {
            FortuneTagAddCommand tagCommand = new FortuneTagAddCommand();
            BeanUtil.copyProperties(tagTemplateBo, tagCommand);
            tagCommand.setEnable(Boolean.TRUE);
            tagCommand.setBookId(fortuneBookModel.getBookId());
            if (Objects.isNull(tagCommand.getParentId())) {
                tagCommand.setParentId(-1L);
            } else {
                tagCommand.setParentId(map.get(tagMap.get(tagCommand.getParentId())));
            }
            FortuneTagModel tagModel = fortuneTagService.add(tagCommand);
            map.put(tagCommand.getTagName(), tagModel.getTagId());
        }
        List<CategoryTemplateBo> categoryList = bookTemplateBo.getCategoryList();
        Map<Long, String> categoryMap = categoryList.stream().collect(Collectors.toMap(CategoryTemplateBo::getCategoryId, CategoryTemplateBo::getCategoryName));
        for (CategoryTemplateBo categoryTemplateBo : categoryList) {
            FortuneCategoryAddCommand categoryCommand = new FortuneCategoryAddCommand();
            BeanUtil.copyProperties(categoryTemplateBo, categoryCommand);
            categoryCommand.setEnable(Boolean.TRUE);
            categoryCommand.setBookId(fortuneBookModel.getBookId());
            if (Objects.isNull(categoryCommand.getParentId())) {
                categoryCommand.setParentId(-1L);
            } else {
                categoryCommand.setParentId(map.get(categoryMap.get(categoryCommand.getParentId())));
            }
            FortuneCategoryModel categoryModel = fortuneCategoryService.add(categoryCommand);
            map.put(categoryModel.getCategoryName(), categoryModel.getCategoryId());
        }
        List<PayeeTemplateBo> payeeList = bookTemplateBo.getPayeeList();
        for (PayeeTemplateBo payeeTemplateBo : payeeList) {
            FortunePayeeAddCommand payeeCommand = new FortunePayeeAddCommand();
            BeanUtil.copyProperties(payeeTemplateBo, payeeCommand);
            payeeCommand.setEnable(Boolean.TRUE);
            payeeCommand.setBookId(fortuneBookModel.getBookId());
            fortunePayeeService.add(payeeCommand);
        }
        if (StringUtils.isBlank(fortuneBookModel.getRemark())) {
            fortuneBookModel.setRemark(bookTemplateBo.getRemark());
            fortuneBookModel.updateById();
        }
        return fortuneBookModel;
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
