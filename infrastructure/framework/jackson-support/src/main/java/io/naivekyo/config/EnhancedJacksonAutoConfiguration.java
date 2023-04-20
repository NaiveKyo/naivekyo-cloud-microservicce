package io.naivekyo.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.time.format.DateTimeFormatter;

/**
 * <p>
 *     Jackson enhanced configuration.
 * </p>
 * @author NaiveKyo
 * @since 1.0
 */
@ConditionalOnClass(ObjectMapper.class)
@AutoConfiguration(before = JacksonAutoConfiguration.class)
public class EnhancedJacksonAutoConfiguration {
    
    private static final String NORMAL_DATE_TIME_FORMATTER = "yyyy-MM-dd HH:mm:ss";

    private static final String NORMAL_DATE_FORMATTER = "yyyy-MM-dd";

    @Bean
    @ConditionalOnClass(JavaTimeModule.class)
    @ConditionalOnMissingBean
    public JavaTimeModule javaTimeModule() {
        return new JavaTimeModule();
    }

    @Bean
    @ConditionalOnClass(Jdk8Module.class)
    @ConditionalOnMissingBean
    public Jdk8Module jdk8Module() {
        return new Jdk8Module();
    }

    @Bean
    Jackson2ObjectMapperBuilderCustomizer jacksonCustomizer() {
        return new JacksonCustomizer();
    }

    static class JacksonCustomizer implements Jackson2ObjectMapperBuilderCustomizer {
        @Override
        public void customize(Jackson2ObjectMapperBuilder jacksonObjectMapperBuilder) {
            DateTimeFormatter dateDTF = DateTimeFormatter.ofPattern(NORMAL_DATE_FORMATTER);
            DateTimeFormatter dateTimeDTF = DateTimeFormatter.ofPattern(NORMAL_DATE_TIME_FORMATTER);

            jacksonObjectMapperBuilder.serializers(new LocalDateSerializer(dateDTF));
            jacksonObjectMapperBuilder.serializers(new LocalDateTimeSerializer(dateTimeDTF));
            jacksonObjectMapperBuilder.deserializers(new LocalDateDeserializer(dateDTF));
            jacksonObjectMapperBuilder.deserializers(new LocalDateTimeDeserializer(dateTimeDTF));
        }
    }
}
