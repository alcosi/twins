package org.twins.core.domain.permission;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors(chain = true)
public class PermissionCheckOverview {
    public UUID userId;
    public UUID twinId;
    public UUID permissionId;
}
