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
@Schema(name =  HistoryContextLinkDTOv1.KEY)
public class HistoryContextLinkDTOv1 implements HistoryContextDTO {

    public static final String KEY = "HistoryContextLinkV1";

    public HistoryContextLinkDTOv1() {
        this.contextType = KEY;
    }

    @Schema(description = "Context type", allowableValues = {KEY}, example = KEY, requiredMode = Schema.RequiredMode.REQUIRED)
    public String contextType;

    @Schema(description = "Link id", example = DTOExamples.LINK_ID)
    public UUID linkId;

    @Schema(description = "Link")
    public LinkDTOv1 link;

    @Schema(description = "Dst twin id")
    public UUID dstTwinId;

    @Schema(description = "Dst twin")
    public UserDTOv1 dstTwin;
}
