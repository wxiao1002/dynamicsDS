package com.github.xiao.dynamicds.aop;

import com.github.xiao.dynamicds.annotation.DSTransactional;
import com.github.xiao.dynamicds.tx.TransactionalExecutor;
import com.github.xiao.dynamicds.tx.TransactionalInfo;
import com.github.xiao.dynamicds.tx.TransactionalTemplate;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import java.lang.reflect.Method;

/**
 * @author wang xiao
 * date 2023/8/15
 */
public class DynamicDataSourceTransactionInterceptor implements MethodInterceptor {

    private final TransactionalTemplate transactionalTemplate = new TransactionalTemplate();

    @Override
    public Object invoke(final MethodInvocation methodInvocation) throws Throwable {
        Method method = methodInvocation.getMethod();
        final DSTransactional dsTransactional = method.getAnnotation(DSTransactional.class);

        TransactionalExecutor transactionalExecutor = new TransactionalExecutor() {
            @Override
            public Object execute() throws Throwable {
                return methodInvocation.proceed();
            }

            @Override
            public TransactionalInfo getTransactionInfo() {
                TransactionalInfo transactionInfo = new TransactionalInfo();
                transactionInfo.setPropagation(dsTransactional.propagation());
                transactionInfo.setNoRollbackFor(dsTransactional.noRollbackFor());
                transactionInfo.setRollbackFor(dsTransactional.rollbackFor());
                return transactionInfo;
            }
        };
        return transactionalTemplate.execute(transactionalExecutor);
    }
}
