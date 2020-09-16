package ru.reimu.server.commons.jdbc.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: Tomonori
 * @Date: 2019/11/4 11:13
 * @Desc: ConfigurationProperties在JdbcAutoConfiguration注入
 */
@Data
@ConfigurationProperties(prefix = "appserver.jdbc")
public class JdbcProperties {

    private boolean enableLazyProxy        = false;
    private boolean enableDynamicSwitching = false;

    private DataSourceProperties defaultDataSource = new DataSourceProperties();
    private List<DataSourceProperties> dataSource = new ArrayList<>();
}
