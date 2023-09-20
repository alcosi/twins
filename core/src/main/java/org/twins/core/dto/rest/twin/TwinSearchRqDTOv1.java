package org.twins.core.dto.rest.twin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.Request;
import org.twins.core.dto.rest.tql.TqlDTOv1;

import java.util.List;
import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name =  "TwinSearchRqV1")
public class TwinSearchRqDTOv1 extends Request {
    @Schema(description = "Twin class id list")
    public List<UUID> twinClassIdList;

    @Schema(description = "Space id list")
    public List<UUID> spaceTwinIdList;

    @Schema(description = "Status id list")
    public List<UUID> statusIdList;

    @Schema(description = "Assigner id list")
    public List<UUID> assignerUserIdList;

    @Schema(description = "Reporter id list")
    public List<UUID> createdByUserIdList;
}
