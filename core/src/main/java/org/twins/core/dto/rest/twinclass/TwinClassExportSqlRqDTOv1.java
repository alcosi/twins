package org.twins.core.dto.rest.twinclass;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@Schema(name = "TwinClassExportSqlRqV1")
public class TwinClassExportSqlRqDTOv1 extends Request {
    @Schema(description = "twin class id to export SQL for")
    public UUID twinClassId;

    @Schema(description = "export twin class fields")
    public boolean duplicateFields = false;

    @Schema(description = "export twin class statuses")
    public boolean duplicateStatuses = false;

    @Schema(description = "export twinflow")
    public boolean duplicateTwinflow = false;
}
