package org.twins.core.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.twins.core.controller.rest.annotation.MapperContextBindingResolver;
import org.twins.core.controller.rest.annotation.MapperModeParamResolver;

import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
//        resolvers.add(new MapperContextBindingResolver());
        resolvers.add(new MapperModeParamResolver());
    }
}
