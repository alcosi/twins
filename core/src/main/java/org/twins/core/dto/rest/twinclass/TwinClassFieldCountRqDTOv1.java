package org.twins.core.dto.rest.twinclass;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;
import org.twins.core.enums.sort.TwinClassFieldGroupField;

import java.util.Set;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "TwinClassFieldCountRqV1")
public class TwinClassFieldCountRqDTOv1 extends Request {
    @Schema(description = "search params")
    public TwinClassFieldSearchDTOv1 search;

    @Schema(description = "Group by fields")
    public Set<TwinClassFieldGroupField> groupFields;
}
