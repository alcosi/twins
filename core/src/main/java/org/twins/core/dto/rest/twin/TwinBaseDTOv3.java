package org.twins.core.dto.rest.twin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.attachment.AttachmentDTOv1;
import org.twins.core.dto.rest.attachment.AttachmentsCountDTOv1;
import org.twins.core.dto.rest.datalist.DataListOptionDTOv1;
import org.twins.core.dto.rest.link.TwinLinkListDTOv1;
import org.twins.core.dto.rest.related.RelatedObject;
import org.twins.core.dto.rest.twinclass.TwinClassDTOv1;
import org.twins.core.dto.rest.twinflow.TwinflowTransitionBaseDTOv1;
import org.twins.core.enums.action.TwinAction;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Schema(name =  "TwinBaseV3")
public class TwinBaseDTOv3 extends TwinBaseDTOv1 {
    @Schema(description = "Attachments")
    public List<AttachmentDTOv1> attachments;

    @Schema(description = "Attachments count")
    public AttachmentsCountDTOv1 attachmentsCount;

    @Schema(description = "Links")
    public TwinLinkListDTOv1 links;

    @Schema(description = "TransitionId list." + DTOExamples.LAZY_RELATION_MODE_OFF)
    @RelatedObject(type = TwinflowTransitionBaseDTOv1.class, name = "transitions")
    public Set<UUID> transitionsIdList;

    @Schema(description = "MarkerId list." + DTOExamples.LAZY_RELATION_MODE_OFF)
    @RelatedObject(type = DataListOptionDTOv1.class, name = "markers")
    public Set<UUID> markerIdList;

    @Schema(description = "TagId list."  + DTOExamples.LAZY_RELATION_MODE_OFF)
    @RelatedObject(type = DataListOptionDTOv1.class, name = "tags")
    public Set<UUID> tagIdList;

    @Schema(description = "Suitable actions list")
    public Set<TwinAction> actions;

    @Schema(description = "Twins of which classes are possible to create as children for given twin")
    @RelatedObject(type = TwinClassDTOv1.class, name = "creatableChildTwinClassList")
    public Set<UUID> creatableChildTwinClassIds;

    @Schema(description = "List of twin segments")
    @RelatedObject(type = TwinDTOv2.class, name = "segmentTwins")
    public Set<UUID> segmentTwinIdList;
}


