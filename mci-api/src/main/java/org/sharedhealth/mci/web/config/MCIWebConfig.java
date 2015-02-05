package org.sharedhealth.mci.web.config;

import org.sharedhealth.mci.web.handler.FeedMessageConverter;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.xml.Jaxb2RootElementHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.util.List;

@Configuration
public class MCIWebConfig extends WebMvcConfigurerAdapter {
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
}
