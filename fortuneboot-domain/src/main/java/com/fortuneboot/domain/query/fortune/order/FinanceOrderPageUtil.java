package com.fortuneboot.domain.query.fortune.order;

import java.util.ArrayList;
import java.util.List;

/**
 * 单据分页参数工具。
 * <p>分页默认值与上限只在此处定义，供各查询对象、Repository和Service复用。
 *
 * @author work.chi.zhang@gmail.com
 * @date 2026/10/9
 */
public final class FinanceOrderPageUtil {

    public static final int DEFAULT_PAGE_NUM = 1;
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 100;

    private FinanceOrderPageUtil() {
    }

    public static int pageNum(Integer pageNum) {
        return pageNum == null || pageNum < DEFAULT_PAGE_NUM ? DEFAULT_PAGE_NUM : pageNum;
    }

    public static int pageSize(Integer pageSize) {
        return pageSize == null || pageSize < 1 ? DEFAULT_PAGE_SIZE : Math.min(pageSize, MAX_PAGE_SIZE);
    }

    /**
     * 内存分页，用于必须先做业务过滤的列表。
     */
    public static <T> List<T> paginate(List<T> rows, Integer pageNum, Integer pageSize) {
        int from = (pageNum(pageNum) - 1) * pageSize(pageSize);
        if (from >= rows.size()) {
            return List.of();
        }
        int to = Math.min(from + pageSize(pageSize), rows.size());
        return new ArrayList<>(rows.subList(from, to));
    }
}
