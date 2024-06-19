package org.twins.core.mappers.rest.twin;

import lombok.Getter;
import org.twins.core.mappers.rest.MapperMode;
import org.twins.core.mappers.rest.MapperModeCollection;
import org.twins.core.mappers.rest.link.LinkRestDTOMapper;
import org.twins.core.mappers.rest.link.TwinLinkRestDTOMapper;
import org.twins.core.mappers.rest.twinclass.TwinClassBaseRestDTOMapper;
import org.twins.core.mappers.rest.twinclass.TwinClassFieldRestDTOMapper;
import org.twins.core.mappers.rest.twinclass.TwinClassRestDTOMapper;

@Getter
public enum RelatedByHeadTwinMode implements MapperModeCollection {
    WHITE(0,
            TwinBaseRestDTOMapper.TwinMode.HIDE,
            MapperMode.TwinMarkerMode.HIDE,
            MapperMode.TwinTagMode.HIDE,
            TwinRestDTOMapper.FieldsMode.NO_FIELDS,
            AttachmentMode.HIDE,
            TwinClassFieldRestDTOMapper.Mode.HIDE,
            TwinClassBaseRestDTOMapper.ClassMode.HIDE,
            TwinClassRestDTOMapper.StatusMode.HIDE,
            TwinClassRestDTOMapper.MarkerMode.HIDE,
            TwinClassRestDTOMapper.TagMode.HIDE,
            TwinLinkRestDTOMapper.Mode.HIDE,
            LinkRestDTOMapper.Mode.HIDE,
            MapperMode.TransitionMode.HIDE),
    GREEN(1,
            TwinBaseRestDTOMapper.TwinMode.SHORT,
            MapperMode.TwinMarkerMode.HIDE,
            MapperMode.TwinTagMode.HIDE,
            TwinRestDTOMapper.FieldsMode.NO_FIELDS,
            AttachmentMode.HIDE,
            TwinClassFieldRestDTOMapper.Mode.HIDE,
            TwinClassBaseRestDTOMapper.ClassMode.SHORT,
            TwinClassRestDTOMapper.StatusMode.HIDE,
            TwinClassRestDTOMapper.MarkerMode.HIDE,
            TwinClassRestDTOMapper.TagMode.HIDE,
            TwinLinkRestDTOMapper.Mode.HIDE,
            LinkRestDTOMapper.Mode.HIDE,
            MapperMode.TransitionMode.HIDE),
    FOREST_GREEN(1,
            TwinBaseRestDTOMapper.TwinMode.SHORT,
            MapperMode.TwinMarkerMode.HIDE,
            MapperMode.TwinTagMode.DETAILED,
            TwinRestDTOMapper.FieldsMode.NO_FIELDS,
            AttachmentMode.HIDE,
            TwinClassFieldRestDTOMapper.Mode.HIDE,
            TwinClassBaseRestDTOMapper.ClassMode.SHORT,
            TwinClassRestDTOMapper.StatusMode.HIDE,
            TwinClassRestDTOMapper.MarkerMode.HIDE,
            TwinClassRestDTOMapper.TagMode.HIDE,
            TwinLinkRestDTOMapper.Mode.HIDE,
            LinkRestDTOMapper.Mode.HIDE,
            MapperMode.TransitionMode.HIDE),
    YELLOW(2,
            TwinBaseRestDTOMapper.TwinMode.DETAILED,
            MapperMode.TwinMarkerMode.HIDE,
            MapperMode.TwinTagMode.HIDE,
            TwinRestDTOMapper.FieldsMode.NO_FIELDS,
            AttachmentMode.HIDE,
            TwinClassFieldRestDTOMapper.Mode.HIDE,
            TwinClassBaseRestDTOMapper.ClassMode.SHORT,
            TwinClassRestDTOMapper.StatusMode.HIDE,
            TwinClassRestDTOMapper.MarkerMode.HIDE,
            TwinClassRestDTOMapper.TagMode.HIDE,
            TwinLinkRestDTOMapper.Mode.HIDE,
            LinkRestDTOMapper.Mode.HIDE,
            MapperMode.TransitionMode.HIDE),
    BLUE(3,
            TwinBaseRestDTOMapper.TwinMode.DETAILED,
            MapperMode.TwinMarkerMode.HIDE,
            MapperMode.TwinTagMode.HIDE,
            TwinRestDTOMapper.FieldsMode.NOT_EMPTY_FIELDS,
            AttachmentMode.HIDE,
            TwinClassFieldRestDTOMapper.Mode.HIDE,
            TwinClassBaseRestDTOMapper.ClassMode.SHORT,
            TwinClassRestDTOMapper.StatusMode.HIDE,
            TwinClassRestDTOMapper.MarkerMode.HIDE,
            TwinClassRestDTOMapper.TagMode.HIDE,
            TwinLinkRestDTOMapper.Mode.HIDE,
            LinkRestDTOMapper.Mode.HIDE,
            MapperMode.TransitionMode.HIDE),
    BLACK(4,
            TwinBaseRestDTOMapper.TwinMode.DETAILED,
            MapperMode.TwinMarkerMode.HIDE,
            MapperMode.TwinTagMode.HIDE,
            TwinRestDTOMapper.FieldsMode.NOT_EMPTY_FIELDS,
            AttachmentMode.HIDE,
            TwinClassFieldRestDTOMapper.Mode.HIDE,
            TwinClassBaseRestDTOMapper.ClassMode.DETAILED,
            TwinClassRestDTOMapper.StatusMode.HIDE,
            TwinClassRestDTOMapper.MarkerMode.HIDE,
            TwinClassRestDTOMapper.TagMode.HIDE,
            TwinLinkRestDTOMapper.Mode.HIDE,
            LinkRestDTOMapper.Mode.HIDE,
            MapperMode.TransitionMode.HIDE),
    GRAY(4,
            TwinBaseRestDTOMapper.TwinMode.DETAILED,
            MapperMode.TwinMarkerMode.HIDE,
            MapperMode.TwinTagMode.DETAILED,
            TwinRestDTOMapper.FieldsMode.NOT_EMPTY_FIELDS,
            AttachmentMode.HIDE,
            TwinClassFieldRestDTOMapper.Mode.HIDE,
            TwinClassBaseRestDTOMapper.ClassMode.DETAILED,
            TwinClassRestDTOMapper.StatusMode.HIDE,
            TwinClassRestDTOMapper.MarkerMode.HIDE,
            TwinClassRestDTOMapper.TagMode.HIDE,
            TwinLinkRestDTOMapper.Mode.HIDE,
            LinkRestDTOMapper.Mode.HIDE,
            MapperMode.TransitionMode.HIDE),
    ORANGE(5,
            TwinBaseRestDTOMapper.TwinMode.DETAILED,
            MapperMode.TwinMarkerMode.HIDE,
            MapperMode.TwinTagMode.HIDE,
            TwinRestDTOMapper.FieldsMode.NOT_EMPTY_FIELDS,
            AttachmentMode.HIDE,
            TwinClassFieldRestDTOMapper.Mode.DETAILED,
            TwinClassBaseRestDTOMapper.ClassMode.DETAILED,
            TwinClassRestDTOMapper.StatusMode.HIDE,
            TwinClassRestDTOMapper.MarkerMode.HIDE,
            TwinClassRestDTOMapper.TagMode.HIDE,
            TwinLinkRestDTOMapper.Mode.HIDE,
            LinkRestDTOMapper.Mode.HIDE,
            MapperMode.TransitionMode.HIDE);

    public static final String _WHITE = "WHITE";
    public static final String _GREEN = "GREEN";
    public static final String _YELLOW = "YELLOW";

    final int priority;
    final MapperMode[] configuredModes;

    RelatedByHeadTwinMode(int priority, MapperMode... configuredModes) {
        this.priority = priority;
        this.configuredModes = configuredModes;
    }
}
