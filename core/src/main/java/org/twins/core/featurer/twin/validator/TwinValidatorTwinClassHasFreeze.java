package org.twins.core.featurer.twin.validator;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.ValidationResult;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamUUIDSet;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.params.FeaturerParamUUIDSetTwinClassFreezeId;

import java.util.Properties;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Component
@Featurer(id = FeaturerTwins.ID_1619,
        name = "Twin class has freeze",
        description = "Validator for checking the freeze of a twin class")
public class TwinValidatorTwinClassHasFreeze extends TwinValidator {
    @FeaturerParam(name = "Freeze ids", description = "if empty - validator will check any freeze id", order = 1, optional = true)
    public static final FeaturerParamUUIDSet freezeIds = new FeaturerParamUUIDSetTwinClassFreezeId("freezeIds");
    @Override
    protected ValidationResult isValid(Properties properties, TwinEntity twinEntity, boolean invert) throws ServiceException {
        Set<UUID> freezeIdSet = freezeIds.extract(properties);
        UUID twinClassFreezeId = twinEntity.getTwinClass().getTwinClassFreezeId();

        boolean isValid = freezeIdSet.isEmpty()
                ? twinClassFreezeId != null
                : twinClassFreezeId != null && freezeIdSet.contains(twinClassFreezeId);

        return buildResult(isValid,
                invert,
                twinEntity.getTwinClass().logShort() + " has no freezes from given set",
                twinEntity.getTwinClass().logShort() + " has freezes from given set");
    }
}
