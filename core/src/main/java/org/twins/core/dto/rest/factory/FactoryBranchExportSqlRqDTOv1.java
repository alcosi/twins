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
@Schema(name = "FactoryBranchExportSqlRqV1")
public class FactoryBranchExportSqlRqDTOv1 extends Request {
    @Schema(description = "twin factory branch ids to export SQL for")
    public Set<UUID> twinFactoryBranchIds;
}
