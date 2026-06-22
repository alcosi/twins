package org.twins.core.dto.rest.factory;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

import java.util.Set;
import java.util.UUID;

@EqualsAndHashCode(callSuper = false)
@Data
@Accessors(chain = true)
@Schema(name = "FactoryTriggerExportSqlRqV1")
public class FactoryTriggerExportSqlRqDTOv1 extends Request {
    @Schema(description = "twin factory trigger ids to export SQL for")
    public Set<UUID> twinFactoryTriggerIds;
}
