package com.fortuneboot.infrastructure.handler;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * SQLite 日期类型处理器
 * <p>
 * SQLite 没有原生 DATETIME 类型，默认的 DateTypeHandler 会通过 setTimestamp/getTimestamp
 * 导致日期被存储为 epoch 毫秒数，读取时又无法解析。
 * <p>
 * 本处理器统一使用 'yyyy-MM-dd HH:mm:ss' 字符串格式进行读写，确保 SQLite 兼容。
 *
 * @author fortuneboot
 */
public class SqliteDateTypeHandler extends BaseTypeHandler<Date> {

    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    private final ThreadLocal<SimpleDateFormat> formatter =
            ThreadLocal.withInitial(() -> new SimpleDateFormat(DATE_FORMAT));

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Date parameter, JdbcType jdbcType)
            throws SQLException {
        ps.setString(i, formatter.get().format(parameter));
    }

    @Override
    public Date getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return parseDate(rs.getString(columnName));
    }

    @Override
    public Date getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return parseDate(rs.getString(columnIndex));
    }

    @Override
    public Date getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return parseDate(cs.getString(columnIndex));
    }

    private Date parseDate(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            // 先尝试作为日期字符串解析
            return formatter.get().parse(value);
        } catch (Exception e) {
            try {
                // 兼容已有的 epoch 毫秒数
                return new Date(Long.parseLong(value));
            } catch (NumberFormatException ex) {
                return null;
            }
        }
    }
}