package org.twins.core.dto.rest.space;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;
import org.twins.core.enums.sort.SpaceRoleGroupField;

import java.util.Set;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "SpaceRoleCountRqV1")
public class SpaceRoleCountRqDTOv1 extends Request {
    @Valid
    @Schema(description = "search params")
    public SpaceRoleSearchDTOv1 search;

    @Size(max = 2)
    @Schema(description = "Group by fields")
    public Set<SpaceRoleGroupField> groupFields;
}
