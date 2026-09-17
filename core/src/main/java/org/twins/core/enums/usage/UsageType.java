package org.twins.core.enums.usage;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.twins.core.dao.factory.TwinFactoryBranchEntity;
import org.twins.core.dao.factory.TwinFactoryPipelineEntity;
import org.twins.core.dao.twinflow.TwinflowFactoryEntity;
import org.twins.core.dao.twinflow.TwinflowTransitionEntity;
import org.twins.core.mappers.rest.mappercontext.MapperMode;
import org.twins.core.mappers.rest.mappercontext.modes.FactoryBranchMode;
import org.twins.core.mappers.rest.mappercontext.modes.FactoryPipelineMode;
import org.twins.core.mappers.rest.mappercontext.modes.TransitionMode;
import org.twins.core.mappers.rest.mappercontext.modes.TwinflowFactoryMode;

import java.util.UUID;
import java.util.function.Function;

/**
 * Describes where an entity is used (referenced from). Each constant names the referencing
 * entity field in dotted-path form and carries:
 * <ul>
 *     <li>{@code entityFieldName} — lombok {@code Fields} constant of the referencing entity,
 *     used to build the search specification;</li>
 *     <li>{@code entityIdFunction} — getter reference for reading the referenced id from a
 *     found entity, compile-time checked at the declaration site;</li>
 *     <li>{@code defaultShowMode} — mode used when postponing the referencing entity into
 *     relatedObjects (applied only if the caller did not configure that mode explicitly,
 *     so global request modes keep priority).</li>
 * </ul>
 * Shared across domains — the admin "usages" feature will grow this enum with new sources
 * (e.g. twinClassField.fieldTyperParams).
 */
@Getter
@AllArgsConstructor
public enum UsageType {
    /** factoryPipeline.nextFactory */
    FACTORY_PIPELINE_NEXT_FACTORY(
            TwinFactoryPipelineEntity.Fields.nextTwinFactoryId,
            entityIdGetter(TwinFactoryPipelineEntity::getNextTwinFactoryId),
            FactoryPipelineMode.DETAILED),
    /** factoryPipeline.afterCommitFactory */
    FACTORY_PIPELINE_AFTER_COMMIT_FACTORY(
            TwinFactoryPipelineEntity.Fields.afterCommitTwinFactoryId,
            entityIdGetter(TwinFactoryPipelineEntity::getAfterCommitTwinFactoryId),
            FactoryPipelineMode.DETAILED),
    /** factoryBranch.nextFactory */
    FACTORY_BRANCH_NEXT_FACTORY(
            TwinFactoryBranchEntity.Fields.nextTwinFactoryId,
            entityIdGetter(TwinFactoryBranchEntity::getNextTwinFactoryId),
            FactoryBranchMode.DETAILED),
    /** twinflowTransition.inbuiltTwinFactory */
    TWINFLOW_TRANSITION_INBUILT_FACTORY(
            TwinflowTransitionEntity.Fields.inbuiltTwinFactoryId,
            entityIdGetter(TwinflowTransitionEntity::getInbuiltTwinFactoryId),
            TransitionMode.DETAILED),
    /** twinflowFactory.twinFactory (any launcher) */
    TWINFLOW_FACTORY_LAUNCHER(
            TwinflowFactoryEntity.Fields.twinFactoryId,
            entityIdGetter(TwinflowFactoryEntity::getTwinFactoryId),
            TwinflowFactoryMode.DETAILED);

    final String entityFieldName;
    final Function<Object, UUID> entityIdFunction;
    final MapperMode defaultShowMode;

    /**
     * Adapts a typed getter reference to the looser {@code Function<Object, UUID>} stored in the
     * enum constant. The cast inside is safe by construction: the function is only applied to
     * entities of the class it was declared for (registerUsages on the matching service).
     */
    @SuppressWarnings("unchecked")
    private static <E> Function<Object, UUID> entityIdGetter(Function<E, UUID> getter) {
        return entity -> getter.apply((E) entity);
    }
}
