package com.github.xiao.dynamicds.util;

import org.springframework.core.NamedThreadLocal;
import org.springframework.util.StringUtils;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * 数据源切换工具类
 * @author wang xiao
 * date 2023/8/15
 */
public final class DynamicDataSourceCtxHolder {

    private DynamicDataSourceCtxHolder() {
    }


    /**
     * 服务嵌套问题解决
     */
    public static final ThreadLocal<Deque<String>> LOOKUP_KEY_HOLDER = new NamedThreadLocal<Deque<String>>("dynamic-datasource"){
        @Override
        protected Deque<String> initialValue() {
            return new ArrayDeque<>();
        }
    };


    /**
     * 获得当前线程数据源
     * @return 数据源名称
     */
    public static String peek() {
        return LOOKUP_KEY_HOLDER.get().peek();
    }

    /**
     * 设置当前线程数据源
     * @param ds 数据源名称
     * @return 数据源名称
     */
    public static String push(String ds) {
        String dataSourceStr = StringUtils.isEmpty(ds) ? "" : ds;
        LOOKUP_KEY_HOLDER.get().push(dataSourceStr);
        return dataSourceStr;
    }

    /**
     * 清空当前线程数据源
     */
    public static void poll() {
        Deque<String> deque = LOOKUP_KEY_HOLDER.get();
        deque.poll();
        if (deque.isEmpty()) {
            LOOKUP_KEY_HOLDER.remove();
        }
    }

    /**
     * 清空
     */
    public static void remove() {
        LOOKUP_KEY_HOLDER.remove();
    }

}
