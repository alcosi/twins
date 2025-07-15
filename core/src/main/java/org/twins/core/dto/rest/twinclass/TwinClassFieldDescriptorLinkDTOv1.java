package org.twins.core.dto.rest.twinclass;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.twin.TwinBaseDTOv2;

import java.util.ArrayList;
import java.util.List;

@Data
@Accessors(fluent = true)
@Schema(name =  TwinClassFieldDescriptorLinkDTOv1.KEY)
public class TwinClassFieldDescriptorLinkDTOv1 implements TwinClassFieldDescriptorDTO {

    public static final String KEY = "TwinClassFieldDescriptorLinkV1";

    public TwinClassFieldDescriptorLinkDTOv1() {
        this.fieldType = KEY;
    }

    @Schema(description = "Field type", allowableValues = {KEY}, example = KEY, requiredMode = Schema.RequiredMode.REQUIRED)
    public String fieldType;

    @Schema(description = "Multiple choice support", example = "true")
    public Boolean multiple;

    @Schema(description = "Valid options", example = "")
    public List<TwinBaseDTOv2> dstTwins = new ArrayList<>();

    public TwinClassFieldDescriptorLinkDTOv1 add(TwinBaseDTOv2 dstTwin) {
        dstTwins.add(dstTwin);
        return this;
    }
}
