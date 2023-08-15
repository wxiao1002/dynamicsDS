
package com.github.xiao.dynamicds.tx;


import com.github.xiao.dynamicds.connection.ConnectionFactory;
import com.github.xiao.dynamicds.util.TransactionCtxHolder;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;
import java.util.UUID;

/**
 * 本地事务工具类
 *
 * @author wang xiao
 * date 2023/8/15
 */

public final class LocalTxUtil {

    /**
     * SecureRandom instance used to generate UUIDs.
     */
    private static final ThreadLocal<SecureRandom> SECURE_RANDOM_HOLDER = new ThreadLocal<SecureRandom>() {
        @Override
        protected SecureRandom initialValue() {
            return new SecureRandom();
        }
    };

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
        randomBytes[8] |= 0x80;
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
}