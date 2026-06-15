package org.twins.core.dto.rest.permission;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

import java.util.Set;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@Schema(name = "PermissionExportSqlRqV1")
public class PermissionExportSqlRqDTOv1 extends Request {
    @Schema(description = "permission ids to export SQL for")
    public Set<UUID> permissionIds;
}
