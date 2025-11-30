package org.twins.core.mappers.rest.mappercontext.modes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldNameConstants;
import org.twins.core.mappers.rest.mappercontext.MapperMode;
import org.twins.core.mappers.rest.mappercontext.MapperModePointer;

@Getter
@AllArgsConstructor
@FieldNameConstants(onlyExplicitlyIncluded = true)
public enum UserMode implements MapperMode {
    @FieldNameConstants.Include HIDE(0),
    @FieldNameConstants.Include SHORT(1),
    @FieldNameConstants.Include DETAILED(2);

    final int priority;

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public enum Attachment2UserMode implements MapperModePointer<UserMode> {
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
    public enum Comment2UserMode implements MapperModePointer<UserMode> {
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
    public enum TwinClassFieldDescriptor2UserMode implements MapperModePointer<UserMode> {
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
    public enum History2UserMode implements MapperModePointer<UserMode> {
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
    public enum Space2UserMode implements MapperModePointer<UserMode> {
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
    public enum Transition2UserMode implements MapperModePointer<UserMode> {
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
    public enum TwinClass2UserMode implements MapperModePointer<UserMode> {
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
    public enum TwinLink2UserMode implements MapperModePointer<UserMode> {
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
    public enum Link2UserMode implements MapperModePointer<UserMode> {
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
    public enum Twinflow2UserMode implements MapperModePointer<UserMode> {
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
    public enum Twin2UserMode implements MapperModePointer<UserMode> {
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
    public enum BusinessAccountUser2UserMode implements MapperModePointer<UserMode> {
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
    public enum DomainUser2UserMode implements MapperModePointer<UserMode> {
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
    public enum PermissionGrantUserGroup2UserMode implements MapperModePointer<UserMode> {
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
    public enum PermissionSchema2UserMode implements MapperModePointer<UserMode> {
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
    public enum PermissionGrantUser2UserMode implements MapperModePointer<UserMode> {
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
    public enum Draft2UserMode implements MapperModePointer<UserMode> {
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
    public enum TwinflowSchema2UserMode implements MapperModePointer<UserMode> {
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
    public enum PermissionGrantTwinRole2UserMode implements MapperModePointer<UserMode> {
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
    public enum PermissionGrantAssigneePropagation2UserMode implements MapperModePointer<UserMode> {
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
    public enum PermissionGrantSpaceRole2UserMode implements MapperModePointer<UserMode> {
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
    public enum Factory2UserMode implements MapperModePointer<UserMode> {
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
    public enum TwinField2UserMode implements MapperModePointer<UserMode> {
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
    public enum FactoryConditionSet2UserMode implements MapperModePointer<UserMode> {
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
    public enum DataListOptionProjection2UserMode implements MapperModePointer<UserMode> {
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
}
