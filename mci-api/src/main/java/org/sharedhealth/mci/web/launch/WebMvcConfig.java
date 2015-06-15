package org.sharedhealth.mci.web.launch;


import org.sharedhealth.mci.web.config.ActuatorConfig;
import org.sharedhealth.mci.web.config.MCIConfig;
import org.sharedhealth.mci.web.config.MCISecurityConfig;
import org.sharedhealth.mci.web.handler.FeedMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.xml.Jaxb2RootElementHttpMessageConverter;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Configuration
@Import({MCIConfig.class, MCISecurityConfig.class, ActuatorConfig.class})
@EnableWebMvc
@EnableScheduling
@ComponentScan(basePackages = {"org.sharedhealth.mci.web.config",
        "org.sharedhealth.mci.web.controller",
        "org.sharedhealth.mci.web.exception",
        "org.sharedhealth.mci.web.infrastructure",
        "org.sharedhealth.mci.web.mapper",
        "org.sharedhealth.mci.web.model",
        "org.sharedhealth.mci.web.service",
        "org.sharedhealth.mci.web.handler",
        "org.sharedhealth.mci.utils",
        "org.sharedhealth.mci.validation",
        "org.sharedhealth.mci.tasks"
})
public class WebMvcConfig extends WebMvcConfigurerAdapter implements SchedulingConfigurer {

    @Override
    public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
        configurer.enable();
    }


    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(new FeedMessageConverter());
        converters.add(new MappingJackson2HttpMessageConverter());
        converters.add(new Jaxb2RootElementHttpMessageConverter());
    }

    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer contentNegotiator) {
        super.configureContentNegotiation(contentNegotiator);
        contentNegotiator.mediaType("application", MediaType.APPLICATION_ATOM_XML);
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setScheduler(taskExecutor());
    }

    @Bean(destroyMethod = "shutdown")
    public Executor taskExecutor() {
        return Executors.newScheduledThreadPool(10);
    }


}
