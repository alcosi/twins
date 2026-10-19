package org.twins.core.dto.rest.twinstatus;

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
@Schema(name = "TwinStatusExportSqlRqV1")
public class TwinStatusExportSqlRqDTOv1 extends Request {
    @Schema(description = "twin status ids to export SQL for")
    public Set<UUID> statusIds;
}
