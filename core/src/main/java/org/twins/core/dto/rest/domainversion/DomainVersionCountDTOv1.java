package org.twins.core.dto.rest.domainversion;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.CountDTOv1;
import org.twins.core.enums.domain.DomainVersionStatus;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@Schema(name = "DomainVersionCountV1")
public class DomainVersionCountDTOv1 extends CountDTOv1 {
    @Schema(description = "status. Meaningful only when the count is grouped by status; null otherwise", example = "RELEASED")
    public DomainVersionStatus status;
}
