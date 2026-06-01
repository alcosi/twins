package org.twins.core.dto.rest.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;
import org.twins.core.enums.sort.DomainBusinessAccountUserGroupField;

import java.util.Set;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "DomainBusinessAccountUserCountRqV1")
public class DomainBusinessAccountUserCountRqDTOv1 extends Request {
    @Schema(description = "search params")
    public DomainBusinessAccountUserSearchDTOv1 search;

    @Schema(description = "Group by fields")
    public Set<DomainBusinessAccountUserGroupField> groupFields;
}
