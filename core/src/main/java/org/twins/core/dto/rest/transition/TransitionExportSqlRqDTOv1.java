package org.twins.core.dto.rest.transition;

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
@Schema(name = "TransitionExportSqlRqV1")
public class TransitionExportSqlRqDTOv1 extends Request {
    @Schema(description = "transition ids to export SQL for")
    public Set<UUID> transitionIds;

    @Schema(description = "include inbuilt and drafting factories (with their elements)")
    public boolean includeFactory = false;

    @Schema(description = "include src and dst twin statuses")
    public boolean includeStatuses = false;

    @Schema(description = "include permission entity")
    public boolean includePermission = false;

    @Schema(description = "include transition triggers")
    public boolean includeTriggers = false;

    @Schema(description = "include validator rules with validator sets")
    public boolean includeValidatorRules = false;
}
