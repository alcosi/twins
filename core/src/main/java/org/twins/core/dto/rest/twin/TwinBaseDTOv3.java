package org.twins.core.dto.rest.twin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.attachment.AttachmentViewDTOv1;
import org.twins.core.dto.rest.link.TwinLinkListDTOv1;
import org.twins.core.dto.rest.twinflow.TwinTransitionListDTOv1;
import org.twins.core.dto.rest.twinflow.TwinTransitionViewDTOv1;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(fluent = true)
@Schema(name =  "TwinBaseV3")
public class TwinBaseDTOv3 extends TwinBaseDTOv2 {
    @Schema(description = "attachments")
    public List<AttachmentViewDTOv1> attachments;

    @Schema(description = "links")
    public TwinLinkListDTOv1 links;

    @Schema(description = "transitions")
    public List<TwinTransitionViewDTOv1> transitions;
}
