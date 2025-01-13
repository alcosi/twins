package org.twins.core.dto.rest.link;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.twinclass.TwinClassBaseDTOv1;
import org.twins.core.dto.rest.user.UserDTOv1;

@Data
@Accessors(fluent = true)
@Schema(name =  "LinkV3")
public class LinkDTOv3 extends LinkDTOv2 {
    @Schema(description = "Source twin class")
    public TwinClassBaseDTOv1 srcTwinClass;

    @Schema(description = "Created by user")
    public UserDTOv1 createdByUser;

}
