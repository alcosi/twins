package org.twins.core.dto.rest.twin;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.enums.twin.TwinGroupSort;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "TwinGroupLevelV1")
public class TwinGroupLevelDTOv1 {
    @NotNull
    @Schema(description = "Twin class field id. A column, a countable dynamic field, or a link-typed field")
    public UUID fieldId;

    @Schema(description = "Order of nodes at this level. Default is NAME_ASC for users, head and link-typed fields, KEY_ASC otherwise")
    public TwinGroupSort sort;
}
