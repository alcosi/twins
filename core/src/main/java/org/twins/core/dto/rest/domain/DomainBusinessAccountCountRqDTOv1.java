package org.twins.core.dto.rest.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;
import org.twins.core.enums.sort.DomainBusinessAccountGroupField;

import java.util.Set;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "DomainBusinessAccountCountRqV1")
public class DomainBusinessAccountCountRqDTOv1 extends Request {
    @Schema(description = "search params")
    public DomainBusinessAccountSearchDTOv1 search;

    @Size(max = 2)
    @Schema(description = "Group by fields")
    public Set<DomainBusinessAccountGroupField> groupFields;
}
