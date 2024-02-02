package org.twins.core.dto.rest.datalist;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(fluent = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "DataListOptionMapRqV1")
public class DataListOptionMapRqDTOv1 extends Request {
    @Schema(description = "data list option id set", example = """
         [ "074eaf50-3030-4bfe-b15a-d4f6e3f35270", "56d3b3d1-59ca-4a93-81d8-6d173674edfc" ]
            """)
    public Set<UUID> dataListOptionIdSet;
}
