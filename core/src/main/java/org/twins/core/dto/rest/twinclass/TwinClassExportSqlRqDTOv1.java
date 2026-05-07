package org.twins.core.dto.rest.twinclass;

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
@Schema(name = "TwinClassExportSqlRqV1")
public class TwinClassExportSqlRqDTOv1 extends Request {
    @Schema(description = "twin class ids to export SQL for")
    public Set<UUID> twinClassIds;

    @Schema(description = "include twin class fields in export")
    public boolean includeFields = false;

    @Schema(description = "include twin class statuses in export")
    public boolean includeStatuses = false;

    @Schema(description = "include twinflow in export")
    public boolean includeTwinflow = false;
}
