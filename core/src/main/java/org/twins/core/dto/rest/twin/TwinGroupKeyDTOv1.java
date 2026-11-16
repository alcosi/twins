package org.twins.core.dto.rest.twin;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.enums.twin.TwinGroupValueType;

@Data
@Accessors(chain = true)
@Schema(name = "TwinGroupKeyV1")
public class TwinGroupKeyDTOv1 {
    @Schema(description = "Kind of value. Selects the relatedObjects map: USER userMap, STATUS statusMap, TWIN_CLASS twinClassMap, TWIN twinMap, DATA_LIST_OPTION dataListsOptionMap. TEXT, DECIMAL, BOOLEAN and TIMESTAMP have no map")
    public TwinGroupValueType type;

    @JsonInclude(JsonInclude.Include.ALWAYS)
    @Schema(description = "Group key. Null is the empty group. For TWIN, USER, STATUS, TWIN_CLASS and DATA_LIST_OPTION this is the id in the matching relatedObjects map")
    public Object value;
}
