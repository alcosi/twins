package org.twins.core.dto.rest.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.ResponseRelatedObjectsDTOv1;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "DomainViewRsv1")
public class DomainViewRsDTOv1 extends ResponseRelatedObjectsDTOv1 {
    @Schema(description = "domain")
    private DomainViewDTOv1 domain;
}
