package com.github.xiao.dynamicds.annotation;

import com.github.xiao.dynamicds.tx.DsPropagation;

/**
 * @author wang xiao
 * date 2023/8/15
 */
public @interface DSTransactional {


    /**
     * 回滚异常
     *
     * @return Class[]
     */
    Class<? extends Throwable>[] rollbackFor() default {Exception.class};

    /**
     * 不回滚异常
     *
     * @return Class[]
     */
    Class<? extends Throwable>[] noRollbackFor() default {};

    /**
     * 事务传播行为
     *
     * @return DsPropagation
     */
    DsPropagation propagation() default DsPropagation.REQUIRED;
}
