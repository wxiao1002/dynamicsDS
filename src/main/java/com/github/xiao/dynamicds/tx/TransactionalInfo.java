package com.github.xiao.dynamicds.tx;


/**
 * 事务基础信息
 * @author wang xiao
 * date 2023/8/15
 */
public class TransactionalInfo {

    /**
     * 回滚异常
     */
    Class<? extends Throwable>[] rollbackFor;

    /**
     * 不回滚异常
     */
    Class<? extends Throwable>[] noRollbackFor;

    /**
     * 事务传播行为
     */
    DsPropagation propagation;

    public Class<? extends Throwable>[] getRollbackFor() {
        return rollbackFor;
    }

    public void setRollbackFor(Class<? extends Throwable>[] rollbackFor) {
        this.rollbackFor = rollbackFor;
    }

    public Class<? extends Throwable>[] getNoRollbackFor() {
        return noRollbackFor;
    }

    public void setNoRollbackFor(Class<? extends Throwable>[] noRollbackFor) {
        this.noRollbackFor = noRollbackFor;
    }

    public DsPropagation getPropagation() {
        return propagation;
    }

    public void setPropagation(DsPropagation propagation) {
        this.propagation = propagation;
    }
}