package org.sharedhealth.mci.web.config;

import org.sharedhealth.mci.web.launch.WebMvcConfig;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Configuration
@EnableWebMvc
@Import({MCIConfig.class})
@EnableCaching
public class WebMvcConfigTest extends WebMvcConfig {

    @Bean(name = "validator")
    public LocalValidatorFactoryBean validator(){
        return new LocalValidatorFactoryBean();
    }
}
