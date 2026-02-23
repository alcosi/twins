package org.twins.core.dao.twin;

import java.math.BigDecimal;
import java.util.UUID;

public record TwinFieldHeadSumCountProjection(UUID headTwinId, BigDecimal sum, Long count) {
} 