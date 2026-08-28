package org.twins.core.dto.rest.twinpointer;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;

import java.util.HashMap;
import java.util.UUID;

/**
 * Common fields shared by {@link TwinPointerCreateDTOv1} and {@link TwinPointerUpdateDTOv1}.
 * The {@code id} field is intentionally NOT declared here — it lives only on the update DTO.
 */
@Data
@Accessors(chain = true)
@Schema(name = "TwinPointerSaveV1")
public class TwinPointerSaveDTOv1 {
    @Schema(description = "twin class id. null means the pointer is shared / global", example = DTOExamples.TWIN_CLASS_ID)
    public UUID twinClassId;

    @Schema(description = "pointer featurer id", example = DTOExamples.FEATURER_ID)
    public Integer pointerFeaturerId;

    @Schema(description = "pointer params (hstore)", example = DTOExamples.FEATURER_PARAM)
    public HashMap<String, String> pointerParams;

    @Schema(description = "name", example = DTOExamples.NAME)
    public String name;

    @Schema(description = "optional. When true, a pointer resolution failure is swallowed (log + cached null) instead of failing the recompute batch. Default: false (strict fail-fast)", example = DTOExamples.BOOLEAN_TRUE)
    public Boolean optional;
}
