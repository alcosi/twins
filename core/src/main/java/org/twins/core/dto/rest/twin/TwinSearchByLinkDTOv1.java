package org.twins.core.dto.rest.twin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.related.RelatedObject;

import java.util.List;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name =  "TwinSearchByLinkV1")
public class TwinSearchByLinkDTOv1 {
    @Schema(description = "Twin-link class id")
    @RelatedObject(type = TwinDTOv2.class, name = "link")
    public UUID linkId;

    @Schema(description = "Twin dest ids for in(ex)clude from search")
    @Deprecated
    public List<UUID> dstTwinIdList;

    @Schema(description = "Twin src or dest ids for in(ex)clude from search")
    public List<UUID> twinIdList;

    @Schema(description = "search direction")
    public boolean srcElseDst;
}


