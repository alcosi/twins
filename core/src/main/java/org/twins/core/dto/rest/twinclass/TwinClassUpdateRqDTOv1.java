package org.twins.core.dto.rest.twinclass;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.Map;
import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name =  "TwinClassUpdateRqV1")
public class TwinClassUpdateRqDTOv1 extends TwinClassSaveRqDTOv1 {
    @Schema(description = "[optional] if marker data list is changed during update, you should pass map [old_marker_id -> new_marker_id]")
    public Map<UUID, UUID> markersReplaceMap;

    @JsonIgnore
    public UUID twinClassId;
}
