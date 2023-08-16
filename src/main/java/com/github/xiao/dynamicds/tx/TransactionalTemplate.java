
package com.github.xiao.dynamicds.tx;


import com.github.xiao.dynamicds.connection.ConnectionFactory;
import com.github.xiao.dynamicds.util.TransactionCtxHolder;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;
import java.util.Objects;
import java.util.UUID;

/**
 * 事务模板
 * @author wang xiao
 * date 2023/8/15
 */
public class TransactionalTemplate {


    private static final ThreadLocal<SecureRandom> SECURE_RANDOM_HOLDER = new ThreadLocal<SecureRandom>() {
        @Override
        protected SecureRandom initialValue() {
            return new SecureRandom();
        }
    };

    /**
     * Execute with transaction.
     *
     * @param transactionalExecutor TransactionalExecutor
     * @return Object
     * @throws Throwable Throwable
     */
    public Object execute(TransactionalExecutor transactionalExecutor) throws Throwable {
        TransactionalInfo transactionInfo = transactionalExecutor.getTransactionInfo();
        DsPropagation propagation = transactionInfo.propagation;
        SuspendedResourcesHolder suspendedResourcesHolder = null;
        try {
            switch (propagation) {
                case NOT_SUPPORTED:
                    // If transaction is existing, suspend it.
                    if (existingTransaction()) {
                        suspendedResourcesHolder = suspend();
                    }
                    return transactionalExecutor.execute();
                case REQUIRES_NEW:
                    // If transaction is existing, suspend it, and then begin new transaction.
                    if (existingTransaction()) {
                        suspendedResourcesHolder = suspend();
                    }
                    // Continue and execute with new transaction
                    break;
                case SUPPORTS:
                    // If transaction is not existing, execute without transaction.
                    if (!existingTransaction()) {
                        return transactionalExecutor.execute();
                    }
                    // Continue and execute with new transaction
                    break;
                case REQUIRED:
                    // default
                    break;
                case NEVER:
                    // If transaction is existing, throw exception.
                    if (existingTransaction()) {
                        throw new TransactionException("Existing transaction found for transaction marked with propagation never");
                    } else {
                        // Execute without transaction and return.
                        return transactionalExecutor.execute();
                    }
                case MANDATORY:
                    // If transaction is not existing, throw exception.
                    if (!existingTransaction()) {
                        throw new TransactionException("No existing transaction found for transaction marked with propagation 'mandatory'");
                    }
                    // Continue and execute with current transaction.
                    break;
                case NESTED:
                    // If transaction is existing,Open a save point for child transaction rollback.
                    if (existingTransaction()) {
                        // todo
                    }
                    // Continue and execute with current transaction.
                    break;
                default:
                    throw new TransactionException("Not Supported Propagation:" + propagation);
            }
            return doExecute(transactionalExecutor);
        } finally {
            resume(suspendedResourcesHolder);
        }
    }

    /**
     * 判断是否存在事务
     *
     * @param transactionalExecutor TransactionalExecutor
     * @return 是否存在事务
     * @throws Throwable Throwable
     */
    private Object doExecute(TransactionalExecutor transactionalExecutor) throws Throwable {
        TransactionalInfo transactionInfo = transactionalExecutor.getTransactionInfo();
        DsPropagation propagation = transactionInfo.propagation;
        if (!StringUtils.isEmpty(TransactionCtxHolder.getXid()) && !propagation.equals(DsPropagation.NESTED)) {
            return transactionalExecutor.execute();
        }
        boolean state = true;
        Object o;
        String xid = startTransaction();
        try {
            o = transactionalExecutor.execute();
        } catch (Exception e) {
            state = !isRollback(e, transactionInfo);
            throw e;
        } finally {
            if (state) {
                commit(xid);
            } else {
                rollback(xid);
            }
        }
        return o;
    }

    /**
     * 判断是否回滚
     *
     * @param e               异常
     * @param transactionInfo 事务信息
     * @return 是否回滚
     */
    private boolean isRollback(Throwable e, TransactionalInfo transactionInfo) {
        boolean isRollback = true;
        Class<? extends Throwable>[] rollbacks = transactionInfo.rollbackFor;
        Class<? extends Throwable>[] noRollbackFor = transactionInfo.noRollbackFor;
        if (isNotEmpty(noRollbackFor)) {
            for (Class<? extends Throwable> noRollBack : noRollbackFor) {
                int depth = getDepth(e.getClass(), noRollBack);
                if (depth >= 0) {
                    return false;
                }
            }
        }
        if (isNotEmpty(rollbacks)) {
            for (Class<? extends Throwable> rollback : rollbacks) {
                int depth = getDepth(e.getClass(), rollback);
                if (depth >= 0) {
                    return isRollback;
                }
            }
        }
        return false;
    }

    /**
     * 获取深度
     *
     * @param exceptionClass 异常类
     * @param rollback       回滚类
     * @return 深度
     */
    private int getDepth(Class<?> exceptionClass, Class<? extends Throwable> rollback) {
        if (rollback == Throwable.class || rollback == Exception.class) {
            return 0;
        }
        // If we've gone as far as we can go and haven't found it...
        if (exceptionClass == Throwable.class) {
            return -1;
        }
        if (Objects.equals(exceptionClass, rollback)) {
            return 0;
        }
        return getDepth(exceptionClass.getSuperclass(), rollback);
    }

    private void resume(SuspendedResourcesHolder suspendedResourcesHolder) {
        if (suspendedResourcesHolder != null) {
            String xid = suspendedResourcesHolder.getXid();
            TransactionCtxHolder.bind(xid);
        }
    }

    /**
     * 挂起资源
     *
     * @return 挂起资源
     */
    public SuspendedResourcesHolder suspend() {
        String xid = TransactionCtxHolder.getXid();
        if (xid != null) {
            TransactionCtxHolder.unbind(xid);
            return new SuspendedResourcesHolder(xid);
        } else {
            return null;
        }
    }

    /**
     * 判断是否存在事务
     *
     * @return 是否存在事务
     */
    public boolean existingTransaction() {
        return !StringUtils.isEmpty(TransactionCtxHolder.getXid());
    }

    /**
     * 判断数据是否为空
     *
     * @param array 长度
     * @return 数组对象为null或者长度为 0 时，返回 false
     */
    public boolean isEmpty(Object[] array) {
        return array == null || array.length == 0;
    }

    /**
     * 判断数组是否不为空
     *
     * @param array 数组
     * @return 数组对象内含有任意对象时返回 true
     */
    public boolean isNotEmpty(Object[] array) {
        return !isEmpty(array);
    }


    /**
     * 手动开启事务
     *
     * @return 事务ID
     */
    public static String startTransaction() {
        String xid = TransactionCtxHolder.getXid();
        if (StringUtils.isEmpty(xid)) {
            xid = randomUUID().toString();
            TransactionCtxHolder.bind(xid);
        }
        return xid;
    }

    /**
     * 手动提交事务
     *
     * @param xid 事务ID
     */
    public static void commit(String xid) throws Exception {
        try {
            ConnectionFactory.commit(xid);
        } finally {
            TransactionCtxHolder.remove();
        }
    }

    /**
     * 手动回滚事务
     *
     * @param xid 事务ID
     */
    public static void rollback(String xid) throws Exception {
        try {
            ConnectionFactory.rollback(xid);
        } finally {
            TransactionCtxHolder.remove();
        }
    }


    /**
     * 随机生成UUID
     *
     * @return UUID
     */
    public static UUID randomUUID() {
        SecureRandom ng = SECURE_RANDOM_HOLDER.get();
        byte[] randomBytes = new byte[16];
        ng.nextBytes(randomBytes);
        // clear version
        randomBytes[6] &= 0x0f;
        // set to version 4
        randomBytes[6] |= 0x40;
        // clear variant
        randomBytes[8] &= 0x3f;
        // set to IETF variant
        randomBytes[8] |= (byte) 0x80;
        long msb = 0;
        long lsb = 0;
        for (int i = 0; i < 8; i++) {
            msb = (msb << 8) | (randomBytes[i] & 0xff);
        }
        for (int i = 8; i < 16; i++) {
            lsb = (lsb << 8) | (randomBytes[i] & 0xff);
        }
        return new UUID(msb, lsb);
    }



}