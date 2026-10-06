package org.twins.core.dto.rest.link;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "LinkValidatorExportSqlRqV1")
public class LinkValidatorExportSqlRqDTOv1 extends Request {

    @Schema(description = "link validator ids to export SQL for")
    public Set<UUID> linkValidatorIds;
}
