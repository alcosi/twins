package org.twins.core.featurer.twin.counter;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.FeaturerType;
import org.twins.core.dao.specifications.GroupExpressionProvider;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.FieldTyper;

import java.util.HashMap;
import java.util.Properties;

/**
 * Count/group featurer (type 54) — the grouping counterpart of {@code TwinSorter} (type 41).
 * Each implementation returns a {@link GroupExpressionProvider} that contributes the GROUP BY
 * expression for a dynamic twin class field (the field value it groups by), JOINing the proper
 * field-storage table on demand.
 */
@FeaturerType(id = FeaturerTwins.TYPE_54, name = "Twin Search Counter", description = "Group/count twin search")
@Slf4j
public abstract class TwinCounter extends FeaturerTwins {

    public GroupExpressionProvider<TwinEntity> createGroup(HashMap<String, String> twinCounterParams, TwinClassFieldEntity twinClassFieldEntity) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, twinCounterParams);
        if (!checkCompatibleCounter(featurerService.getFeaturer(twinClassFieldEntity.getFieldTyperFeaturerId(), FieldTyper.class)))
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_COUNTER_IS_INCOMPATIBLE,
                    "The field typer " + twinClassFieldEntity.getFieldTyperFeaturerId() + " is not compatible with the counter " + twinClassFieldEntity.getTwinCounterFeaturerId() + "; " + twinClassFieldEntity.logShort());
        log.info("Running featurer[{}].createGroup with params: {}", this.getClass().getSimpleName(), properties.toString());
        return createGroup(properties, twinClassFieldEntity);
    }

    public abstract boolean checkCompatibleCounter(FieldTyper fieldTyper) throws ServiceException;

    public abstract GroupExpressionProvider<TwinEntity> createGroup(Properties properties, TwinClassFieldEntity twinClassFieldEntity) throws ServiceException;
}
