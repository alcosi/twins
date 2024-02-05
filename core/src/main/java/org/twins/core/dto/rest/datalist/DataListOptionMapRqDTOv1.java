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
         [ "133b1c94-a8c0-4795-8076-10548ed772b3", "7de977d4-df6d-4250-9cb2-088363d139a1" ]
            """)
    public Set<UUID> dataListOptionIdSet;
}
