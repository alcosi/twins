package org.twins.core.dto.rest.twin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

import java.util.List;
import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name =  "TwinSearchRqV1")
public class TwinSearchRqDTOv1 extends Request {
    @Schema(description = "Twin class id list")
    public List<UUID> twinClassIdList;

    @Schema(description = "Twin class extends id list")
    public List<UUID> extendsTwinClassIdList;

    @Schema(description = "Head twin id list")
    public List<UUID> headTwinIdList;

    @Schema(description = "Status id list")
    public List<UUID> statusIdList;

    @Schema(description = "Assigner id list")
    public List<UUID> assignerUserIdList;

    @Schema(description = "Reporter id list")
    public List<UUID> createdByUserIdList;

    @Schema(description = "Reporter id list")
    public List<TwinSearchByLinkDTOv1> linksList;
}
