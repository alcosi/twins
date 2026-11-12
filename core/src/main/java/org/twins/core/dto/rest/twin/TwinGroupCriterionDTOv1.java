package org.twins.core.dto.rest.twin;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.enums.twin.TwinGroupCriterionType;
import org.twins.core.enums.twin.TwinGroupSort;
import org.twins.core.enums.twin.TwinLinkNodeEnd;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "TwinGroupCriterionV1")
public class TwinGroupCriterionDTOv1 {
    @NotNull
    @Schema(description = "FIELD groups by a twin column or a countable dynamic field. LINK groups by the other end of twin_link")
    public TwinGroupCriterionType type;

    @Schema(description = "Twin class field id. Required when type is FIELD")
    public UUID fieldId;

    @Schema(description = "Link id. Required when type is LINK")
    public UUID linkId;

    @Schema(description = "Which end of the link is the node. DST: node is dst_twin_id and the leaf is src. SRC: the opposite")
    public TwinLinkNodeEnd nodeEnd;

    @Schema(description = "Order of nodes at this level. Default is NAME_ASC for assignee, owner, creator, head and link, KEY_ASC otherwise")
    public TwinGroupSort sort;
}
