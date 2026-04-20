package org.twins.core.dao.specifications;

import org.hibernate.boot.model.FunctionContributions;
import org.hibernate.boot.model.FunctionContributor;

public class PostgresAnyFunctionContributor implements FunctionContributor {
    @Override
    public void contributeFunctions(FunctionContributions functionContributions) {
        functionContributions.getFunctionRegistry()
                .registerPattern("native_uuid_any", "?1 = ANY(CAST(?2 AS uuid[]))");
    }
}