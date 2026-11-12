package org.twins.core.dto.rest.domainversion;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;
import org.twins.core.enums.sort.DomainVersionGroupField;

import java.util.Set;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "DomainVersionCountRqV1")
public class DomainVersionCountRqDTOv1 extends Request {
    @Schema(description = "search params")
    public DomainVersionSearchDTOv1 search;

    @Size(max = 1)
    @Schema(description = "Group by fields. Empty set = total count without grouping")
    public Set<DomainVersionGroupField> groupFields;
}
