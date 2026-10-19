package org.twins.core.featurer.factory.factoryprocessor;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.FeaturerType;
import org.twins.core.dao.factory.TwinFactoryEntity;
import org.twins.core.domain.factory.FactoryBranchId;
import org.twins.core.domain.factory.FactoryContext;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;

import java.util.Properties;

/**
 * Featurer that drives the execution of a single {@link TwinFactoryEntity}.
 * <p>
 * The default implementation {@link FactoryProcessorImpl} runs the factory steps configured in the
 * database (multipliers, pipelines, branches, erasers, triggers). A custom implementation may
 * override {@link #doProcess(Properties, TwinFactoryEntity, FactoryContext)} to provide any hardcoded
 * logic while still being assignable per factory via {@code twin_factory.factory_processor_featurer_id}.
 * <p>
 * The {@link #process(TwinFactoryEntity, FactoryContext)} template method is final on purpose: it
 * enforces the recursion guard ({@code FactoryBranchId.alreadyVisited}) and branch level bookkeeping
 * for every implementation, so a custom processor cannot accidentally bypass them and cause a
 * stack overflow on cyclic factory configs.
 */
@FeaturerType(id = FeaturerTwins.TYPE_54,
        name = "FactoryProcessor",
        description = "Processes a single factory run (multipliers, pipelines, branches, erasers, triggers)")
@Slf4j
public abstract class FactoryProcessor extends FeaturerTwins {

    public final void process(TwinFactoryEntity factoryEntity, FactoryContext factoryContext) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, factoryEntity.getFactoryProcessorParams());
        log.info("Running factoryProcessor[{}] with params: {}", this.getClass().getSimpleName(), properties.toString());
        if (factoryContext.getCurrentFactoryBranchId() == null)   //we are in root factory
            factoryContext.setCurrentFactoryBranchId(FactoryBranchId.root(factoryEntity.getId()));
        else if (factoryContext.getCurrentFactoryBranchId().alreadyVisited(factoryEntity.getId()))
            throw new ServiceException(ErrorCodeTwins.FACTORY_INCORRECT, "Incorrect factory config: recursion call. Current branch[" + factoryContext.getCurrentFactoryBranchId() + "]");
        else
            factoryContext.currentFactoryBranchLevelDown(factoryEntity.getId()); //branchId must be incremented
        doProcess(properties, factoryEntity, factoryContext);
    }

    protected abstract void doProcess(Properties properties, TwinFactoryEntity factoryEntity, FactoryContext factoryContext) throws ServiceException;
}
