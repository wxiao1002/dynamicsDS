package com.github.xiao.dynamicds.aop;

import com.github.xiao.dynamicds.util.DynamicDataSourceCtxHolder;
import com.github.xiao.dynamicds.annotation.DS;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import java.lang.reflect.Method;

/**
 * @author wang xiao
 * date 2023/8/15
 */
public class DsAnnotationInterceptor implements MethodInterceptor {

    @Override
    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        Method method = methodInvocation.getMethod();
        final DS ds = method.getAnnotation(DS.class);
        DynamicDataSourceCtxHolder.push(ds.value());
        try {
            return methodInvocation.proceed();
        } finally {
            DynamicDataSourceCtxHolder.poll();
        }
    }
}
