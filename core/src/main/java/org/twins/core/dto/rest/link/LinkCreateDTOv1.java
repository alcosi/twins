package org.twins.core.dto.rest.link;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name =  "LinkCreateV1")
public class LinkCreateDTOv1 extends LinkSaveDTOv1 {

    @Schema(description = "Source twin class id")
    public UUID srcTwinClassId;

    @Schema(description = "Destination twin class id")
    public UUID dstTwinClassId;



}
