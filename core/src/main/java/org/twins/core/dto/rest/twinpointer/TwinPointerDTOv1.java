package org.twins.core.dto.rest.twinpointer;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.featurer.FeaturerDTOv1;
import org.twins.core.dto.rest.related.RelatedObject;
import org.twins.core.dto.rest.twinclass.TwinClassDTOv1;
import org.twins.core.dto.rest.user.UserDTOv1;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "TwinPointerV1")
public class TwinPointerDTOv1 {
    @Schema(description = "id", example = DTOExamples.UUID_ID)
    public UUID id;

    @Schema(description = "twin class id. null means the pointer is shared / global", example = DTOExamples.TWIN_CLASS_ID)
    @RelatedObject(type = TwinClassDTOv1.class, name = "twinClass")
    public UUID twinClassId;

    @Schema(description = "pointer featurer id", example = DTOExamples.FEATURER_ID)
    @RelatedObject(type = FeaturerDTOv1.class, name = "pointerFeaturer")
    public Integer pointerFeaturerId;

    @Schema(description = "pointer params (hstore)", example = DTOExamples.FEATURER_PARAM)
    public HashMap<String, String> pointerParams;

    @Schema(description = "name", example = DTOExamples.NAME)
    public String name;

    @Schema(description = "optional. When true, a pointer resolution failure is swallowed (log + cached null) instead of failing the recompute batch. Default: false", example = DTOExamples.BOOLEAN_TRUE)
    public Boolean optional;

    @Schema(description = "created at", example = DTOExamples.INSTANT)
    public Timestamp createdAt;

    @Schema(description = "created by user id", example = DTOExamples.USER_ID)
    @RelatedObject(type = UserDTOv1.class, name = "createdByUser")
    public UUID createdByUserId;
}
