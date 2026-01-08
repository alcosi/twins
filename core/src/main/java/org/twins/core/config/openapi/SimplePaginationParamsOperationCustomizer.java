package org.twins.core.config.openapi;

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.cambium.common.pagination.SimplePagination;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.twins.core.controller.rest.annotation.SimplePaginationParams;

import java.util.ArrayList;
import java.util.List;

@Component
public class SimplePaginationParamsOperationCustomizer implements OperationCustomizer {

    @Override
    public Operation customize(Operation operation, HandlerMethod handlerMethod) {
        for (var methodParameter : handlerMethod.getMethodParameters()) {
            SimplePaginationParams paginationParams = methodParameter.getParameterAnnotation(SimplePaginationParams.class);
            if (paginationParams != null) {
                List<Parameter> parameters = new ArrayList<>();

                parameters.add(new Parameter()
                        .name(SimplePagination.Fields.offset)
                        .in("query")
                        .schema(new Schema<>().type("integer")._default(paginationParams.offset())));

                parameters.add(new Parameter()
                        .name(SimplePagination.Fields.limit)
                        .in("query")
                        .schema(new Schema<>().type("integer")._default(paginationParams.limit())));

                parameters.add(new Parameter()
                        .name(SimplePagination.SORT_ASC)
                        .in("query")
                        .schema(new Schema<>().type("boolean")._default(paginationParams.sortAsc())));

                if (null == operation.getParameters()) operation.setParameters(new ArrayList<>());
                operation.getParameters().addAll(parameters);
                break;
            }
        }
        return operation;
    }
}
