package org.twins.core.dao.twin;

import java.util.UUID;

public record TwinFieldHeadSumCountProjection(UUID headTwinId, Double sum, Long count) {
} 