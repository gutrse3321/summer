package ru.reimu.server.commons.jdbc.multiData;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/**
 * @Author: Tomonori
 * @Date: 2019/11/8 11:38
 * @Desc: 多数据源路由
 */
public class DynamicDataSourceRouter extends AbstractRoutingDataSource {

    private static final Logger log = LoggerFactory.getLogger(DynamicDataSourceRouter.class);

    private static final ThreadLocal<String> dataSource = new ThreadLocal<>();

    @Override
    protected Object determineCurrentLookupKey() {
        return null;
    }

    public String getDataSource() {
        return dataSource.get();
    }

    public void clearDataSource() {
        dataSource.remove();
    }

    public void setDataSource(String customerType) {
        dataSource.set(customerType);
    }
}
