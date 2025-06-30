package org.twins.core.dto.rest.twinclass;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.twin.TwinBaseDTOv2;

import java.util.ArrayList;
import java.util.List;

@Data
@Accessors(fluent = true)
@Schema(name =  "TwinClassFieldDescriptorLinkV1")
public class TwinClassFieldDescriptorLinkDTOv1 implements TwinClassFieldDescriptorDTO {
    public static final String KEY = "selectLinkV1";
    @Override
    public String fieldType() {
        return KEY;
    }

    @Schema(description = "Multiple choice support", example = "true")
    public Boolean multiple;

    @Schema(description = "Valid options", example = "")
    public List<TwinBaseDTOv2> dstTwins = new ArrayList<>();

    public TwinClassFieldDescriptorLinkDTOv1 add(TwinBaseDTOv2 dstTwin) {
        dstTwins.add(dstTwin);
        return this;
    }
}
