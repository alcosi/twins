package org.twins.core.dto.rest.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.CountDTOv1;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.businessaccount.BusinessAccountDTOv1;
import org.twins.core.dto.rest.related.RelatedObject;
import org.twins.core.dto.rest.user.UserDTOv1;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@Schema(name = "DomainBusinessAccountUserCountV1")
public class DomainBusinessAccountUserCountDTOv1 extends CountDTOv1 {
    @Schema(description = "user id", example = DTOExamples.UUID_ID)
    @RelatedObject(type = UserDTOv1.class, name = "user")
    public UUID userId;

    @Schema(description = "business account id", example = DTOExamples.UUID_ID)
    @RelatedObject(type = BusinessAccountDTOv1.class, name = "businessAccount")
    public UUID businessAccountId;
}
