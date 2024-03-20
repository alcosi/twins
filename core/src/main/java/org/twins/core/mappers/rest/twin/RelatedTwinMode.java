package org.twins.core.mappers.rest.twin;

import lombok.Getter;
import org.twins.core.mappers.rest.MapperMode;
import org.twins.core.mappers.rest.MapperModeCollection;
import org.twins.core.mappers.rest.attachment.AttachmentViewRestDTOMapper;
import org.twins.core.mappers.rest.link.LinkRestDTOMapper;
import org.twins.core.mappers.rest.link.TwinLinkRestDTOMapper;
import org.twins.core.mappers.rest.twinclass.TwinClassBaseRestDTOMapper;
import org.twins.core.mappers.rest.twinclass.TwinClassFieldRestDTOMapper;
import org.twins.core.mappers.rest.twinclass.TwinClassRestDTOMapper;
import org.twins.core.mappers.rest.twinflow.TwinTransitionRestDTOMapper;

@Getter
public enum RelatedTwinMode implements MapperModeCollection {
    GREEN(0,
            TwinBaseRestDTOMapper.TwinMode.SHORT,
            TwinBaseV2RestDTOMapper.TwinHeadMode.HIDE,
            TwinBaseV3RestDTOMapper.TwinMarkerMode.HIDE,
            TwinBaseV3RestDTOMapper.TwinTagMode.HIDE,
            TwinRestDTOMapper.FieldsMode.NO_FIELDS,
            AttachmentViewRestDTOMapper.Mode.HIDE,
            TwinClassFieldRestDTOMapper.Mode.HIDE,
            TwinClassBaseRestDTOMapper.ClassMode.SHORT,
            TwinClassRestDTOMapper.HeadTwinMode.HIDE,
            TwinClassRestDTOMapper.StatusMode.HIDE,
            TwinClassRestDTOMapper.MarkerMode.HIDE,
            TwinClassRestDTOMapper.TagMode.HIDE,
            TwinLinkRestDTOMapper.Mode.HIDE,
            LinkRestDTOMapper.Mode.HIDE,
            TwinTransitionRestDTOMapper.Mode.HIDE),
    YELLOW(1,
            TwinBaseRestDTOMapper.TwinMode.DETAILED,
            TwinBaseV2RestDTOMapper.TwinHeadMode.HIDE,
            TwinBaseV3RestDTOMapper.TwinMarkerMode.HIDE,
            TwinBaseV3RestDTOMapper.TwinTagMode.HIDE,
            TwinRestDTOMapper.FieldsMode.NO_FIELDS,
            AttachmentViewRestDTOMapper.Mode.HIDE,
            TwinClassFieldRestDTOMapper.Mode.HIDE,
            TwinClassBaseRestDTOMapper.ClassMode.SHORT,
            TwinClassRestDTOMapper.HeadTwinMode.HIDE,
            TwinClassRestDTOMapper.StatusMode.HIDE,
            TwinClassRestDTOMapper.MarkerMode.HIDE,
            TwinClassRestDTOMapper.TagMode.HIDE,
            TwinLinkRestDTOMapper.Mode.HIDE,
            LinkRestDTOMapper.Mode.HIDE,
            TwinTransitionRestDTOMapper.Mode.HIDE),
    BLUE(1,
            TwinBaseRestDTOMapper.TwinMode.DETAILED,
            TwinBaseV2RestDTOMapper.TwinHeadMode.HIDE,
            TwinBaseV3RestDTOMapper.TwinMarkerMode.HIDE,
            TwinBaseV3RestDTOMapper.TwinTagMode.HIDE,
            TwinRestDTOMapper.FieldsMode.NOT_EMPTY_FIELDS,
            AttachmentViewRestDTOMapper.Mode.HIDE,
            TwinClassFieldRestDTOMapper.Mode.HIDE,
            TwinClassBaseRestDTOMapper.ClassMode.SHORT,
            TwinClassRestDTOMapper.HeadTwinMode.HIDE,
            TwinClassRestDTOMapper.StatusMode.HIDE,
            TwinClassRestDTOMapper.MarkerMode.HIDE,
            TwinClassRestDTOMapper.TagMode.HIDE,
            TwinLinkRestDTOMapper.Mode.HIDE,
            LinkRestDTOMapper.Mode.HIDE,
            TwinTransitionRestDTOMapper.Mode.HIDE),
    BLACK(1,
            TwinBaseRestDTOMapper.TwinMode.DETAILED,
            TwinBaseV2RestDTOMapper.TwinHeadMode.HIDE,
            TwinBaseV3RestDTOMapper.TwinMarkerMode.HIDE,
            TwinBaseV3RestDTOMapper.TwinTagMode.HIDE,
            TwinRestDTOMapper.FieldsMode.NOT_EMPTY_FIELDS,
            AttachmentViewRestDTOMapper.Mode.HIDE,
            TwinClassFieldRestDTOMapper.Mode.HIDE,
            TwinClassBaseRestDTOMapper.ClassMode.DETAILED,
            TwinClassRestDTOMapper.HeadTwinMode.HIDE,
            TwinClassRestDTOMapper.StatusMode.HIDE,
            TwinClassRestDTOMapper.MarkerMode.HIDE,
            TwinClassRestDTOMapper.TagMode.HIDE,
            TwinLinkRestDTOMapper.Mode.HIDE,
            LinkRestDTOMapper.Mode.HIDE,
            TwinTransitionRestDTOMapper.Mode.HIDE),
    ORANGE(1,
            TwinBaseRestDTOMapper.TwinMode.DETAILED,
            TwinBaseV2RestDTOMapper.TwinHeadMode.HIDE,
            TwinBaseV3RestDTOMapper.TwinMarkerMode.HIDE,
            TwinBaseV3RestDTOMapper.TwinTagMode.HIDE,
            TwinRestDTOMapper.FieldsMode.NOT_EMPTY_FIELDS,
            AttachmentViewRestDTOMapper.Mode.HIDE,
            TwinClassFieldRestDTOMapper.Mode.DETAILED,
            TwinClassBaseRestDTOMapper.ClassMode.DETAILED,
            TwinClassRestDTOMapper.HeadTwinMode.HIDE,
            TwinClassRestDTOMapper.StatusMode.HIDE,
            TwinClassRestDTOMapper.MarkerMode.HIDE,
            TwinClassRestDTOMapper.TagMode.HIDE,
            TwinLinkRestDTOMapper.Mode.HIDE,
            LinkRestDTOMapper.Mode.HIDE,
            TwinTransitionRestDTOMapper.Mode.HIDE);

    public static final String _GREEN = "GREEN";
    public static final String _YELLOW = "YELLOW";

    final int priority;
    final MapperMode[] configuredModes;

    RelatedTwinMode(int priority, MapperMode... configuredModes) {
        this.priority = priority;
        this.configuredModes = configuredModes;
    }
}
