package org.twins.core.dto.rest.twinclass;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.datalist.DataListOptionDTOv1;
import org.twins.core.dto.rest.featurer.FeaturerDTOv1;
import org.twins.core.dto.rest.link.LinkDTOv1;
import org.twins.core.dto.rest.twinstatus.TwinStatusDTOv1;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Schema(name =  "TwinClassV1")
public class TwinClassDTOv1 extends TwinClassBaseDTOv1 {
    @Schema(description = "Class fields list")
    public List<TwinClassFieldDTOv1> fields;

    @Schema()
    public Map<UUID, LinkDTOv1> forwardLinkMap;

    @Schema()
    public Map<UUID, LinkDTOv1> backwardLinkMap;

    @Schema(description = "Map of statuses." + DTOExamples.LAZY_RELATION_MODE_ON)
    public Map<UUID, TwinStatusDTOv1> statusMap;

    @Schema(description = "Map of markers." + DTOExamples.LAZY_RELATION_MODE_ON)
    public Map<UUID, DataListOptionDTOv1> markerMap;

    @Schema(description = "Map of tags." + DTOExamples.LAZY_RELATION_MODE_ON)
    public Map<UUID, DataListOptionDTOv1> tagMap;

    @Schema(description = "extends class")
    public TwinClassBaseDTOv1 extendsClass;

    @Schema(description = "head hunter featurer")
    public FeaturerDTOv1 headHunterFeaturer;

    @Schema(description = "twin class freeze")
    public TwinClassFreezeDTOv1 twinClassFreeze;
}
