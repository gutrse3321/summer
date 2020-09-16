package ru.reimu.server.commons.jdbc.config;

import lombok.Data;
import ru.reimu.akari.commons.support.StringUtility;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: Tomonori
 * @Date: 2019/11/1 17:18
 * @Desc: 数据库参数
 */
@Data
public class DataSourceProperties {

    /**
     * 连接名，用于数据库切换
     */
    private String name;

    /**
     * 库名
     */
    private String db;

    /**
     * 连接地址
     */
    private String baseUrl;

    /**
     * 连接地址参数
     */
    private String searchUrl;

    private String  driverClassName;
    private String  jdbcUrl;
    private String  username;
    private String  password;

    //TODO NEW
    private Integer minimumIdle;
    private Integer maximumPoolSize;
    private Long    connectionTimeout;
    private Long    validationTimeout;
    private String  connectionInitSql;
    private Boolean readOnly;
    private Boolean autoCommit;

    //FIXME OLD
//    private Integer maxActive;
//    private Integer initialSize;
//    private Integer minIdle;
//    private Integer maxIdle;
//    private Boolean testWhileIdle;
//    private Boolean testOnBorrom;
//    private Integer minEvictableIdleTimeMillis;
//    private Integer timeBetweenEvictionRunsMillis;
//    private Integer maxWait;
//    private Integer removeAbandonedTimeout;
//    private Boolean removeAbandoned;
//    private Integer validationInterval;
//    private String  validationQuery;
//    private Boolean defaultAutoCommit;
//    private Boolean defaultReadOnly;

    /**
     * 生成数据库连接字符串
     * @return
     */
    public String makeUrl() {
        StringBuilder builder = new StringBuilder();
        builder.append(this.getBaseUrl());

        if (StringUtility.hasText(this.getDb())) {
            builder.append("/").append(this.getDb());
        }
        //连接地址参数
        if (StringUtility.hasText(this.getSearchUrl())) {
            builder.append("?").append(this.getSearchUrl());
        }
        return builder.toString();
    }

    public String getJdbcUrl() {
        if (null == jdbcUrl) {
            jdbcUrl = makeUrl();
        }
        return jdbcUrl;
    }

    public List<String> ignore() {
        List<String> list = new ArrayList<>();
        list.add("name");
        list.add("db");
        list.add("baseUrl");
        list.add("searchUrl");
        list.add("class");
        return list;
    }
}
