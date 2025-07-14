package org.twins.core.dto.rest.twinclass;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors(fluent = true)
@Schema(name =  TwinClassFieldDescriptorLinkLongDTOv1.KEY, oneOf = { TwinClassFieldDescriptorDTO.class })
public class TwinClassFieldDescriptorLinkLongDTOv1 implements TwinClassFieldDescriptorDTO {

    public static final String KEY = "TwinClassFieldDescriptorLinkLongV1";

    public TwinClassFieldDescriptorLinkLongDTOv1() {
        this.fieldType = KEY;
    }

    @Schema(description = "Field type", allowableValues = {KEY}, example = KEY, requiredMode = Schema.RequiredMode.REQUIRED)
    public String fieldType;

    @Schema(description = "Multiple choice support", example = "true")
    public Boolean multiple;

    @Schema(description = "Link id for grabbing valid dst twins", example = "")
    public UUID linkId;
}
