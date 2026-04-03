package com.fortuneboot.infrastructure.handler;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.*;

/**
 * SQLite BLOB 类型处理器
 * <p>
 * SQLite JDBC 驱动不支持 {@link ResultSet#getBlob(String)} 方法，
 * 默认的 BlobTypeHandler 会抛出 SQLFeatureNotSupportedException。
 * <p>
 * 本处理器改用 {@link ResultSet#getBytes(String)} 进行读取，
 * 写入时使用 {@link PreparedStatement#setBytes(int, byte[])}，确保 SQLite 兼容。
 *
 * @author fortuneboot
 */
public class SqliteBlobTypeHandler extends BaseTypeHandler<byte[]> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, byte[] parameter, JdbcType jdbcType)
            throws SQLException {
        ps.setBytes(i, parameter);
    }

    @Override
    public byte[] getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return rs.getBytes(columnName);
    }

    @Override
    public byte[] getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return rs.getBytes(columnIndex);
    }

    @Override
    public byte[] getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return cs.getBytes(columnIndex);
    }
}