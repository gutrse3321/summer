package ru.reimu.server.commons.jdbc.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;
import org.springframework.jdbc.datasource.lookup.BeanFactoryDataSourceLookup;
import ru.reimu.akari.commons.support.EntityPropertyUtility;
import ru.reimu.akari.commons.support.ReflectionUtility;
import ru.reimu.akariserver.datasource.aop.DynamicDataSourceAspect;
import ru.reimu.akariserver.datasource.jdbc.multiData.DynamicDataSourceRouter;

import javax.sql.DataSource;
import java.beans.PropertyDescriptor;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @Author: Tomonori
 * @Date: 2019/11/4 11:00
 * @Desc: 数据源配置与注入
 */
@Configuration
@EnableConfigurationProperties(JdbcProperties.class)
public class JdbcAutoConfiguration implements ApplicationContextAware, InitializingBean {

    private final JdbcProperties jdbcProperties;
    private GenericApplicationContext applicationContext;

    public JdbcAutoConfiguration(JdbcProperties jdbcProperties) {
        this.jdbcProperties = jdbcProperties;
    }

    private String makeDataSourceBeanName(String name) {
        return "reimu_akariserver_datasource_" + name.replace("-", "_").replace(".", "_").replace(" ", "_").toLowerCase();
    }

    /**
     * 数据源托管Bean实例
     * @return
     */
    @Bean
    public BeanFactoryDataSourceLookup beanFactoryDataSourceLookup() {
        return new BeanFactoryDataSourceLookup();
    }

    /**
     * Bean注入
     * 数据源
     */
    @Bean
    @Primary
    public DataSource dataSource() {
        //多数据源路由
        DynamicDataSourceRouter router = new DynamicDataSourceRouter();
        router.setDataSourceLookup(beanFactoryDataSourceLookup());

        Map<Object, Object> targetDataSources = new LinkedHashMap<>();
        jdbcProperties.getDataSource().forEach(dataSourceProperties -> targetDataSources.put(dataSourceProperties.getName(), makeDataSourceBeanName(dataSourceProperties.getName())));
        router.setTargetDataSources(targetDataSources);
        router.setDefaultTargetDataSource(makeDataSourceBeanName(jdbcProperties.getDefaultDataSource().getName()));

        return router;
    }

    /**
     * 注入数据源切换切面
     */
    @Bean
    //自动化配置
    @ConditionalOnProperty(value = "appserver.jdbc.enable-dynamic-switch", havingValue = "true")
    public DynamicDataSourceAspect dynamicDataSourceAspect() {
        return new DynamicDataSourceAspect();
    }

    /**
     * ApplicationContextAware实现
     * @param applicationContext
     * @throws BeansException
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if (applicationContext instanceof GenericApplicationContext) {
            this.applicationContext = (GenericApplicationContext) applicationContext;
        }
    }

    /**
     * InitializingBean实现
     * 初始化bean
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        registerDataSource(jdbcProperties.getDefaultDataSource());
        //将jdbcProperties dataSource属性的值遍历执行registerDataSource方法
        jdbcProperties.getDataSource().forEach(this::registerDataSource);
    }

    private void registerDataSource(DataSourceProperties dataSourceProperties) {
        DataSourceProperties defaultSource = new DataSourceProperties();
        EntityPropertyUtility.copyNotNull(jdbcProperties.getDefaultDataSource(), defaultSource);
        EntityPropertyUtility.copyNotNull(dataSourceProperties, defaultSource);
        dataSourceProperties = defaultSource;

        //获取数据库参数类的所有属性信息(包括继承的
        PropertyDescriptor[] propertyDescriptors = EntityPropertyUtility.getPropertyDescriptors(DataSourceProperties.class);
        Map<String, Object> map = new HashMap<>();

        //遍历数据库参数类的所有属性信息
        for (PropertyDescriptor descriptor : propertyDescriptors) {
            //如果属性名称与ignore方法返回的list元素有相同的就跳过本次循环
            if (dataSourceProperties.ignore().contains(descriptor.getName())) continue;
            /**
             * 反射执行getter方法
             */
            Object value = ReflectionUtility.invokeMethod(descriptor.getReadMethod(), dataSourceProperties);
            //没有getter到值则跳过本次循环，否则将属性名称与值加到map中
            if (null == value) continue;
            map.put(descriptor.getName(), value);
        }

        /**
         * GenericBeanDefinition 一站式Bean定义组件，可以满足任何要求的Bean定义。例如读取配置后生成的就是GenericBeanDefinition
         */
        GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
        if (jdbcProperties.isEnableLazyProxy()) {
            //注入bean的类
            beanDefinition.setBeanClass(LazyConnectionDataSourceProxy.class);
            //创建数据源
//            DataSource dataSource = DataSourceBuilder.create().type(org.apache.tomcat.jdbc.pool.DataSource.class).build();
            DataSource dataSource = DataSourceBuilder.create().type(HikariDataSource.class).build();
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                //获取数据源对象的属性信息
                PropertyDescriptor propertyDescriptor = EntityPropertyUtility.getPropertyDescriptor(dataSource.getClass(), entry.getKey());
                //将dataSourceProperties属性的值setter到dataSource对象对应的属性值
                ReflectionUtility.invokeMethod(propertyDescriptor.getWriteMethod(), dataSource, entry.getValue());
            }
            //添加bean的属性
            beanDefinition.getPropertyValues().addPropertyValue("targetDataSource", dataSource);
        } else {
//            beanDefinition.setBeanClass(DataSourceBuilder.create().type(org.apache.tomcat.jdbc.pool.DataSource.class).findType(null));
            beanDefinition.setBeanClass(DataSourceBuilder.create().type(HikariDataSource.class).findType(null));
            beanDefinition.getPropertyValues().addPropertyValues(map);
        }

        beanDefinition.setSynthetic(true);
        /**
         * Generic ApplicationContext 持有一个DefaultListableBeanFactory实例，并且没有假设一个特定的bean definition 的format。
         * 实现了BeanDefinitionRegistry接口以允许配置任何bean definition reader
         */
        applicationContext.registerBeanDefinition(makeDataSourceBeanName(dataSourceProperties.getName()), beanDefinition);
    }

}
