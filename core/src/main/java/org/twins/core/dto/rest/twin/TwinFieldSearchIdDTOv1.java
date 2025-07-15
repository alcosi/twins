package org.twins.core.dto.rest.twin;

import com.fasterxml.jackson.annotation.JsonTypeName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(fluent = true)
@Schema(name = TwinFieldSearchIdDTOv1.KEY)
public class TwinFieldSearchIdDTOv1 implements TwinFieldSearchDTOv1 {

    public static final String KEY = "TwinFieldSearchIdV1";

    public TwinFieldSearchIdDTOv1() {
        this.type = KEY;
    }

    @Schema(description = "Search type", allowableValues = {KEY}, example = KEY, requiredMode = Schema.RequiredMode.REQUIRED)
    public String type;

    @Schema(description = "User id list")
    public Set<UUID> idList;

    @Schema(description = "User id exclude list")
    public Set<UUID> idExcludeList;
}
