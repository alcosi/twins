package org.twins.core.dto.rest.twinclass;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Accessors(fluent = true)
@Schema(name =  "TwinClassFieldDescriptorAttachmentV1")
public class TwinClassFieldDescriptorAttachmentDTOv1 implements TwinClassFieldDescriptorDTO {
    public static final String KEY = "attachmentFieldV1";
    public String fieldType = KEY;

    @Schema(description = "restriction Id")
    public UUID restrictionId;
}
