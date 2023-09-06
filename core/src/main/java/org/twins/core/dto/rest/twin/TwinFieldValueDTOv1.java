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
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(fluent = true)
@Schema(name =  "TwinV1")
public class TwinFieldValueDTOv1 extends TwinClassFieldDTOv1 {
    @Schema(description = "Values sorted list. Count of values depends upon field type", example = "")
    public List<Object> values;
}
