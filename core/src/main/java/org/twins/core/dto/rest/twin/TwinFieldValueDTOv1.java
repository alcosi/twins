package org.twins.core.dto.rest.twin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.twinclass.TwinClassDTOv1;
import org.twins.core.dto.rest.twinclass.TwinClassFieldDTOv1;
import org.twins.core.dto.rest.user.UserDTOv1;

import java.time.Instant;
import java.util.Hashtable;
import java.util.List;
import java.util.SortedMap;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(fluent = true)
@Schema(name = "TwinFieldValueV1")
public class TwinFieldValueDTOv1 extends TwinClassFieldDTOv1 {
    @Schema(description = "On of values", example = "", oneOf = {
            TwinFieldValueText.class,
            TwinFieldValueColorHex.class,
            TwinFieldValueDate.class,
            TwinFieldValueDataListOptions.class}, defaultValue = "fieldType")
    public TwinFieldValue value;
}
