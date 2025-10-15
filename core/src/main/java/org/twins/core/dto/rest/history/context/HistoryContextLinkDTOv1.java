package org.twins.core.dto.rest.history.context;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.link.LinkDTOv1;
import org.twins.core.dto.rest.related.RelatedObject;
import org.twins.core.dto.rest.twin.TwinDTOv2;

import java.util.UUID;

@Data
@Accessors(fluent = true)
@Schema(name =  "HistoryContextLinkV1")
public class HistoryContextLinkDTOv1 implements HistoryContextDTO {
    public static final String KEY = "linkV1";
    public String contextType = KEY;

    @Schema(description = "Link id", example = DTOExamples.LINK_ID)
    @RelatedObject(type = LinkDTOv1.class, name = "link")
    public UUID linkId;

    @Schema(description = "Dst twin id")
    @RelatedObject(type = TwinDTOv2.class, name = "dstTwin")
    public UUID dstTwinId;
}


