package org.twins.core.dto.rest.twinstatus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;
import org.twins.core.enums.sort.TwinStatusGroupField;

import java.util.Set;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "TwinStatusCountRqV1")
public class TwinStatusCountRqDTOv1 extends Request {
    @Schema(description = "search params")
    public TwinStatusSearchDTOv1 search;

    @Schema(description = "Group by fields")
    public Set<TwinStatusGroupField> groupFields;
}
