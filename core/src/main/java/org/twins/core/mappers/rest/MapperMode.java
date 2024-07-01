/*
 * Copyright (c)
 * created:2021 - 5 - 13
 * by Yan Tayanouski
 * ESAS Ltd. La propriété, c'est le vol!
 */

package org.twins.core.mappers.rest;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldNameConstants;
import org.twins.core.mappers.rest.permission.PermissionRestDTOMapper;
import org.twins.core.mappers.rest.twin.RelationTwinMode;

public interface MapperMode {
    int getPriority();

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    enum ActionMode implements MapperMode {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHOW(1);

        final int priority;
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    enum AliasMode implements MapperMode {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    enum AttachmentMode implements MapperMode {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    enum AttachmentCollectionMode implements MapperMode {
        @FieldNameConstants.Include DIRECT(0),
        @FieldNameConstants.Include FROM_TRANSITIONS(1),
        @FieldNameConstants.Include FROM_COMMENTS(1),
        @FieldNameConstants.Include FROM_FIELDS(1),
        @FieldNameConstants.Include ALL(2);

        final int priority;
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    enum DataListOptionMode implements MapperMode {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    enum LinkMode implements MapperMode {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    enum LinkRelationMode implements MapperMode {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    enum TransitionMode implements MapperMode {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    enum StatusMode implements MapperMode {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    enum StarredMode implements MapperMode {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    enum StarredTwinMode implements MapperModePointer<TwinMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;

        @Override
        public TwinMode point() {
            return switch (this) {
                case HIDE -> TwinMode.HIDE;
                case SHORT -> TwinMode.SHORT;
                case DETAILED -> TwinMode.DETAILED;
            };
        }
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    enum TransitionResultMode implements MapperMode {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    enum TwinMode implements MapperMode {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    enum ClassMode implements MapperMode {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    enum TwinflowMode implements MapperMode {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        public static final String _HIDE = "HIDE";
        public static final String _SHORT = "SHORT";
        public static final String _DETAILED = "DETAILED";

        final int priority;
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    enum UserMode implements MapperMode {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;
    }


    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    enum ClassFieldMode implements MapperMode {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;
    }

    /* *
     *
     * TODO POINTS ****************************************************************************************************
     *
     * */

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    enum AttachmentTransitionMode implements MapperModePointer<TransitionMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;

        @Override
        public TransitionMode point() {
            return switch (this) {
                case HIDE -> TransitionMode.HIDE;
                case SHORT -> TransitionMode.SHORT;
                case DETAILED -> TransitionMode.DETAILED;
            };
        }
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    enum TransitionStatusMode implements MapperModePointer<StatusMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;

        @Override
        public StatusMode point() {
            return switch (this) {
                case HIDE -> StatusMode.HIDE;
                case SHORT -> StatusMode.SHORT;
                case DETAILED -> StatusMode.DETAILED;
            };
        }
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    enum TransitionPermissionMode implements MapperModePointer<PermissionRestDTOMapper.Mode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;

        @Override
        public PermissionRestDTOMapper.Mode point() {
            return switch (this) {
                case HIDE -> PermissionRestDTOMapper.Mode.HIDE;
                case SHORT -> PermissionRestDTOMapper.Mode.SHORT;
                case DETAILED -> PermissionRestDTOMapper.Mode.DETAILED;
            };
        }
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    enum TwinActionMode implements MapperModePointer<ActionMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHOW(2);

        final int priority;

        @Override
        public ActionMode point() {
            return switch (this) {
                case HIDE -> ActionMode.HIDE;
                case SHOW -> ActionMode.SHOW;
            };
        }
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    enum TwinAliasMode implements MapperModePointer<AliasMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;

        @Override
        public AliasMode point() {
            return switch (this) {
                case HIDE -> AliasMode.HIDE;
                case SHORT -> AliasMode.SHORT;
                case DETAILED -> AliasMode.DETAILED;
            };
        }
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    enum TwinAttachmentMode implements MapperModePointer<AttachmentMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;

        @Override
        public AttachmentMode point() {
            return switch (this) {
                case HIDE -> AttachmentMode.HIDE;
                case SHORT -> AttachmentMode.SHORT;
                case DETAILED -> AttachmentMode.DETAILED;
            };
        }
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    enum TwinAttachmentCollectionMode implements MapperModePointer<AttachmentCollectionMode> {
        @FieldNameConstants.Include DIRECT(0),
        @FieldNameConstants.Include FROM_TRANSITIONS(1),
        @FieldNameConstants.Include FROM_COMMENTS(1),
        @FieldNameConstants.Include FROM_FIELDS(1),
        @FieldNameConstants.Include ALL(2);

        final int priority;

        @Override
        public AttachmentCollectionMode point() {
            return switch (this) {
                case DIRECT -> AttachmentCollectionMode.DIRECT;
                case FROM_TRANSITIONS -> AttachmentCollectionMode.FROM_TRANSITIONS;
                case FROM_COMMENTS -> AttachmentCollectionMode.FROM_COMMENTS;
                case FROM_FIELDS -> AttachmentCollectionMode.FROM_FIELDS;
                case ALL -> AttachmentCollectionMode.ALL;
            };
        }
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
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


    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    enum TwinClassMode implements MapperModePointer<ClassMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;

        @Override
        public ClassMode point() {
            return switch (this) {
                case HIDE -> ClassMode.HIDE;
                case SHORT -> ClassMode.SHORT;
                case DETAILED -> ClassMode.DETAILED;
            };
        }
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    enum TwinClassLinkMode implements MapperModePointer<LinkMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;

        @Override
        public LinkMode point() {
            return switch (this) {
                case HIDE -> LinkMode.HIDE;
                case SHORT -> LinkMode.SHORT;
                case DETAILED -> LinkMode.DETAILED;
            };
        }
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    enum TwinClassStatusMode implements MapperModePointer<StatusMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;

        @Override
        public StatusMode point() {
            return switch (this) {
                case HIDE -> StatusMode.HIDE;
                case SHORT -> StatusMode.SHORT;
                case DETAILED -> StatusMode.DETAILED;
            };
        }
    }


    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    enum TwinLinkMode implements MapperModePointer<LinkRelationMode>{
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;

        @Override
        public LinkRelationMode point() {
            return switch (this) {
                case HIDE -> LinkRelationMode.HIDE;
                case SHORT -> LinkRelationMode.SHORT;
                case DETAILED -> LinkRelationMode.DETAILED;
            };
        }
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    enum TwinLinkOnLinkMode implements MapperModePointer<LinkMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;

        @Override
        public LinkMode point() {
            return switch (this) {
                case HIDE -> LinkMode.HIDE;
                case SHORT -> LinkMode.SHORT;
                case DETAILED -> LinkMode.DETAILED;
            };
        }
    }


    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    enum TwinMarkerMode implements MapperModePointer<DataListOptionMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;

        @Override
        public DataListOptionMode point() {
            return switch (this) {
                case HIDE -> DataListOptionMode.HIDE;
                case SHORT -> DataListOptionMode.SHORT;
                case DETAILED -> DataListOptionMode.DETAILED;
            };
        }
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    enum TwinStatusMode implements MapperModePointer<StatusMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;

        @Override
        public StatusMode point() {
            return switch (this) {
                case HIDE -> StatusMode.HIDE;
                case SHORT -> StatusMode.SHORT;
                case DETAILED -> StatusMode.DETAILED;
            };
        }
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    enum TwinTagMode implements MapperModePointer<DataListOptionMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;

        @Override
        public DataListOptionMode point() {
            return switch (this) {
                case HIDE -> DataListOptionMode.HIDE;
                case SHORT -> DataListOptionMode.SHORT;
                case DETAILED -> DataListOptionMode.DETAILED;
            };
        }
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    enum TwinTransitionMode implements MapperModePointer<TransitionMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;

        @Override
        public TransitionMode point() {
            return switch (this) {
                case HIDE -> TransitionMode.HIDE;
                case SHORT -> TransitionMode.SHORT;
                case DETAILED -> TransitionMode.DETAILED;
            };
        }
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    enum TwinflowInitStatusMode implements MapperModePointer<StatusMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;

        @Override
        public StatusMode point() {
            return switch (this) {
                case HIDE -> StatusMode.HIDE;
                case SHORT -> StatusMode.SHORT;
                case DETAILED -> StatusMode.DETAILED;
            };
        }
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    enum TwinflowTransitionMode implements MapperModePointer<TransitionMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;

        @Override
        public TransitionMode point() {
            return switch (this) {
                case HIDE -> TransitionMode.HIDE;
                case SHORT -> TransitionMode.SHORT;
                case DETAILED -> TransitionMode.DETAILED;
            };
        }
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    enum AttachmentUserMode implements MapperModePointer<UserMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;

        @Override
        public UserMode point() {
            return switch (this) {
                case HIDE -> UserMode.HIDE;
                case SHORT -> UserMode.SHORT;
                case DETAILED -> UserMode.DETAILED;
            };
        }
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    enum CommentUserMode implements MapperModePointer<UserMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;

        @Override
        public UserMode point() {
            return switch (this) {
                case HIDE -> UserMode.HIDE;
                case SHORT -> UserMode.SHORT;
                case DETAILED -> UserMode.DETAILED;
            };
        }
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    enum CommentAttachmentMode implements MapperModePointer<AttachmentMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;

        @Override
        public AttachmentMode point() {
            return switch (this) {
                case HIDE -> AttachmentMode.HIDE;
                case SHORT -> AttachmentMode.SHORT;
                case DETAILED -> AttachmentMode.DETAILED;
            };
        }
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    enum HistoryUserMode implements MapperModePointer<UserMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;

        @Override
        public UserMode point() {
            return switch (this) {
                case HIDE -> UserMode.HIDE;
                case SHORT -> UserMode.SHORT;
                case DETAILED -> UserMode.DETAILED;
            };
        }
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    enum TwinUserMode implements MapperModePointer<UserMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;

        @Override
        public UserMode point() {
            return switch (this) {
                case HIDE -> UserMode.HIDE;
                case SHORT -> UserMode.SHORT;
                case DETAILED -> UserMode.DETAILED;
            };
        }
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    enum TwinClassUserMode implements MapperModePointer<UserMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;

        @Override
        public UserMode point() {
            return switch (this) {
                case HIDE -> UserMode.HIDE;
                case SHORT -> UserMode.SHORT;
                case DETAILED -> UserMode.DETAILED;
            };
        }
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    enum TransitionUserMode implements MapperModePointer<UserMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;

        @Override
        public UserMode point() {
            return switch (this) {
                case HIDE -> UserMode.HIDE;
                case SHORT -> UserMode.SHORT;
                case DETAILED -> UserMode.DETAILED;
            };
        }
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    enum FieldUserMode implements MapperModePointer<UserMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;

        @Override
        public UserMode point() {
            return switch (this) {
                case HIDE -> UserMode.HIDE;
                case SHORT -> UserMode.SHORT;
                case DETAILED -> UserMode.DETAILED;
            };
        }
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    enum SpaceUserMode implements MapperModePointer<UserMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;

        @Override
        public UserMode point() {
            return switch (this) {
                case HIDE -> UserMode.HIDE;
                case SHORT -> UserMode.SHORT;
                case DETAILED -> UserMode.DETAILED;
            };
        }
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    enum TwinLinkUserMode implements MapperModePointer<UserMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;

        @Override
        public UserMode point() {
            return switch (this) {
                case HIDE -> UserMode.HIDE;
                case SHORT -> UserMode.SHORT;
                case DETAILED -> UserMode.DETAILED;
            };
        }
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    enum TwinflowUserMode implements MapperModePointer<UserMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;

        @Override
        public UserMode point() {
            return switch (this) {
                case HIDE -> UserMode.HIDE;
                case SHORT -> UserMode.SHORT;
                case DETAILED -> UserMode.DETAILED;
            };
        }
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    enum TwinClassTagMode implements MapperModePointer<DataListOptionMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;

        @Override
        public DataListOptionMode point() {
            return switch (this) {
                case HIDE -> DataListOptionMode.HIDE;
                case SHORT -> DataListOptionMode.SHORT;
                case DETAILED -> DataListOptionMode.DETAILED;
            };
        }
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    enum TwinClassMarkerMode implements MapperModePointer<DataListOptionMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;

        @Override
        public DataListOptionMode point() {
            return switch (this) {
                case HIDE -> DataListOptionMode.HIDE;
                case SHORT -> DataListOptionMode.SHORT;
                case DETAILED -> DataListOptionMode.DETAILED;
            };
        }
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    enum TwinClassFieldMode implements MapperModePointer<ClassFieldMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;

        @Override
        public ClassFieldMode point() {
            return switch (this) {
                case HIDE -> ClassFieldMode.HIDE;
                case SHORT -> ClassFieldMode.SHORT;
                case DETAILED -> ClassFieldMode.DETAILED;
            };
        }
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    enum TwinFieldCollectionMode implements MapperMode {
        @FieldNameConstants.Include NO_FIELDS(0),
        @FieldNameConstants.Include NOT_EMPTY_FIELDS(1),
        @FieldNameConstants.Include ALL_FIELDS(2),
        @FieldNameConstants.Include NOT_EMPTY_FIELDS_WITH_ATTACHMENTS(3),
        @FieldNameConstants.Include ALL_FIELDS_WITH_ATTACHMENTS(4);

        final int priority;
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    enum CardMode implements MapperMode {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    enum CardWidgetMode implements MapperModePointer<WidgetMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;

        @Override
        public WidgetMode point() {
            return switch (this) {
                case HIDE -> WidgetMode.HIDE;
                case SHORT -> WidgetMode.SHORT;
                case DETAILED -> WidgetMode.DETAILED;
            };
        }
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    enum WidgetMode implements MapperMode {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    enum CommentMode implements MapperMode {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    enum DataListMode implements MapperMode {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    enum DomainMode implements MapperMode {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;
    }
}
