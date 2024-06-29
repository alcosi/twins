package org.twins.core.dto.rest.twin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.dao.action.TwinAction;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.attachment.AttachmentViewDTOv1;
import org.twins.core.dto.rest.datalist.DataListOptionDTOv1;
import org.twins.core.dto.rest.link.TwinLinkListDTOv1;
import org.twins.core.dto.rest.twinflow.TwinTransitionViewDTOv1;
import org.twins.core.mappers.rest.MapperMode;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Schema(name =  "TwinBaseV3")
public class TwinBaseDTOv3 extends TwinBaseDTOv2 {
    @MapperModeBinding(modes = {MapperMode.TwinAttachmentMode.class, MapperMode.TwinAttachmentCollectionMode.class})
    @Schema(description = "attachments")
    public List<AttachmentViewDTOv1> attachments;

    @MapperModeBinding(modes = MapperMode.TwinLinkMode.class)
    @Schema(description = "links")
    public TwinLinkListDTOv1 links;

    @Schema(description = "TransitionId list." + DTOExamples.LAZY_RELATION_MODE_OFF)
    public Set<UUID> transitionsIdList;

    @MapperModeBinding(modes = MapperMode.TwinTransitionMode.class)
    @Schema(description = "Transition list." + DTOExamples.LAZY_RELATION_MODE_ON)
    public List<TwinTransitionViewDTOv1> transitions;

    @Schema(description = "MarkerId list." + DTOExamples.LAZY_RELATION_MODE_OFF)
    public Set<UUID> markerIdList;

    @MapperModeBinding(modes = MapperMode.TwinMarkerMode.class)
    @Schema(description = "Marker list." + DTOExamples.LAZY_RELATION_MODE_ON)
    public List<DataListOptionDTOv1> markers;

    @Schema(description = "TagId list."  + DTOExamples.LAZY_RELATION_MODE_OFF)
    public Set<UUID> tagIdList;

    @MapperModeBinding(modes = MapperMode.TwinTagMode.class)
    @Schema(description = "Tag list." + DTOExamples.LAZY_RELATION_MODE_ON)
    public List<DataListOptionDTOv1> tags;

    @MapperModeBinding(modes = MapperMode.TwinActionMode.class)
    @Schema(description = "Suitable actions list")
    public Set<TwinAction> actions;
}
