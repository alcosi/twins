package org.twins.core.dto.rest.factory;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.CountDTOv1;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.related.RelatedObject;
import org.twins.core.dto.rest.user.UserDTOv1;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@Schema(name = "FactoryCountV1")
public class FactoryCountDTOv1 extends CountDTOv1 {
    @Schema(description = "created by user id", example = DTOExamples.UUID_ID)
    @RelatedObject(type = UserDTOv1.class, name = "user")
    public UUID createdByUserId;

    @Schema(description = "domain id", example = DTOExamples.UUID_ID)
    public UUID domainId;
}
