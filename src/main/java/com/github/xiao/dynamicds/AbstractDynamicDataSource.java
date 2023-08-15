package com.github.xiao.dynamicds;

import org.springframework.jdbc.datasource.AbstractDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 *  抽象动态数据源
 * @author wang xiao
 * date 2023/8/15
 */
public abstract class AbstractDynamicDataSource extends AbstractDataSource {


    /**
     * 获取数据源
     * @return DataSource
     */
    protected abstract DataSource determineDataSource();

    /**
     * 获取默认数据源名称
     *
     * @return 名称
     */
    protected abstract String getPrimary();

    @Override
    public Connection getConnection() throws SQLException {

    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return null;
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return super.unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return super.isWrapperFor(iface);
    }
}
