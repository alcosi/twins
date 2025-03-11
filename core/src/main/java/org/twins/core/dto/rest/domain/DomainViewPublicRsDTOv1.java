package org.twins.core.dto.rest.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Response;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "DomainViewPublicRsV1")
public class DomainViewPublicRsDTOv1 extends Response {
    @Schema(description = "domain")
    private DomainViewPublicDTOv1 domain;
}
