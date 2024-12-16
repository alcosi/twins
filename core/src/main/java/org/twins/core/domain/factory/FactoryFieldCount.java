package org.twins.core.domain.factory;

import lombok.Data;

@Data
public class FactoryFieldCount {
    private Long usagesCount;
    private Long pipelinesCount;
    private Long multipliersCount;
    private Long branchesCount;
    private Long erasersCount;
}
