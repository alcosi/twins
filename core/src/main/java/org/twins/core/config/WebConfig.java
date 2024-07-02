package org.twins.core.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.twins.core.config.resolvers.DynamicParamsHandlerMethodArgumentResolver;
import org.twins.core.service.DynamicMapperParameterService;

import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private DynamicMapperParameterService mapperParameterService;


    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new DynamicParamsHandlerMethodArgumentResolver(mapperParameterService));
    }
}
