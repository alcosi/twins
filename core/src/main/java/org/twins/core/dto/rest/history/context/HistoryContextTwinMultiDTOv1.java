package org.twins.core.dto.rest.history.context;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.twin.TwinBaseDTOv1;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(fluent = true)
@Schema(name =  HistoryContextTwinMultiDTOv1.KEY)
public class HistoryContextTwinMultiDTOv1 implements HistoryContextDTO {

    public static final String KEY = "HistoryContextTwinMultiV1";

    public HistoryContextTwinMultiDTOv1() {
        this.contextType = KEY;
    }

    @Schema(description = "Context type", allowableValues = {KEY}, example = KEY, requiredMode = Schema.RequiredMode.REQUIRED)
    public String contextType;

    @Schema(description = "From twin id set")
    public Set<UUID> fromTwinIdSet;

    @Schema(description = "From twin set")
    public Set<TwinBaseDTOv1> fromTwinSet;

    @Schema(description = "To twin id set")
    public Set<UUID> toTwinIdSet;

    @Schema(description = "To twin set")
    public Set<TwinBaseDTOv1> toTwinSet;

}
