package org.twins.core.mappers.rest.mappercontext.modes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldNameConstants;
import org.twins.core.mappers.rest.mappercontext.MapperMode;
import org.twins.core.mappers.rest.mappercontext.MapperModeCollection;
import org.twins.core.mappers.rest.mappercontext.MapperModePointer;

@Getter
@FieldNameConstants(onlyExplicitlyIncluded = true)
public enum RelationTwinMode implements MapperModeCollection {
    @FieldNameConstants.Include WHITE(0,
            TwinMode.HIDE,
            DataListOptionMode.TwinMarker2DataListOptionMode.HIDE,
            DataListOptionMode.TwinTag2DataListOptionMode.HIDE,
            TwinFieldCollectionMode.NO_FIELDS,
            AttachmentMode.HIDE,
            TwinClassFieldMode.TwinClass2TwinClassFieldMode.HIDE,
            TwinClassMode.Twin2TwinClassMode.HIDE,
            StatusMode.TwinClass2StatusMode.HIDE,
            DataListOptionMode.TwinClassMarker2DataListOptionMode.HIDE,
            DataListOptionMode.TwinClassTag2DataListOptionMode.HIDE,
            TwinLinkMode.Twin2TwinLinkMode.HIDE,
            LinkMode.TwinLink2LinkMode.HIDE,
            TransitionMode.HIDE),
    @FieldNameConstants.Include GREEN(1,
            TwinMode.SHORT,
            DataListOptionMode.TwinMarker2DataListOptionMode.HIDE,
            DataListOptionMode.TwinTag2DataListOptionMode.HIDE,
            TwinFieldCollectionMode.NO_FIELDS,
            AttachmentMode.HIDE,
            TwinClassFieldMode.TwinClass2TwinClassFieldMode.HIDE,
            TwinClassMode.Twin2TwinClassMode.SHORT,
            StatusMode.TwinClass2StatusMode.HIDE,
            DataListOptionMode.TwinClassMarker2DataListOptionMode.HIDE,
            DataListOptionMode.TwinClassTag2DataListOptionMode.HIDE,
            TwinLinkMode.Twin2TwinLinkMode.HIDE,
            LinkMode.TwinLink2LinkMode.HIDE,
            TransitionMode.HIDE),
    @FieldNameConstants.Include FOREST_GREEN(1,
            TwinMode.SHORT,
            DataListOptionMode.TwinMarker2DataListOptionMode.HIDE,
            DataListOptionMode.TwinTag2DataListOptionMode.DETAILED,
            TwinFieldCollectionMode.NO_FIELDS,
            AttachmentMode.HIDE,
            TwinClassFieldMode.TwinClass2TwinClassFieldMode.HIDE,
            TwinClassMode.Twin2TwinClassMode.SHORT,
            StatusMode.TwinClass2StatusMode.HIDE,
            DataListOptionMode.TwinClassMarker2DataListOptionMode.HIDE,
            DataListOptionMode.TwinClassTag2DataListOptionMode.HIDE,
            TwinLinkMode.Twin2TwinLinkMode.HIDE,
            LinkMode.TwinLink2LinkMode.HIDE,
            TransitionMode.HIDE),
    @FieldNameConstants.Include YELLOW(2,
            TwinMode.DETAILED,
            DataListOptionMode.TwinMarker2DataListOptionMode.HIDE,
            DataListOptionMode.TwinTag2DataListOptionMode.HIDE,
            TwinFieldCollectionMode.NO_FIELDS,
            AttachmentMode.HIDE,
            TwinClassFieldMode.TwinClass2TwinClassFieldMode.HIDE,
            TwinClassMode.Twin2TwinClassMode.SHORT,
            StatusMode.TwinClass2StatusMode.HIDE,
            DataListOptionMode.TwinClassMarker2DataListOptionMode.HIDE,
            DataListOptionMode.TwinClassTag2DataListOptionMode.HIDE,
            TwinLinkMode.Twin2TwinLinkMode.HIDE,
            LinkMode.TwinLink2LinkMode.HIDE,
            TransitionMode.HIDE),
    @FieldNameConstants.Include BLUE(3,
            TwinMode.DETAILED,
            DataListOptionMode.TwinMarker2DataListOptionMode.HIDE,
            DataListOptionMode.TwinTag2DataListOptionMode.HIDE,
            TwinFieldCollectionMode.NOT_EMPTY_FIELDS,
            DataListOptionMode.TwinField2DataListOptionMode.HIDE,
            UserMode.TwinField2UserMode.HIDE,
            TwinMode.TwinField2TwinMode.HIDE,
            AttachmentMode.HIDE,
            TwinClassFieldMode.TwinClass2TwinClassFieldMode.HIDE,
            TwinClassMode.Twin2TwinClassMode.SHORT,
            StatusMode.TwinClass2StatusMode.HIDE,
            DataListOptionMode.TwinClassMarker2DataListOptionMode.HIDE,
            DataListOptionMode.TwinClassTag2DataListOptionMode.HIDE,
            TwinLinkMode.Twin2TwinLinkMode.HIDE,
            LinkMode.TwinLink2LinkMode.HIDE,
            TransitionMode.HIDE),
    @FieldNameConstants.Include BLACK(4,
            TwinMode.DETAILED,
            DataListOptionMode.TwinMarker2DataListOptionMode.HIDE,
            DataListOptionMode.TwinTag2DataListOptionMode.HIDE,
            TwinFieldCollectionMode.NOT_EMPTY_FIELDS,
            DataListOptionMode.TwinField2DataListOptionMode.HIDE,
            UserMode.TwinField2UserMode.HIDE,
            TwinMode.TwinField2TwinMode.HIDE,
            AttachmentMode.HIDE,
            TwinClassFieldMode.TwinClass2TwinClassFieldMode.HIDE,
            TwinClassMode.Twin2TwinClassMode.DETAILED,
            StatusMode.TwinClass2StatusMode.HIDE,
            DataListOptionMode.TwinClassMarker2DataListOptionMode.HIDE,
            DataListOptionMode.TwinClassTag2DataListOptionMode.HIDE,
            TwinLinkMode.Twin2TwinLinkMode.HIDE,
            LinkMode.TwinLink2LinkMode.HIDE,
            TransitionMode.HIDE),
    @FieldNameConstants.Include GRAY(4,
            TwinMode.DETAILED,
            DataListOptionMode.TwinMarker2DataListOptionMode.HIDE,
            DataListOptionMode.TwinTag2DataListOptionMode.DETAILED,
            TwinFieldCollectionMode.NOT_EMPTY_FIELDS,
            DataListOptionMode.TwinField2DataListOptionMode.HIDE,
            UserMode.TwinField2UserMode.HIDE,
            TwinMode.TwinField2TwinMode.HIDE,
            AttachmentMode.HIDE,
            TwinClassFieldMode.TwinClass2TwinClassFieldMode.HIDE,
            TwinClassMode.Twin2TwinClassMode.DETAILED,
            StatusMode.TwinClass2StatusMode.HIDE,
            DataListOptionMode.TwinClassMarker2DataListOptionMode.HIDE,
            DataListOptionMode.TwinClassTag2DataListOptionMode.HIDE,
            TwinLinkMode.Twin2TwinLinkMode.HIDE,
            LinkMode.TwinLink2LinkMode.HIDE,
            TransitionMode.HIDE),
    @FieldNameConstants.Include ORANGE(5,
            TwinMode.DETAILED,
            DataListOptionMode.TwinMarker2DataListOptionMode.HIDE,
            DataListOptionMode.TwinTag2DataListOptionMode.HIDE,
            TwinFieldCollectionMode.NOT_EMPTY_FIELDS,
            DataListOptionMode.TwinField2DataListOptionMode.HIDE,
            UserMode.TwinField2UserMode.HIDE,
            TwinMode.TwinField2TwinMode.HIDE,
            AttachmentMode.HIDE,
            TwinClassFieldMode.TwinClass2TwinClassFieldMode.DETAILED,
            TwinClassMode.Twin2TwinClassMode.DETAILED,
            StatusMode.TwinClass2StatusMode.HIDE,
            DataListOptionMode.TwinClassMarker2DataListOptionMode.HIDE,
            DataListOptionMode.TwinClassTag2DataListOptionMode.HIDE,
            TwinLinkMode.Twin2TwinLinkMode.HIDE,
            LinkMode.TwinLink2LinkMode.HIDE,
            TransitionMode.HIDE),
    @FieldNameConstants.Include MAGENTA(6,
            TwinMode.DETAILED,
            DataListOptionMode.TwinMarker2DataListOptionMode.HIDE,
            DataListOptionMode.TwinTag2DataListOptionMode.HIDE,
            TwinFieldCollectionMode.NOT_EMPTY_FIELDS,
            DataListOptionMode.TwinField2DataListOptionMode.DETAILED,
            UserMode.TwinField2UserMode.DETAILED,
            TwinMode.TwinField2TwinMode.DETAILED,
            AttachmentMode.HIDE,
            TwinClassFieldMode.TwinClass2TwinClassFieldMode.HIDE,
            TwinClassMode.Twin2TwinClassMode.HIDE,
            StatusMode.TwinClass2StatusMode.HIDE,
            DataListOptionMode.TwinClassMarker2DataListOptionMode.HIDE,
            DataListOptionMode.TwinClassTag2DataListOptionMode.HIDE,
            TwinLinkMode.Twin2TwinLinkMode.DETAILED,
            LinkMode.TwinLink2LinkMode.DETAILED,
            TransitionMode.HIDE),
    @FieldNameConstants.Include PINK(6,
            TwinMode.DETAILED,
            DataListOptionMode.TwinMarker2DataListOptionMode.HIDE,
            DataListOptionMode.TwinTag2DataListOptionMode.HIDE,
            TwinFieldCollectionMode.NOT_EMPTY_FIELDS,
            DataListOptionMode.TwinField2DataListOptionMode.DETAILED,
            UserMode.TwinField2UserMode.DETAILED,
            TwinMode.TwinField2TwinMode.DETAILED,
            AttachmentMode.HIDE,
            TwinClassFieldMode.TwinClass2TwinClassFieldMode.HIDE,
            TwinClassMode.Twin2TwinClassMode.HIDE,
            StatusMode.TwinClass2StatusMode.HIDE,
            DataListOptionMode.TwinClassMarker2DataListOptionMode.HIDE,
            DataListOptionMode.TwinClassTag2DataListOptionMode.HIDE,
            TwinLinkMode.Twin2TwinLinkMode.HIDE,
            LinkMode.TwinLink2LinkMode.HIDE,
            TransitionMode.Twin2TransitionMode.HIDE,
            TransitionMode.HIDE),
    @FieldNameConstants.Include LAVENDER(6,
            TwinMode.DETAILED,
            DataListOptionMode.TwinMarker2DataListOptionMode.HIDE,
            DataListOptionMode.TwinTag2DataListOptionMode.DETAILED,
            TwinFieldCollectionMode.NOT_EMPTY_FIELDS,
            DataListOptionMode.TwinField2DataListOptionMode.DETAILED,
            UserMode.TwinField2UserMode.DETAILED,
            TwinMode.TwinField2TwinMode.DETAILED,
            AttachmentMode.HIDE,
            TwinClassFieldMode.TwinClass2TwinClassFieldMode.HIDE,
            TwinClassMode.Twin2TwinClassMode.HIDE,
            StatusMode.TwinClass2StatusMode.HIDE,
            DataListOptionMode.TwinClassMarker2DataListOptionMode.HIDE,
            DataListOptionMode.TwinClassTag2DataListOptionMode.HIDE,
            TwinLinkMode.Twin2TwinLinkMode.DETAILED,
            LinkMode.TwinLink2LinkMode.DETAILED,
            TransitionMode.HIDE);


    final int priority;
    final MapperMode[] configuredModes;

    RelationTwinMode(int priority, MapperMode... configuredModes) {
        this.priority = priority;
        this.configuredModes = configuredModes;
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public
    enum TwinByHeadMode implements MapperModePointer<RelationTwinMode>, MapperModeCollection {
        @FieldNameConstants.Include WHITE(0, new RelationTwinMode[]{RelationTwinMode.WHITE}),
        @FieldNameConstants.Include GREEN(1, new RelationTwinMode[]{RelationTwinMode.GREEN}),
        @FieldNameConstants.Include FOREST_GREEN(1, new RelationTwinMode[]{RelationTwinMode.FOREST_GREEN}),
        @FieldNameConstants.Include YELLOW(2, new RelationTwinMode[]{RelationTwinMode.YELLOW}),
        @FieldNameConstants.Include BLUE(3, new RelationTwinMode[]{RelationTwinMode.BLUE}),
        @FieldNameConstants.Include BLACK(4, new RelationTwinMode[]{RelationTwinMode.BLACK}),
        @FieldNameConstants.Include GRAY(4, new RelationTwinMode[]{RelationTwinMode.GRAY}),
        @FieldNameConstants.Include ORANGE(5, new RelationTwinMode[]{RelationTwinMode.ORANGE}),
        @FieldNameConstants.Include MAGENTA(6, new RelationTwinMode[]{RelationTwinMode.MAGENTA}),
        @FieldNameConstants.Include PINK(6, new RelationTwinMode[]{RelationTwinMode.PINK}),
        @FieldNameConstants.Include LAVENDER(6, new RelationTwinMode[]{RelationTwinMode.LAVENDER});

        final int priority;
        final MapperMode[] configuredModes;

        @Override
        public RelationTwinMode point() {
            return switch (this) {
                case WHITE -> RelationTwinMode.WHITE;
                case GREEN -> RelationTwinMode.GREEN;
                case FOREST_GREEN -> RelationTwinMode.FOREST_GREEN;
                case YELLOW -> RelationTwinMode.YELLOW;
                case BLUE -> RelationTwinMode.BLUE;
                case BLACK -> RelationTwinMode.BLACK;
                case GRAY -> RelationTwinMode.GRAY;
                case ORANGE -> RelationTwinMode.ORANGE;
                case MAGENTA -> RelationTwinMode.MAGENTA;
                case PINK -> RelationTwinMode.PINK;
                case LAVENDER -> RelationTwinMode.LAVENDER;
            };
        }
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public
    enum TwinByLinkMode implements MapperModePointer<RelationTwinMode>, MapperModeCollection {
        @FieldNameConstants.Include WHITE(0, new RelationTwinMode[]{RelationTwinMode.WHITE}),
        @FieldNameConstants.Include GREEN(1, new RelationTwinMode[]{RelationTwinMode.GREEN}),
        @FieldNameConstants.Include FOREST_GREEN(1, new RelationTwinMode[]{RelationTwinMode.FOREST_GREEN}),
        @FieldNameConstants.Include YELLOW(2, new RelationTwinMode[]{RelationTwinMode.YELLOW}),
        @FieldNameConstants.Include BLUE(3, new RelationTwinMode[]{RelationTwinMode.BLUE}),
        @FieldNameConstants.Include BLACK(4, new RelationTwinMode[]{RelationTwinMode.BLACK}),
        @FieldNameConstants.Include GRAY(4, new RelationTwinMode[]{RelationTwinMode.GRAY}),
        @FieldNameConstants.Include ORANGE(5, new RelationTwinMode[]{RelationTwinMode.ORANGE}),
        @FieldNameConstants.Include MAGENTA(6, new RelationTwinMode[]{RelationTwinMode.MAGENTA}),
        @FieldNameConstants.Include PINK(6, new RelationTwinMode[]{RelationTwinMode.PINK}),
        @FieldNameConstants.Include LAVENDER(6, new RelationTwinMode[]{RelationTwinMode.LAVENDER});

        final int priority;
        final MapperMode[] configuredModes;

        @Override
        public RelationTwinMode point() {
            return switch (this) {
                case WHITE -> RelationTwinMode.WHITE;
                case GREEN -> RelationTwinMode.GREEN;
                case FOREST_GREEN -> RelationTwinMode.FOREST_GREEN;
                case YELLOW -> RelationTwinMode.YELLOW;
                case BLUE -> RelationTwinMode.BLUE;
                case BLACK -> RelationTwinMode.BLACK;
                case GRAY -> RelationTwinMode.GRAY;
                case ORANGE -> RelationTwinMode.ORANGE;
                case MAGENTA -> RelationTwinMode.MAGENTA;
                case PINK -> RelationTwinMode.PINK;
                case LAVENDER -> RelationTwinMode.LAVENDER;
            };
        }
    }
}
