package org.twins.core.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.twins.core.config.resolvers.MapperContextHandlerMethodArgumentResolver;
import org.twins.core.config.resolvers.SimplePaginationParamsHandlerMethodArgumentResolver;
import org.twins.core.service.MapperModesResolveService;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final MapperModesResolveService mapperModesResolveService;

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new MapperContextHandlerMethodArgumentResolver(mapperModesResolveService));
        resolvers.add(new SimplePaginationParamsHandlerMethodArgumentResolver());
    }
}
