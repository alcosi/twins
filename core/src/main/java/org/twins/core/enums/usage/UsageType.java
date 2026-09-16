package org.twins.core.enums.usage;

/**
 * Describes where an entity is used (referenced from). Each constant names the referencing
 * entity field in dotted-path form. Shared across domains — the admin "usages" feature will
 * grow this enum with new sources (e.g. twinClassField.fieldTyperParams).
 */
public enum UsageType {
    /** factoryPipeline.nextFactory */
    FACTORY_PIPELINE_NEXT_FACTORY,
    /** factoryPipeline.afterCommitFactory */
    FACTORY_PIPELINE_AFTER_COMMIT_FACTORY,
    /** factoryBranch.nextFactory */
    FACTORY_BRANCH_NEXT_FACTORY,
    /** twinflowTransition.inbuiltTwinFactory */
    TWINFLOW_TRANSITION_INBUILT_FACTORY,
    /** twinflowFactory.twinFactory (any launcher) */
    TWINFLOW_FACTORY_LAUNCHER
}
