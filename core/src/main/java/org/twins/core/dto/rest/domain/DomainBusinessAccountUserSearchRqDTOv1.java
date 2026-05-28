package org.twins.core.dto.rest.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "DomainBusinessAccountUserSearchRqV1")
public class DomainBusinessAccountUserSearchRqDTOv1 extends Request {
    @Schema(description = "search params")
    public DomainBusinessAccountUserSearchDTOv1 search;
}
