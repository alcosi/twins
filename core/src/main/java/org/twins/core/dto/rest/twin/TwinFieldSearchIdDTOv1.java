package org.twins.core.dto.rest.twin;

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
    public String type = KEY;

    @Schema(description = "User id list")
    public Set<UUID> idList;

    @Schema(description = "User id exclude list")
    public Set<UUID> idExcludeList;
}
