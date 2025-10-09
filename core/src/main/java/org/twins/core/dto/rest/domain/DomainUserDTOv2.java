package org.twins.core.dto.rest.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.user.UserDTOv1;

import java.util.List;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "DomainUserV2")
public class DomainUserDTOv2 extends DomainUserDTOv1 {
    @Schema(description = "user")
    public UserDTOv1 user;

    @Schema(description = "Business account users." + DTOExamples.LAZY_RELATION_MODE_ON)
    public List<BusinessAccountUserDTOv1> businessAccountUsers;
}
