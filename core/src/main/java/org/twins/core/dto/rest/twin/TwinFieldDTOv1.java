package org.twins.core.dto.rest.twin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.twinclass.TwinClassFieldDTOv1;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(fluent = true)
@Schema(name = "TwinFieldV1")
public class TwinFieldDTOv1 extends TwinClassFieldDTOv1 {
    //    @Schema(description = "On of values", example = "", oneOf = {
//            TwinFieldValueTextDTOv1.class,
//            TwinFieldValueColorHexDTOv1.class,
//            TwinFieldValueDateDTOv1.class,
//            TwinFieldValueDataListOptionsDTOv1.class}, defaultValue = "fieldType")
    @Schema(description = "On of values", example = "")
    public TwinFieldValueDTO value;
}
