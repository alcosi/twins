package org.twins.core.dto.rest.link;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;
import org.twins.core.dto.rest.twin.TwinSearchSimpleDTOv1;

import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "LinkValidTwinsForNewSearchRqV1")
public class LinkValidTwinsForNewSearchRqDTOv1 extends Request {
    @Schema(description = "simple filter for valid twins")
    public TwinSearchSimpleDTOv1 twinSearch;

    @Schema(description = "an id of twin class of new twin", requiredMode = Schema.RequiredMode.REQUIRED)
    public UUID twinClassId;

    @Schema(description = "an id of head twin of new twin (if any)")
    public UUID headTwinId;
}
