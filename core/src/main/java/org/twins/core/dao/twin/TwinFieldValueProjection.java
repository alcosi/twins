package org.twins.core.dao.twin;

import java.math.BigDecimal;
import java.util.UUID;

public record TwinFieldValueProjection(UUID headTwinId, BigDecimal value) {
} 