package org.twins.core.config.resolvers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.pagination.SimplePagination;
import org.cambium.common.util.PaginationUtils;
import org.cambium.common.util.StringUtils;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.twins.core.controller.rest.annotation.SimplePaginationParams;

@Component
@RequiredArgsConstructor
@Slf4j
public class SimplePaginationParamsHandlerMethodArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(SimplePaginationParams.class);
    }

    @Override
    public SimplePagination resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        SimplePaginationParams paginationParams = parameter.getParameterAnnotation(SimplePaginationParams.class);
        if (paginationParams == null)
            return null;
        int offset = parseParameter(webRequest, SimplePagination.Fields.offset, paginationParams.offset());
        int limit = parseParameter(webRequest, SimplePagination.Fields.limit, paginationParams.limit());
        boolean sortAsc = parseParameter(webRequest, "sortAsc", paginationParams.sortAsc());
        String sortField = webRequest.getParameter("sortField");

        return new SimplePagination()
                .setOffset(offset)
                .setLimit(limit)
                .setSort(PaginationUtils.sortType(sortAsc,  StringUtils.isNotEmpty(sortField) ? new String []{sortField} : paginationParams.sortField()));
    }

    private int parseParameter(NativeWebRequest webRequest, String paramName, int defaultValue) {
        String paramValue = webRequest.getParameter(paramName);
        if (paramValue != null) {
            try {
                return Integer.parseInt(paramValue);
            } catch (NumberFormatException e) {
                log.warn("Invalid parameter value for {}: {}", paramName, paramValue);
            }
        }
        return defaultValue;
    }

    private boolean parseParameter(NativeWebRequest webRequest, String paramName, boolean defaultValue) {
        String paramValue = webRequest.getParameter(paramName);
        if (paramValue != null) {
            return Boolean.parseBoolean(paramValue);
        }
        return defaultValue;
    }
}
