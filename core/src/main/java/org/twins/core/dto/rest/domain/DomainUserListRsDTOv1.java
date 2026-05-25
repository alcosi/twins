package org.twins.core.dto.rest.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.ResponseRelatedObjectsDTOv1;

import java.util.List;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "DomainUserListRsV1")
public class DomainUserListRsDTOv1 extends ResponseRelatedObjectsDTOv1 {
    @Schema(description = "user list")
    public List<DomainUserDTOv1> users;
}
