package io.naivekyo.mvc;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * <p>
 *     Spring MVC advanced customizations.
 * </p>
 * @author NaiveKyo
 * @since 1.0
 */
@Configuration(proxyBeanMethods = false)
@EnableWebMvc
public class SpringMVCConfiguration implements WebMvcConfigurer {
    
}
