package org.twins.core.domain.usage;

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
 * Server-side twin of the client-facing {@code org.twins.core.enums.usage.UsageType}: same
 * constant names, plus the metadata needed to search and render usages:
 * <ul>
 *     <li>{@code entityClass} — class of the referencing entity, used by Usage (an EntityRef)
 *     to resolve the referencing instance lazily/bulk via EntityServiceRegistry;</li>
 *     <li>{@code entityFieldName} — lombok {@code Fields} constant of the referencing entity,
 *     used to build the search specification;</li>
 *     <li>{@code entityIdFunction} — getter reference for reading the referenced id from a
 *     found entity, compile-time checked at the declaration site;</li>
 *     <li>{@code defaultShowMode} — mode used when postponing the referencing entity into
 *     relatedObjects (applied only if the caller did not configure that mode explicitly,
 *     so global request modes keep priority).</li>
 * </ul>
 * This enum will grow with new usage sources (e.g. twinClassField.fieldTyperParams) — add the
 * constant here AND to the client enum, plus a case in the UsageRestDTOMapper switch (the
 * exhaustive switch makes a missed client twin a compile error, not a runtime one).
 */
@Getter
@AllArgsConstructor
public enum UsageType {
    /** factoryPipeline.nextFactory */
    FACTORY_PIPELINE_NEXT_FACTORY(
            TwinFactoryPipelineEntity.class,
            TwinFactoryPipelineEntity.Fields.nextTwinFactoryId,
            entityIdGetter(TwinFactoryPipelineEntity::getNextTwinFactoryId),
            FactoryPipelineMode.DETAILED),
    /** factoryPipeline.afterCommitFactory */
    FACTORY_PIPELINE_AFTER_COMMIT_FACTORY(
            TwinFactoryPipelineEntity.class,
            TwinFactoryPipelineEntity.Fields.afterCommitTwinFactoryId,
            entityIdGetter(TwinFactoryPipelineEntity::getAfterCommitTwinFactoryId),
            FactoryPipelineMode.DETAILED),
    /** factoryBranch.nextFactory */
    FACTORY_BRANCH_NEXT_FACTORY(
            TwinFactoryBranchEntity.class,
            TwinFactoryBranchEntity.Fields.nextTwinFactoryId,
            entityIdGetter(TwinFactoryBranchEntity::getNextTwinFactoryId),
            FactoryBranchMode.DETAILED),
    /** twinflowTransition.inbuiltTwinFactory */
    TWINFLOW_TRANSITION_INBUILT_FACTORY(
            TwinflowTransitionEntity.class,
            TwinflowTransitionEntity.Fields.inbuiltTwinFactoryId,
            entityIdGetter(TwinflowTransitionEntity::getInbuiltTwinFactoryId),
            TransitionMode.DETAILED),
    /** twinflowFactory.twinFactory (any launcher) */
    TWINFLOW_FACTORY_LAUNCHER(
            TwinflowFactoryEntity.class,
            TwinflowFactoryEntity.Fields.twinFactoryId,
            entityIdGetter(TwinflowFactoryEntity::getTwinFactoryId),
            TwinflowFactoryMode.DETAILED);

    final Class<?> entityClass;
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
