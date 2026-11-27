package org.twins.core.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.twins.core.config.resolvers.MapperContextHandlerMethodArgumentResolver;
import org.twins.core.config.resolvers.SimplePaginationParamsHandlerMethodArgumentResolver;
import org.twins.core.service.MapperModesResolveService;
import org.twins.core.service.permission.PermissionService;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final MapperModesResolveService mapperModesResolveService;
    @Lazy
    private final PermissionService permissionService;

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new MapperContextHandlerMethodArgumentResolver(mapperModesResolveService, permissionService));
        resolvers.add(new SimplePaginationParamsHandlerMethodArgumentResolver());
    }
}
