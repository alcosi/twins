package org.twins.core.dto.rest.link;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.attachment.AttachmentBaseDTOv1;
import org.twins.core.dto.rest.twin.TwinDTOv2;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name =  "TwinLinkAddV1")
public class TwinLinkAddDTOv1 {
    @Schema(description = "Link id", example = DTOExamples.LINK_ID)
    public UUID linkId;

    @Schema(description = "Destination twin id", example = DTOExamples.TWIN_ID)
    public UUID dstTwinId;
}
