package org.twins.core.dto.rest.twinclass;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
@Schema(name =  "TwinClassFieldConditonDescriptorValueV1")
public class TwinClassFieldConditonDescriptorValueDTOv1 extends TwinClassFieldConditionDescriptorBasicDTOv1 {
    public static final String KEY = "value";

    @Override
    public String conditionType() {
        return KEY;
    }
}
