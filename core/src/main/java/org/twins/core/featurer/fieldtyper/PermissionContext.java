package org.twins.core.featurer.fieldtyper;

import java.util.UUID;

public record PermissionContext(UUID userId, UUID userGroupFootprintId) {
}
