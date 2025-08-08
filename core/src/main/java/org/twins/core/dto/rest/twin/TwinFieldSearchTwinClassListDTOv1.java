package org.twins.core.dto.rest.twin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Set;
import java.util.UUID;


@Data
@Accessors(fluent = true)
@Schema(name = TwinFieldSearchTwinClassListDTOv1.KEY)
public class TwinFieldSearchTwinClassListDTOv1 implements TwinFieldSearchDTOv1 {

    public static final String KEY = "TwinFieldSearchTwinClassListV1";

    @Override
    public String type() {
        return KEY;
    }

    @Schema(description = "Twin class id include all set")
    public Set<UUID> idIncludeAllSet;

    @Schema(description = "Twin class id exclude all set")
    public Set<UUID> idExcludeAllSet;

    @Schema(description = "Twin class id include any set")
    public Set<UUID> idIncludeAnySet;

    @Schema(description = "Twin class id exclude any set")
    public Set<UUID> idExcludeAnySet;
}
