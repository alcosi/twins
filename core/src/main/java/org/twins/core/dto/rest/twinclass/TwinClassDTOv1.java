package org.twins.core.dto.rest.twinclass;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.link.LinkDTOv1;
import org.twins.core.dto.rest.twin.TwinDTOv1;
import org.twins.core.dto.rest.twin.TwinStatusDTOv1;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(fluent = true)
@Schema(name =  "TwinClassV1")
public class TwinClassDTOv1 extends TwinClassBaseDTOv1 {
    @Schema(description = "list of available heads")
    public List<TwinDTOv1> validHeads;

    @Schema(description = "list of available heads")
    public List<UUID> validHeadsIds;

    @Schema(description = "Class fields list")
    public List<TwinClassFieldDTOv1> fields;

    @Schema(description = "Class fields id list")
    public List<UUID> fieldIds;

    @Schema()
    public Map<UUID, LinkDTOv1> forwardLinkMap;

    @Schema()
    public Map<UUID, LinkDTOv1> backwardLinkMap;

    @Schema()
    public Map<UUID, TwinStatusDTOv1> statusMap;
}
