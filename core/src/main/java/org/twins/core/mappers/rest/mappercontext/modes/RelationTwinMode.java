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
            TwinFieldCollectionMode.HIDE,
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
            TwinFieldCollectionMode.HIDE,
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
            TwinFieldCollectionMode.HIDE,
            AttachmentMode.HIDE,
            TwinClassFieldMode.TwinClass2TwinClassFieldMode.HIDE,
            TwinClassMode.Twin2TwinClassMode.SHORT,
            StatusMode.TwinClass2StatusMode.HIDE,
            DataListOptionMode.TwinClassMarker2DataListOptionMode.HIDE,
            DataListOptionMode.TwinClassTag2DataListOptionMode.HIDE,
            TwinLinkMode.Twin2TwinLinkMode.HIDE,
            LinkMode.TwinLink2LinkMode.HIDE,
            TransitionMode.HIDE),
    @FieldNameConstants.Include LIGHT_GREEN(1,
            TwinMode.SHORT,
            TwinFieldCollectionMode.SHOW,
            TwinFieldCollectionFilterEmptyMode.ONLY_NOT,
            TwinClassFieldMode.TwinClass2TwinClassFieldMode.HIDE),
    @FieldNameConstants.Include YELLOW_LIGHT(2,
            TwinMode.DETAILED,
            DataListOptionMode.TwinMarker2DataListOptionMode.HIDE,
            DataListOptionMode.TwinTag2DataListOptionMode.HIDE,
            TwinFieldCollectionMode.HIDE,
            AttachmentMode.HIDE,
            TwinClassFieldMode.TwinClass2TwinClassFieldMode.HIDE,
            TwinClassMode.Twin2TwinClassMode.HIDE,
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
            TwinFieldCollectionMode.HIDE,
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
            TwinFieldCollectionMode.SHOW,
            TwinFieldCollectionFilterEmptyMode.ONLY_NOT,
            DataListOptionMode.TwinField2DataListOptionMode.HIDE,
            UserMode.TwinField2UserMode.HIDE,
            TwinByFieldMode.WHITE,
            TwinClassFieldMode.TwinClass2TwinClassFieldMode.HIDE,
            TwinClassMode.Twin2TwinClassMode.SHORT,
            StatusMode.TwinClass2StatusMode.HIDE,
            StatusMode.Twin2StatusMode.DETAILED,
            AttachmentMode.Twin2AttachmentMode.DETAILED,
            AttachmentCollectionMode.Twin2AttachmentCollectionMode.ALL,
            DataListOptionMode.TwinClassMarker2DataListOptionMode.HIDE,
            DataListOptionMode.TwinClassTag2DataListOptionMode.HIDE,
            TwinLinkMode.Twin2TwinLinkMode.HIDE,
            LinkMode.TwinLink2LinkMode.HIDE,
            TransitionMode.HIDE),
    @FieldNameConstants.Include BLACK(4,
            TwinMode.DETAILED,
            DataListOptionMode.TwinMarker2DataListOptionMode.HIDE,
            DataListOptionMode.TwinTag2DataListOptionMode.HIDE,
            TwinFieldCollectionMode.SHOW,
            TwinFieldCollectionFilterEmptyMode.ONLY_NOT,
            DataListOptionMode.TwinField2DataListOptionMode.HIDE,
            UserMode.TwinField2UserMode.HIDE,
            TwinByFieldMode.WHITE,
            TwinClassFieldMode.TwinClass2TwinClassFieldMode.HIDE,
            TwinClassMode.Twin2TwinClassMode.DETAILED,
            StatusMode.TwinClass2StatusMode.HIDE,
            StatusMode.Twin2StatusMode.DETAILED,
            DataListOptionMode.TwinClassMarker2DataListOptionMode.HIDE,
            DataListOptionMode.TwinClassTag2DataListOptionMode.HIDE,
            TwinLinkMode.Twin2TwinLinkMode.HIDE,
            AttachmentMode.Twin2AttachmentMode.DETAILED,
            AttachmentCollectionMode.Twin2AttachmentCollectionMode.ALL,
            LinkMode.TwinLink2LinkMode.HIDE,
            TransitionMode.HIDE),
    @FieldNameConstants.Include RED (4,
            TwinMode.DETAILED,
            DataListOptionMode.TwinMarker2DataListOptionMode.HIDE,
            DataListOptionMode.TwinTag2DataListOptionMode.HIDE,
            TwinFieldCollectionMode.HIDE,
            DataListOptionMode.TwinField2DataListOptionMode.HIDE,
            UserMode.TwinField2UserMode.HIDE,
            TwinByFieldMode.WHITE,
            AttachmentMode.HIDE,
            TwinClassFieldMode.TwinClass2TwinClassFieldMode.HIDE,
            TwinClassMode.Twin2TwinClassMode.DETAILED,
            StatusMode.TwinClass2StatusMode.HIDE,
            StatusMode.Twin2StatusMode.DETAILED,
            DataListOptionMode.TwinClassMarker2DataListOptionMode.HIDE,
            DataListOptionMode.TwinClassTag2DataListOptionMode.HIDE,
            TwinLinkMode.Twin2TwinLinkMode.HIDE,
            LinkMode.TwinLink2LinkMode.HIDE,
            TransitionMode.HIDE),
    @FieldNameConstants.Include GRAY(4,
            TwinMode.DETAILED,
            DataListOptionMode.TwinMarker2DataListOptionMode.HIDE,
            DataListOptionMode.TwinTag2DataListOptionMode.DETAILED,
            TwinFieldCollectionMode.SHOW,
            TwinFieldCollectionFilterEmptyMode.ONLY_NOT,
            DataListOptionMode.TwinField2DataListOptionMode.HIDE,
            UserMode.TwinField2UserMode.HIDE,
            TwinByLinkMode.WHITE,
            AttachmentMode.HIDE,
            TwinClassFieldMode.TwinClass2TwinClassFieldMode.HIDE,
            TwinClassMode.Twin2TwinClassMode.DETAILED,
            StatusMode.TwinClass2StatusMode.HIDE,
            StatusMode.Twin2StatusMode.DETAILED,
            DataListOptionMode.TwinClassMarker2DataListOptionMode.HIDE,
            DataListOptionMode.TwinClassTag2DataListOptionMode.HIDE,
            TwinLinkMode.Twin2TwinLinkMode.HIDE,
            LinkMode.TwinLink2LinkMode.HIDE,
            TransitionMode.HIDE),
    @FieldNameConstants.Include ORANGE(5,
            TwinMode.DETAILED,
            DataListOptionMode.TwinMarker2DataListOptionMode.HIDE,
            DataListOptionMode.TwinTag2DataListOptionMode.HIDE,
            TwinFieldCollectionMode.SHOW,
            TwinFieldCollectionFilterEmptyMode.ONLY_NOT,
            DataListOptionMode.TwinField2DataListOptionMode.HIDE,
            UserMode.TwinField2UserMode.HIDE,
            TwinByFieldMode.WHITE,
            AttachmentMode.HIDE,
            TwinClassFieldMode.TwinClass2TwinClassFieldMode.DETAILED,
            TwinClassFieldCollectionMode.SHOW,
            TwinClassMode.Twin2TwinClassMode.DETAILED,
            StatusMode.TwinClass2StatusMode.HIDE,
            StatusMode.Twin2StatusMode.DETAILED,
            DataListOptionMode.TwinClassMarker2DataListOptionMode.HIDE,
            DataListOptionMode.TwinClassTag2DataListOptionMode.HIDE,
            TwinLinkMode.Twin2TwinLinkMode.HIDE,
            LinkMode.TwinLink2LinkMode.HIDE,
            TransitionMode.HIDE),
    @FieldNameConstants.Include MAGENTA(6,
            TwinMode.DETAILED,
            DataListOptionMode.TwinMarker2DataListOptionMode.HIDE,
            DataListOptionMode.TwinTag2DataListOptionMode.HIDE,
            TwinFieldCollectionMode.SHOW,
            TwinFieldCollectionFilterEmptyMode.ONLY_NOT,
            DataListOptionMode.TwinField2DataListOptionMode.DETAILED,
            UserMode.TwinField2UserMode.DETAILED,
            TwinByFieldMode.YELLOW_LIGHT,
            AttachmentMode.HIDE,
            TwinClassFieldMode.TwinClass2TwinClassFieldMode.HIDE,
            TwinClassMode.Twin2TwinClassMode.HIDE,
            StatusMode.TwinClass2StatusMode.HIDE,
            StatusMode.Twin2StatusMode.DETAILED,
            DataListOptionMode.TwinClassMarker2DataListOptionMode.HIDE,
            DataListOptionMode.TwinClassTag2DataListOptionMode.HIDE,
            TwinLinkMode.Twin2TwinLinkMode.DETAILED,
            LinkMode.TwinLink2LinkMode.DETAILED,
            TransitionMode.HIDE),
    @FieldNameConstants.Include PINK(6,
            TwinMode.DETAILED,
            DataListOptionMode.TwinMarker2DataListOptionMode.HIDE,
            DataListOptionMode.TwinTag2DataListOptionMode.HIDE,
            TwinFieldCollectionMode.SHOW,
            TwinFieldCollectionFilterEmptyMode.ONLY_NOT,
            DataListOptionMode.TwinField2DataListOptionMode.DETAILED,
            UserMode.TwinField2UserMode.DETAILED,
            TwinByFieldMode.YELLOW_LIGHT,
            AttachmentMode.HIDE,
            TwinClassFieldMode.TwinClass2TwinClassFieldMode.HIDE,
            TwinClassMode.Twin2TwinClassMode.HIDE,
            StatusMode.TwinClass2StatusMode.HIDE,
            StatusMode.Twin2StatusMode.DETAILED,
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
            TwinFieldCollectionMode.SHOW,
            TwinFieldCollectionFilterEmptyMode.ONLY_NOT,
            DataListOptionMode.TwinField2DataListOptionMode.DETAILED,
            UserMode.TwinField2UserMode.DETAILED,
            TwinByFieldMode.YELLOW_LIGHT,
            AttachmentMode.HIDE,
            TwinClassFieldMode.TwinClass2TwinClassFieldMode.HIDE,
            TwinClassMode.Twin2TwinClassMode.HIDE,
            StatusMode.TwinClass2StatusMode.HIDE,
            StatusMode.Twin2StatusMode.DETAILED,
            DataListOptionMode.TwinClassMarker2DataListOptionMode.HIDE,
            DataListOptionMode.TwinClassTag2DataListOptionMode.HIDE,
            TwinLinkMode.Twin2TwinLinkMode.DETAILED,
            TwinByHeadMode.LAVENDER,
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
    enum TwinByHeadMode implements MapperModePointer<RelationTwinMode> {
        @FieldNameConstants.Include WHITE(0),
        @FieldNameConstants.Include GREEN(1),
        @FieldNameConstants.Include FOREST_GREEN(1),
        @FieldNameConstants.Include LIGHT_GREEN(1),
        @FieldNameConstants.Include YELLOW(2),
        @FieldNameConstants.Include YELLOW_LIGHT(2),
        @FieldNameConstants.Include BLUE(3),
        @FieldNameConstants.Include BLACK(4),
        @FieldNameConstants.Include RED(4),
        @FieldNameConstants.Include GRAY(4),
        @FieldNameConstants.Include ORANGE(5),
        @FieldNameConstants.Include MAGENTA(6),
        @FieldNameConstants.Include PINK(6),
        @FieldNameConstants.Include LAVENDER(6);

        final int priority;

        @Override
        public RelationTwinMode point() {
            return switch (this) {
                case WHITE -> RelationTwinMode.WHITE;
                case GREEN -> RelationTwinMode.GREEN;
                case FOREST_GREEN -> RelationTwinMode.FOREST_GREEN;
                case LIGHT_GREEN -> RelationTwinMode.LIGHT_GREEN;
                case YELLOW -> RelationTwinMode.YELLOW;
                case YELLOW_LIGHT -> RelationTwinMode.YELLOW_LIGHT;
                case BLUE -> RelationTwinMode.BLUE;
                case BLACK -> RelationTwinMode.BLACK;
                case RED -> RelationTwinMode.RED;
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
    enum TwinByLinkMode implements MapperModePointer<RelationTwinMode> {
        @FieldNameConstants.Include WHITE(0),
        @FieldNameConstants.Include GREEN(1),
        @FieldNameConstants.Include FOREST_GREEN(1),
        @FieldNameConstants.Include LIGHT_GREEN(1),
        @FieldNameConstants.Include YELLOW(2),
        @FieldNameConstants.Include YELLOW_LIGHT(2),
        @FieldNameConstants.Include BLUE(3),
        @FieldNameConstants.Include BLACK(4),
        @FieldNameConstants.Include RED(4),
        @FieldNameConstants.Include GRAY(4),
        @FieldNameConstants.Include ORANGE(5),
        @FieldNameConstants.Include MAGENTA(6),
        @FieldNameConstants.Include PINK(6),
        @FieldNameConstants.Include LAVENDER(6);

        final int priority;

        @Override
        public RelationTwinMode point() {
            return switch (this) {
                case WHITE -> RelationTwinMode.WHITE;
                case GREEN -> RelationTwinMode.GREEN;
                case FOREST_GREEN -> RelationTwinMode.FOREST_GREEN;
                case LIGHT_GREEN -> RelationTwinMode.LIGHT_GREEN;
                case YELLOW -> RelationTwinMode.YELLOW;
                case YELLOW_LIGHT -> RelationTwinMode.YELLOW_LIGHT;
                case BLUE -> RelationTwinMode.BLUE;
                case BLACK -> RelationTwinMode.BLACK;
                case RED -> RelationTwinMode.RED;
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
    enum TwinByFieldMode implements MapperModePointer<RelationTwinMode> {
        @FieldNameConstants.Include WHITE(0),
        @FieldNameConstants.Include GREEN(1),
        @FieldNameConstants.Include FOREST_GREEN(1),
        @FieldNameConstants.Include LIGHT_GREEN(1),
        @FieldNameConstants.Include YELLOW(2),
        @FieldNameConstants.Include YELLOW_LIGHT(2),
        @FieldNameConstants.Include BLUE(3),
        @FieldNameConstants.Include BLACK(4),
        @FieldNameConstants.Include RED(4),
        @FieldNameConstants.Include GRAY(4),
        @FieldNameConstants.Include ORANGE(5),
        @FieldNameConstants.Include MAGENTA(6),
        @FieldNameConstants.Include PINK(6),
        @FieldNameConstants.Include LAVENDER(6);

        final int priority;

        @Override
        public RelationTwinMode point() {
            return switch (this) {
                case WHITE -> RelationTwinMode.WHITE;
                case GREEN -> RelationTwinMode.GREEN;
                case FOREST_GREEN -> RelationTwinMode.FOREST_GREEN;
                case LIGHT_GREEN -> RelationTwinMode.LIGHT_GREEN;
                case YELLOW -> RelationTwinMode.YELLOW;
                case YELLOW_LIGHT -> RelationTwinMode.YELLOW_LIGHT;
                case BLUE -> RelationTwinMode.BLUE;
                case BLACK -> RelationTwinMode.BLACK;
                case RED -> RelationTwinMode.RED;
                case GRAY -> RelationTwinMode.GRAY;
                case ORANGE -> RelationTwinMode.ORANGE;
                case MAGENTA -> RelationTwinMode.MAGENTA;
                case PINK -> RelationTwinMode.PINK;
                case LAVENDER -> RelationTwinMode.LAVENDER;
            };
        }
    }
}
