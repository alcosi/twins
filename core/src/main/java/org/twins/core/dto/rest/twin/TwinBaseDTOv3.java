package org.twins.core.dto.rest.twin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dao.action.TwinAction;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.attachment.AttachmentDTOv1;
import org.twins.core.dto.rest.attachment.AttachmentsCountDTOv1;
import org.twins.core.dto.rest.datalist.DataListOptionDTOv1;
import org.twins.core.dto.rest.link.TwinLinkListDTOv1;
import org.twins.core.dto.rest.transition.TwinTransitionViewDTOv1;
import org.twins.core.dto.rest.twinclass.TwinClassDTOv1;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Schema(name =  "TwinBaseV3")
public class TwinBaseDTOv3 extends TwinBaseDTOv2 {
    @Schema(description = "Attachments")
    public List<AttachmentDTOv1> attachments;

    @Schema(description = "Attachments count")
    public AttachmentsCountDTOv1 attachmentsCount;

    @Schema(description = "Links")
    public TwinLinkListDTOv1 links;

    @Schema(description = "TransitionId list." + DTOExamples.LAZY_RELATION_MODE_OFF)
    public Set<UUID> transitionsIdList;

    @Schema(description = "Transition list." + DTOExamples.LAZY_RELATION_MODE_ON)
    public List<TwinTransitionViewDTOv1> transitions;

    @Schema(description = "MarkerId list." + DTOExamples.LAZY_RELATION_MODE_OFF)
    public Set<UUID> markerIdList;

    @Schema(description = "Marker list." + DTOExamples.LAZY_RELATION_MODE_ON)
    public List<DataListOptionDTOv1> markers;

    @Schema(description = "TagId list."  + DTOExamples.LAZY_RELATION_MODE_OFF)
    public Set<UUID> tagIdList;

    @Schema(description = "Tag list." + DTOExamples.LAZY_RELATION_MODE_ON)
    public List<DataListOptionDTOv1> tags;

    @Schema(description = "Suitable actions list")
    public Set<TwinAction> actions;

    @Schema(description = "Twins of which classes are possible to create as children for given twin")
    public Set<UUID> creatableChildTwinClassIds;

    @Schema(description = "Twins of which classes are possible to create as children for given twin")
    public List<TwinClassDTOv1> creatableChildTwinClasses;
}
