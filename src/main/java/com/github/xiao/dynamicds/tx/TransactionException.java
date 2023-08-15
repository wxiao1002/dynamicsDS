package com.github.xiao.dynamicds.tx;

/**
 * 事务异常
 * @author wang xiao
 * date 2023/8/15
 */
public class TransactionException extends RuntimeException {
    /**
     * 构造
     *
     * @param message 消息
     */
    public TransactionException(String message) {
        super(message);
    }

    /**
     * 构造
     *
     * @param message 消息
     * @param cause   异常
     */
    public TransactionException(String message, Throwable cause) {
        super(message, cause);
    }
}