package com.github.xiao.dynamicds;

import com.github.xiao.dynamicds.connection.ConnectionFactory;
import com.github.xiao.dynamicds.connection.ConnectionProxy;
import com.github.xiao.dynamicds.util.DynamicDataSourceCtxHolder;
import com.github.xiao.dynamicds.util.TransactionCtxHolder;
import org.springframework.jdbc.datasource.AbstractDataSource;
import org.springframework.util.StringUtils;

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


    @Override
    public Connection getConnection() throws SQLException {
        String xid = TransactionCtxHolder.getXid();
        if (StringUtils.isEmpty(xid)) {
            return determineDataSource().getConnection();
        }else {
            String dsKey = DynamicDataSourceCtxHolder.peek();
            dsKey = StringUtils.isEmpty(dsKey) ? getPrimaryDsKey(): dsKey;
            ConnectionProxy connection = ConnectionFactory.getConnection(xid, dsKey);
            return connection == null ? getConnectionProxy(xid, dsKey, determineDataSource().getConnection()) : connection;

        }
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        String xid = TransactionCtxHolder.getXid();
        if (StringUtils.isEmpty(xid)) {
            return determineDataSource().getConnection(username, password);
        } else {
            String ds = DynamicDataSourceCtxHolder.peek();
            ds = StringUtils.isEmpty(ds) ? getPrimaryDsKey() : ds;
            ConnectionProxy connection = ConnectionFactory.getConnection(xid, ds);
            return connection == null ? getConnectionProxy(xid, ds, determineDataSource().getConnection(username, password))
                    : connection;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T unwrap(Class<T> iface) throws SQLException {
        if (iface.isInstance(this)) {
            return (T) this;
        }
        return determineDataSource().unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return (iface.isInstance(this) || determineDataSource().isWrapperFor(iface));
    }


    /**
     *  获取主数据源
     * @return String
     */
    protected abstract String getPrimaryDsKey();


    private Connection getConnectionProxy(String xid, String ds, Connection connection) {
        ConnectionProxy connectionProxy = new ConnectionProxy(connection, ds);
        ConnectionFactory.putConnection(xid, ds, connectionProxy);
        return connectionProxy;
    }
}
