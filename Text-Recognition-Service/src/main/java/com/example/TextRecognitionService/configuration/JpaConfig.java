package com.example.TextRecognitionService.configuration;
import com.example.TextRecognitionService.properties.DataBaseProps;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
public class JpaConfig {

    private final DataBaseProps dataBaseProps;
    @Autowired
    public JpaConfig(DataBaseProps dataBaseProps) {
        this.dataBaseProps = dataBaseProps;
    }
    @Bean
    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource.setUrl("jdbc:mysql://"+dataBaseProps.getHost()+":"+dataBaseProps.getPort()+"/"+dataBaseProps.getName());
        dataSource.setUsername(dataBaseProps.getUsername());
        dataSource.setPassword(dataBaseProps.getPassword());
        return dataSource;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
        emf.setDataSource(dataSource);
        emf.setPackagesToScan("com.example.TextRecognitionService");
        emf.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        return emf;
    }

}