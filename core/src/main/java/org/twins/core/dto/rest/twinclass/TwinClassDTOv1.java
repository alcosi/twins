package org.twins.core.dto.rest.twinclass;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.datalist.DataListOptionDTOv1;
import org.twins.core.dto.rest.link.LinkDTOv1;
import org.twins.core.dto.rest.twin.TwinStatusDTOv1;
import org.twins.core.mappers.rest.MapperMode;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(fluent = true)
@Schema(name =  "TwinClassV1")
@MapperModeBinding(modes = MapperMode.ClassMode.class)
public class TwinClassDTOv1 extends TwinClassBaseDTOv1 {
    @Schema(description = "Class fields list")
    public List<TwinClassFieldDTOv1> fields;

    @Schema(description = "Class fields id list")
    public List<UUID> fieldIds;

    @Schema(description = "List of status id." + DTOExamples.LAZY_RELATION_MODE_OFF)
    public List<UUID> statusList;

    @Schema(description = "List of marker id." + DTOExamples.LAZY_RELATION_MODE_OFF)
    public List<UUID> markerList;

    @Schema(description = "List of tag id." + DTOExamples.LAZY_RELATION_MODE_OFF)
    public List<UUID> tagList;

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

    @Schema(description = "head class")
    public TwinClassBaseDTOv1 headClass;

    @Schema(description = "extends class")
    public TwinClassBaseDTOv1 extendsClass;
}
