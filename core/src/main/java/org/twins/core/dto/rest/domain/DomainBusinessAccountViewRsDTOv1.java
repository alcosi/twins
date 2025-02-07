package org.twins.core.dto.rest.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.ResponseRelatedObjectsDTOv1;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "DomainBusinessAccountViewRsV1")
public class DomainBusinessAccountViewRsDTOv1 extends ResponseRelatedObjectsDTOv1 {
    @Schema(description = "result - domain business account")
    public DomainBusinessAccountDTOv1 businessAccount;
}
