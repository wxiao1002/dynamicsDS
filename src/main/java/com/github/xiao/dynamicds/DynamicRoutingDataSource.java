package com.github.xiao.dynamicds;


import com.github.xiao.dynamicds.util.DynamicDataSourceCtxHolder;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 动态数据源
 * @author wang xiao
 * date 2023/8/10
 */
public class DynamicRoutingDataSource extends AbstractDynamicDataSource
        implements InitializingBean, DisposableBean {

    private final String primary = "master";

    private final Map<String, DataSource> dataSourceMap = new ConcurrentHashMap<>();

    @Override
    protected DataSource determineDataSource() {
        String dsKey = DynamicDataSourceCtxHolder.peek();
        return getDataSource(dsKey);
    }



    @Override
    protected String getPrimaryDsKey() {
        return primary;
    }

    @Override
    public void destroy() throws Exception {
        for (Map.Entry<String, DataSource> item : dataSourceMap.entrySet()) {
            closeDataSource(item.getKey(), item.getValue());
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // 添加数据源
    }

    /**
     * 获取数据源
     * @param dsKey 数据源名称
     * @return DataSource
     */
    public DataSource getDataSource(String dsKey) {
        if (StringUtils.isEmpty(dsKey)) {
            return determinePrimaryDataSource();
        } else if (dataSourceMap.containsKey(dsKey)) {
            return dataSourceMap.get(dsKey);
        }
        return determinePrimaryDataSource();
    }

    /**
     * 获取主数据源
     * @return DataSource
     */
    private DataSource determinePrimaryDataSource() {
        return dataSourceMap.get(primary);
    }


    /**
     * 关闭数据源
     * @param dsKey dsKey
     * @param dataSource DataSource
     */
    private void closeDataSource(String dsKey, DataSource dataSource) {
        try {
            Method closeMethod = ReflectionUtils.findMethod(dataSource.getClass(), "close");
            if (closeMethod != null) {
                closeMethod.invoke(dataSource);
            }
        } catch (Exception ignored) {
        }
    }

}
