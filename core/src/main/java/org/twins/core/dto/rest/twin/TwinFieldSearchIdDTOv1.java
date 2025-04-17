package org.twins.core.dto.rest.twin;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.Set;
import java.util.UUID;

import static org.twins.core.dto.rest.twin.TwinFieldSearchTextDTOv1.KEY;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(fluent = true)
@Schema(name = KEY)
public class TwinFieldSearchIdDTOv1 extends TwinFieldSearchDTOv1 {

    public static final String KEY = "TwinFieldSearchIdV1";

    @JsonProperty("type")
    public String type = KEY;

    @Schema(description = "User id list")
    public Set<UUID> idList;

    @Schema(description = "User id exclude list")
    public Set<UUID> idExcludeList;
}
