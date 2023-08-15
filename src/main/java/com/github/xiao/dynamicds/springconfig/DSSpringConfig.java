package com.github.xiao.dynamicds.springconfig;

import com.github.xiao.dynamicds.annotation.DS;
import com.github.xiao.dynamicds.annotation.DSTransactional;
import com.github.xiao.dynamicds.aop.DsAnnotationInterceptor;
import com.github.xiao.dynamicds.aop.DynamicDataSourceAnnotationAdvisor;
import com.github.xiao.dynamicds.aop.DynamicDataSourceTransactionInterceptor;
import org.springframework.aop.Advisor;
import org.springframework.context.annotation.Bean;

/**
 * spring aop config
 * @author wang xiao
 * date 2023/8/15
 */
public class DSSpringConfig {


    @Bean
    public Advisor dynamicDatasourceAnnotationAdvisor() {

        DsAnnotationInterceptor interceptor = new DsAnnotationInterceptor();
        DynamicDataSourceAnnotationAdvisor advisor = new DynamicDataSourceAnnotationAdvisor(interceptor, DS.class);
        return advisor;
    }

    @Bean
    public Advisor dynamicTransactionAdvisor() {
        DynamicDataSourceTransactionInterceptor interceptor = new DynamicDataSourceTransactionInterceptor();
        return new DynamicDataSourceAnnotationAdvisor(interceptor, DSTransactional.class);
    }
}
