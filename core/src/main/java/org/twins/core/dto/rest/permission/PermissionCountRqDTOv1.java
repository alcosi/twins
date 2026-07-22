package org.twins.core.dto.rest.permission;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;
import org.twins.core.enums.sort.PermissionGroupField;

import java.util.Set;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "PermissionCountRqV1")
public class PermissionCountRqDTOv1 extends Request {
    @Schema(description = "search params")
    public PermissionSearchDTOv1 search;

    @Size(max = 2)
    @Schema(description = "Group by fields")
    public Set<PermissionGroupField> groupFields;
}
