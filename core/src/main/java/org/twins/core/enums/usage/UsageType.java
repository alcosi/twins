package org.twins.core.enums.usage;

/**
 * Describes where an entity is used (referenced from) — client-facing enum shipped in the DTO
 * library, so it must stay free of server-side imports. The server-side metadata (search field,
 * id getter, default show mode) lives in the twin enum {@code org.twins.core.domain.usage.UsageType};
 * both enums share constant names and are converted by an explicit switch in UsageRestDTOMapper.
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
