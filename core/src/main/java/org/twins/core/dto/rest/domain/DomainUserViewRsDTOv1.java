package org.twins.core.dto.rest.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.ResponseRelatedObjectsDTOv1;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "DomainUserViewRsV1")
public class DomainUserViewRsDTOv1 extends ResponseRelatedObjectsDTOv1 {
    //todo rename to domainUser
    @Schema(description = "user")
    public DomainUserDTOv1 user;
}
