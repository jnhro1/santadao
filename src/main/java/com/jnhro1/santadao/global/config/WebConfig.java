package com.jnhro1.santadao.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/e/{publicCode}").setViewName("forward:/event.html");
        registry.addViewController("/admin/{adminToken}").setViewName("forward:/admin.html");
    }
}
