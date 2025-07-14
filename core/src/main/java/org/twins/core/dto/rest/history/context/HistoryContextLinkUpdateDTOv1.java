package org.twins.core.dto.rest.history.context;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.link.LinkDTOv1;
import org.twins.core.dto.rest.user.UserDTOv1;

import java.util.UUID;

@Data
@Accessors(fluent = true)
@Schema(name =  HistoryContextLinkUpdateDTOv1.KEY, oneOf = { HistoryContextDTO.class })
public class HistoryContextLinkUpdateDTOv1 implements HistoryContextDTO {

    public static final String KEY = "HistoryContextLinkUpdateV1";

    public HistoryContextLinkUpdateDTOv1() {
        this.contextType = KEY;
    }

    @Schema(description = "Context type", allowableValues = {KEY}, example = KEY, requiredMode = Schema.RequiredMode.REQUIRED)
    public String contextType;

    @Schema(description = "Link id", example = DTOExamples.LINK_ID)
    public UUID linkId;

    @Schema(description = "Link")
    public LinkDTOv1 link;

    @Schema(description = "From twin id", example = DTOExamples.TWIN_ID)
    public UUID fromDstTwinId;

    @Schema(description = "From twin")
    public UserDTOv1 fromDstTwin;

    @Schema(description = "To twin id")
    public UUID toDstTwinId;

    @Schema(description = "To twin")
    public UserDTOv1 toDstTwin;
}
