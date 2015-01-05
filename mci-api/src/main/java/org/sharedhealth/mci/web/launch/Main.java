package org.sharedhealth.mci.web.launch;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import java.util.Map;

import org.sharedhealth.mci.web.launch.migration.Migrations;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.ServletContextInitializer;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.DispatcherServlet;

import static java.lang.Integer.valueOf;
import static java.lang.System.getenv;

@Configuration
@Import(WebMvcConfig.class)
public class Main {

    @Bean
    public EmbeddedServletContainerFactory getFactory() {
        Map<String, String> env = getenv();
        TomcatEmbeddedServletContainerFactory factory = new TomcatEmbeddedServletContainerFactory();
        factory.addInitializers(new ServletContextInitializer() {
            @Override
            public void onStartup(ServletContext servletContext) throws ServletException {

                ServletRegistration.Dynamic mci = servletContext.addServlet("mci", DispatcherServlet.class);
                mci.addMapping("/");
                mci.setInitParameter("contextClass", "org.springframework.web.context.support" +
                        ".AnnotationConfigWebApplicationContext");
                mci.setInitParameter("contextConfigLocation", "org.sharedhealth.mci.web.launch.WebMvcConfig");
                mci.setAsyncSupported(true);

            }
        });

        String mci_port = env.get("MCI_PORT");
        factory.setPort(valueOf(mci_port));
        return factory;
    }

    public static void main(String[] args) throws Exception {
        new Migrations().migrate();
        SpringApplication.run(Main.class, args);
    }
}
