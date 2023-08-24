package org.twins.core.dto.rest.twinclass;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

import java.util.List;
import java.util.UUID;

@Data
@Accessors(fluent = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name =  "TwinClassListRqV1")
public class TwinClassListRqDTOv1 extends Request {
    @Schema(description = "twin class id list", example = "['e2b4b9c0-4cdd-42a0-a52a-a8e439b1c17b']")
    public List<UUID> twinClassIdList;

    @Schema(description = "show fields", example = "true")
    public boolean showFields;
}
