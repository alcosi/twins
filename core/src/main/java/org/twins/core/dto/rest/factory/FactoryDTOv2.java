package org.twins.core.dto.rest.factory;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.user.UserDTOv1;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Schema(name = "FactoryV2")
public class FactoryDTOv2 extends FactoryDTOv1 {
    @Schema(description = "created by user")
    public UserDTOv1 createdByUser;
}
