package org.mci.web.config;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

@Configuration
@EnableWebMvc
@EnableAutoConfiguration
@ComponentScan(basePackages = "org.mci.web")
public class WebMvcConfig extends WebMvcConfigurationSupport {
}
