package org.twins.core.mappers.rest.twin;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldNameConstants;
import org.twins.core.mappers.rest.mappercontext.*;
import org.twins.core.mappers.rest.mappercontext.modes.*;

@Getter
@FieldNameConstants(onlyExplicitlyIncluded = true)
public enum RelationTwinMode implements MapperModeCollection {
    @FieldNameConstants.Include WHITE(0,
            TwinMode.HIDE,
            DataListOptionMode.TwinMarkerOnDataListOptionMode.HIDE,
            DataListOptionMode.TwinTagOnDataListOptionMode.HIDE,
            TwinFieldCollectionMode.NO_FIELDS,
            AttachmentMode.HIDE,
            ClassFieldMode.TwinClassOnClassFieldMode.HIDE,
            TwinClassMode.TwinOnTwinClassMode.HIDE,
            StatusMode.TwinClassOnStatusMode.HIDE,
            DataListOptionMode.TwinClassMarkerModeOnDataListOptionMode.HIDE,
            DataListOptionMode.TwinClassTagOnDataListOptionMode.HIDE,
            LinkRelationMode.TwinLinkOnLinkRelateonMode.HIDE,
            LinkMode.TwinLinkOnLinkMode.HIDE,
            TransitionMode.HIDE),
    @FieldNameConstants.Include GREEN(1,
            TwinMode.SHORT,
            DataListOptionMode.TwinMarkerOnDataListOptionMode.HIDE,
            DataListOptionMode.TwinTagOnDataListOptionMode.HIDE,
            TwinFieldCollectionMode.NO_FIELDS,
            AttachmentMode.HIDE,
            ClassFieldMode.TwinClassOnClassFieldMode.HIDE,
            TwinClassMode.TwinOnTwinClassMode.SHORT,
            StatusMode.TwinClassOnStatusMode.HIDE,
            DataListOptionMode.TwinClassMarkerModeOnDataListOptionMode.HIDE,
            DataListOptionMode.TwinClassTagOnDataListOptionMode.HIDE,
            LinkRelationMode.TwinLinkOnLinkRelateonMode.HIDE,
            LinkMode.TwinLinkOnLinkMode.HIDE,
            TransitionMode.HIDE),
    @FieldNameConstants.Include FOREST_GREEN(1,
            TwinMode.SHORT,
            DataListOptionMode.TwinMarkerOnDataListOptionMode.HIDE,
            DataListOptionMode.TwinTagOnDataListOptionMode.DETAILED,
            TwinFieldCollectionMode.NO_FIELDS,
            AttachmentMode.HIDE,
            ClassFieldMode.TwinClassOnClassFieldMode.HIDE,
            TwinClassMode.TwinOnTwinClassMode.SHORT,
            StatusMode.TwinClassOnStatusMode.HIDE,
            DataListOptionMode.TwinClassMarkerModeOnDataListOptionMode.HIDE,
            DataListOptionMode.TwinClassTagOnDataListOptionMode.HIDE,
            LinkRelationMode.TwinLinkOnLinkRelateonMode.HIDE,
            LinkMode.TwinLinkOnLinkMode.HIDE,
            TransitionMode.HIDE),
    @FieldNameConstants.Include YELLOW(2,
            TwinMode.DETAILED,
            DataListOptionMode.TwinMarkerOnDataListOptionMode.HIDE,
            DataListOptionMode.TwinTagOnDataListOptionMode.HIDE,
            TwinFieldCollectionMode.NO_FIELDS,
            AttachmentMode.HIDE,
            ClassFieldMode.TwinClassOnClassFieldMode.HIDE,
            TwinClassMode.TwinOnTwinClassMode.SHORT,
            StatusMode.TwinClassOnStatusMode.HIDE,
            DataListOptionMode.TwinClassMarkerModeOnDataListOptionMode.HIDE,
            DataListOptionMode.TwinClassTagOnDataListOptionMode.HIDE,
            LinkRelationMode.TwinLinkOnLinkRelateonMode.HIDE,
            LinkMode.TwinLinkOnLinkMode.HIDE,
            TransitionMode.HIDE),
    @FieldNameConstants.Include BLUE(3,
            TwinMode.DETAILED,
            DataListOptionMode.TwinMarkerOnDataListOptionMode.HIDE,
            DataListOptionMode.TwinTagOnDataListOptionMode.HIDE,
            TwinFieldCollectionMode.NOT_EMPTY_FIELDS,
            AttachmentMode.HIDE,
            ClassFieldMode.TwinClassOnClassFieldMode.HIDE,
            TwinClassMode.TwinOnTwinClassMode.SHORT,
            StatusMode.TwinClassOnStatusMode.HIDE,
            DataListOptionMode.TwinClassMarkerModeOnDataListOptionMode.HIDE,
            DataListOptionMode.TwinClassTagOnDataListOptionMode.HIDE,
            LinkRelationMode.TwinLinkOnLinkRelateonMode.HIDE,
            LinkMode.TwinLinkOnLinkMode.HIDE,
            TransitionMode.HIDE),
    @FieldNameConstants.Include BLACK(4,
            TwinMode.DETAILED,
            DataListOptionMode.TwinMarkerOnDataListOptionMode.HIDE,
            DataListOptionMode.TwinTagOnDataListOptionMode.HIDE,
            TwinFieldCollectionMode.NOT_EMPTY_FIELDS,
            AttachmentMode.HIDE,
            ClassFieldMode.TwinClassOnClassFieldMode.HIDE,
            TwinClassMode.TwinOnTwinClassMode.DETAILED,
            StatusMode.TwinClassOnStatusMode.HIDE,
            DataListOptionMode.TwinClassMarkerModeOnDataListOptionMode.HIDE,
            DataListOptionMode.TwinClassTagOnDataListOptionMode.HIDE,
            LinkRelationMode.TwinLinkOnLinkRelateonMode.HIDE,
            LinkMode.TwinLinkOnLinkMode.HIDE,
            TransitionMode.HIDE),
    @FieldNameConstants.Include GRAY(4,
            TwinMode.DETAILED,
            DataListOptionMode.TwinMarkerOnDataListOptionMode.HIDE,
            DataListOptionMode.TwinTagOnDataListOptionMode.DETAILED,
            TwinFieldCollectionMode.NOT_EMPTY_FIELDS,
            AttachmentMode.HIDE,
            ClassFieldMode.TwinClassOnClassFieldMode.HIDE,
            TwinClassMode.TwinOnTwinClassMode.DETAILED,
            StatusMode.TwinClassOnStatusMode.HIDE,
            DataListOptionMode.TwinClassMarkerModeOnDataListOptionMode.HIDE,
            DataListOptionMode.TwinClassTagOnDataListOptionMode.HIDE,
            LinkRelationMode.TwinLinkOnLinkRelateonMode.HIDE,
            LinkMode.TwinLinkOnLinkMode.HIDE,
            TransitionMode.HIDE),
    @FieldNameConstants.Include ORANGE(5,
            TwinMode.DETAILED,
            DataListOptionMode.TwinMarkerOnDataListOptionMode.HIDE,
            DataListOptionMode.TwinTagOnDataListOptionMode.HIDE,
            TwinFieldCollectionMode.NOT_EMPTY_FIELDS,
            AttachmentMode.HIDE,
            ClassFieldMode.TwinClassOnClassFieldMode.DETAILED,
            TwinClassMode.TwinOnTwinClassMode.DETAILED,
            StatusMode.TwinClassOnStatusMode.HIDE,
            DataListOptionMode.TwinClassMarkerModeOnDataListOptionMode.HIDE,
            DataListOptionMode.TwinClassTagOnDataListOptionMode.HIDE,
            LinkRelationMode.TwinLinkOnLinkRelateonMode.HIDE,
            LinkMode.TwinLinkOnLinkMode.HIDE,
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
        @FieldNameConstants.Include ORANGE(5, new RelationTwinMode[]{RelationTwinMode.ORANGE});

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
        @FieldNameConstants.Include ORANGE(5, new RelationTwinMode[]{RelationTwinMode.ORANGE});

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
            };
        }
    }
}
