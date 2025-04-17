package org.twins.core.dto.rest.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;


@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "DomainCreateRqV1")
public class DomainCreateRqDTOv1 extends Request {
    @Schema(description = "domain")
    public DomainCreateDTOv1 domain;
}
