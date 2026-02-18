package org.twins.core.dao.twin;

import java.math.BigDecimal;
import java.util.UUID;

public record TwinFieldCalcProjection(UUID twinId, BigDecimal calc) {
}

