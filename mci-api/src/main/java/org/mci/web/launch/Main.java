package org.mci.web.launch;


import org.mci.web.launch.migration.Migrations;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@EnableAutoConfiguration
@ComponentScan(basePackages = "org.mci.web")
public class Main {


    public static void main(String[] args) throws Exception {
        new Migrations().migrate();
        SpringApplication.run(Main.class, args);
    }
}
