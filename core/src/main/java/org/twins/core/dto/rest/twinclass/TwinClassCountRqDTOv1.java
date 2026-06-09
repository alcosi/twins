package org.twins.core.dto.rest.twinclass;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;
import org.twins.core.enums.sort.TwinClassGroupField;

import java.util.Set;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "TwinClassCountRqV1")
public class TwinClassCountRqDTOv1 extends Request {
    @Schema(description = "search params")
    public TwinClassSearchDTOv1 search;

    @Schema(description = "Group by fields")
    public Set<TwinClassGroupField> groupFields;
}
