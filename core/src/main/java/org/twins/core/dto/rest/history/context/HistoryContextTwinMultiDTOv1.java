package org.twins.core.dto.rest.history.context;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.twin.TwinBaseDTOv1;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(fluent = true)
@Schema(name =  "HistoryContextTwinMultiV1")
public class HistoryContextTwinMultiDTOv1 implements HistoryContextDTO {
    public static final String KEY = "multiTwinV1";
    public String contextType = KEY;

    @Schema(description = "From twin id set")
    public Set<UUID> fromTwinIdSet;

    @Schema(description = "From twin set")
    public Set<TwinBaseDTOv1> fromTwinSet;

    @Schema(description = "To twin id set")
    public Set<UUID> toTwinIdSet;

    @Schema(description = "To twin set")
    public Set<TwinBaseDTOv1> toTwinSet;


}
