package com.github.xiao.dynamicds;


/**
 * 动态数据源
 * @author wang xiao
 * date 2023/8/10
 */
public class DynamicRoutingDataSource extends AbstractDynamicDataSource
        implements InitializingBean, DisposableBean {

    @Override
    protected Object determineCurrentLookupKey() {
        String dataSourceKey = DynamicDataSourceCtxHolder.peek();
        return ;
    }

    @Override
    public void destroy() throws Exception {

    }
}
