package io.naivekyo.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;
import java.time.LocalDateTime;

/**
 * <p>
 *     Mybatis-Plus enhanced configuration.
 * </p>
 * @author NaiveKyo
 * @since 1.0
 */
@ConditionalOnClass({SqlSessionFactory.class, SqlSessionFactoryBean.class})
@ConditionalOnSingleCandidate(DataSource.class)
@AutoConfiguration(before = MybatisPlusAutoConfiguration.class)
public class EnhancedMybatisPlusAutoConfiguration {
    
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // pagination plugin (notice: default database is MySQL)
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        // prevent full table update/delete plugin
        interceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());

        return interceptor;
    }

    @Bean
    public EntityMetaObjectHandler entityMetaObjectHandler() {
        return new EntityMetaObjectHandler();
    }

    static class EntityMetaObjectHandler implements MetaObjectHandler {

        @Override
        public void insertFill(MetaObject metaObject) {
            // auto generate table fields while exec insert statement: create_time, update_time
            // used with @TableField(fill = xxx) annotation
            this.strictInsertFill(metaObject, "createTime", LocalDateTime::now, LocalDateTime.class);
            this.strictInsertFill(metaObject, "updateTime", LocalDateTime::now, LocalDateTime.class);
        }

        @Override
        public void updateFill(MetaObject metaObject) {
            // used with @TableField(fill = xxx) annotation
            this.strictUpdateFill(metaObject, "updateTime", LocalDateTime::now, LocalDateTime.class);
        }
    }
}
