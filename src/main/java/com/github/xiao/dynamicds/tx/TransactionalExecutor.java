package com.github.xiao.dynamicds.tx;

/**
 * 事务执行器
 * @author wang xiao
 * date 2023/8/15
 */
public interface TransactionalExecutor {

    /**
     * 执行
     *
     * @return object
     * @throws Throwable Throwable
     */
    Object execute() throws Throwable;

    /**
     * 获取事务信息
     *
     * @return TransactionalInfo
     */
    TransactionalInfo getTransactionInfo();
}