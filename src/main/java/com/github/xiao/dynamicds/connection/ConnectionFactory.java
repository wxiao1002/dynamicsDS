package com.github.xiao.dynamicds.connection;

import org.springframework.util.CollectionUtils;

import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author wang xiao
 * date 2023/8/15
 */
public class ConnectionFactory {

    /**
     * connection holder
     */
    private static final ThreadLocal<Map<String, Map<String, ConnectionProxy>>> CONNECTION_HOLDER =
            new ThreadLocal<Map<String, Map<String, ConnectionProxy>>>() {
                @Override
                protected Map<String, Map<String, ConnectionProxy>> initialValue() {
                    return new ConcurrentHashMap<>();
                }
            };


    /**
     * put connection
     *
     * @param xid        xid
     * @param ds         ds
     * @param connection connection
     */
    public static void putConnection(String xid, String ds, ConnectionProxy connection) {
        Map<String, Map<String, ConnectionProxy>> concurrentHashMap = CONNECTION_HOLDER.get();
        Map<String, ConnectionProxy> connectionProxyMap = concurrentHashMap.get(xid);
        if (connectionProxyMap == null) {
            connectionProxyMap = new ConcurrentHashMap<>();
            concurrentHashMap.put(xid, connectionProxyMap);
        }
        if (!connectionProxyMap.containsKey(ds)) {
            try {
                connection.setAutoCommit(false);
            } catch (SQLException ignored) {
            }
            connectionProxyMap.put(ds, connection);
        }
    }

    /**
     * getConnection
     *
     * @param xid 事务ID
     * @param ds  ds
     * @return boolean
     */
    public static ConnectionProxy getConnection(String xid, String ds) {
        Map<String, Map<String, ConnectionProxy>> concurrentHashMap = CONNECTION_HOLDER.get();
        Map<String, ConnectionProxy> connectionProxyMap = concurrentHashMap.get(xid);
        if (CollectionUtils.isEmpty(connectionProxyMap)) {
            return null;
        }
        return connectionProxyMap.get(ds);
    }

    /**
     * 提交
     * @param xid
     */
    public static void commit(String xid) throws Exception {
        notify(xid,true);
    }

    /**
     * 回滚
     * @param xid
     */
    public static void rollback(String xid) throws Exception {
        notify(xid,true);
    }

    /**
     * Whether there is a savepoint
     *
     * @param xid   xid
     * @param state state
     * @throws Exception Exception
     */
    private static void notify(String xid, Boolean state) throws Exception {
        Exception exception = null;
        Map<String, Map<String, ConnectionProxy>> concurrentHashMap = CONNECTION_HOLDER.get();
        if (CollectionUtils.isEmpty(concurrentHashMap)) {
            return;
        }
        Map<String, ConnectionProxy> connectionProxyMap = concurrentHashMap.get(xid);
        try {
            for (ConnectionProxy connectionProxy : connectionProxyMap.values()) {
                try {
                    if (connectionProxy != null) {
                        connectionProxy.notify(state);
                    }
                } catch (SQLException e) {
                    exception = e;
                }

            }
        } finally {

            if (exception != null) {
                throw exception;
            }
        }
    }

}
