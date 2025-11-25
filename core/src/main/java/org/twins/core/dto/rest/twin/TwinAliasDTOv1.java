package org.twins.core.dto.rest.twin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.businessaccount.BusinessAccountDTOv1;
import org.twins.core.dto.rest.related.RelatedObject;
import org.twins.core.dto.rest.user.UserDTOv1;

import java.util.UUID;

@Data
@Accessors(fluent = true)
@Schema(name =  "TwinAliasV1")
public class TwinAliasDTOv1 {
    @Schema(description = "id", example = DTOExamples.TWIN_STATUS_ID)
    public UUID id;

    @Schema(description = "alias", example = "TWIN-D41")
    public String alias;

    @Schema(description = "twin id", example = "")
    @RelatedObject(type = TwinDTOv2.class, name = "twin")
    public UUID twinId;

    @Schema(description = "domain id", example = "")
    public UUID domainId;

    @Schema(description = "business account id", example = "")
    @RelatedObject(type = BusinessAccountDTOv1.class, name = "businessAccount")
    public UUID businessAccountId;

    @Schema(description = "user id", example = "")
    @RelatedObject(type = UserDTOv1.class, name = "user")
    public UUID userId;
}


