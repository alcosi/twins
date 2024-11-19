package org.twins.core.dto.rest.draft;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.user.UserDTOv1;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Schema(name =  "DraftV1")
public class DraftDTOv1 extends DraftBaseDTOv1{
    @Schema(description = "created by user")
    public UserDTOv1 createdByUser;
}
