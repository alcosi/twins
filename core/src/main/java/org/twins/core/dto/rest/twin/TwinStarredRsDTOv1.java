package org.twins.core.dto.rest.twin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.ResponseRelatedObjectsDTOv1;

@Data
@Accessors(fluent = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name =  "TwinStarredRsV1")
public class TwinStarredRsDTOv1 extends ResponseRelatedObjectsDTOv1 {
    @Schema(description = "twin starred")
    public TwinStarredDTOv1 twinStarred;
}
