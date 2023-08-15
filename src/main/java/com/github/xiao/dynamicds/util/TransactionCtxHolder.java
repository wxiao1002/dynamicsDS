package com.github.xiao.dynamicds.util;

import org.springframework.util.StringUtils;

/**
 * 事务id
 * @author wang xiao
 * date 2023/8/15
 */
public class TransactionCtxHolder {
    private static final ThreadLocal<String> CONTEXT_HOLDER = new ThreadLocal<>();

    public static Boolean isOpenTx(){
        return !StringUtils.isEmpty(getXid());
    }

    /**
     * 获取事务id
     * @return String
     */
    public static String getXid() {
        String xid = CONTEXT_HOLDER.get();
        if (!StringUtils.isEmpty(xid)) {
            return xid;
        }
        return null;
    }

    /**
     *  取消绑定 xid
     * @param xid 事务ID
     * @return  string
     */
    public static String unbind(String xid) {
        CONTEXT_HOLDER.remove();
        return xid;
    }

    /**
     * 绑定 xid
     * @param xid 事务ID
     * @return  string
     */
    public static String bind(String xid) {
        CONTEXT_HOLDER.set(xid);
        return xid;
    }

    /**
     * 清空
     */
    public static void remove() {
        CONTEXT_HOLDER.remove();
    }

}
