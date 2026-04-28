package org.twins.core.featurer.twin.validator;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.CollectionUtils;
import org.cambium.common.util.StringUtils;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamUUIDSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.params.FeaturerParamUUIDSetTwinsTwinClassFieldId;
import org.twins.core.service.SystemEntityService;
import org.twins.core.service.twin.TwinService;

import java.util.*;

@Slf4j
@Component
@Featurer(id = FeaturerTwins.ID_1614,
        name = "Twin fields value is not null",
        description = "")
public class TwinValidatorTwinFieldNotNull extends TwinValidator {

    @FeaturerParam(name = "Twin class field ids", description = "", order = 1)
    public static final FeaturerParamUUIDSet twinClassFieldIds = new FeaturerParamUUIDSetTwinsTwinClassFieldId("twinClassFieldIds");

    @Lazy
    @Autowired
    TwinService twinService;

    @Override
    protected CollectionValidationResult isValid(Properties properties, Collection<TwinEntity> twinEntityCollection, boolean invert) throws ServiceException {
        Set<UUID> fieldClassIds = twinClassFieldIds.extract(properties);

        Set<UUID> basicFieldIds = null;
        Set<UUID> dynamicFieldIds = null;

        for (UUID fieldClassId : fieldClassIds) {
            if (SystemEntityService.isSystemField(fieldClassId)) {
                basicFieldIds = CollectionUtils.safeAdd(basicFieldIds, fieldClassId);
            } else {
                dynamicFieldIds = CollectionUtils.safeAdd(dynamicFieldIds, fieldClassId);
            }
        }
        if (basicFieldIds == null) {
            basicFieldIds = Collections.emptySet();
        }
        if (dynamicFieldIds == null) {
            dynamicFieldIds = Collections.emptySet();
        }

        if (CollectionUtils.isNotEmpty(dynamicFieldIds)) {
            twinService.loadFieldsValues(twinEntityCollection);
        }

        var result = new CollectionValidationResult();
        for (var twinEntity : twinEntityCollection) {
            Set<UUID> nullFieldIds = null;
            for (UUID fieldClassId : basicFieldIds) {
                Object value = SystemEntityService.getSystemFieldValue(twinEntity, fieldClassId);
                if (value == null) {
                    nullFieldIds = CollectionUtils.safeAdd(nullFieldIds, fieldClassId);
                }
            }

            for (UUID fieldClassId : dynamicFieldIds) {
                FieldValue fieldValue = twinEntity.getFieldValuesKit().get(fieldClassId);
                if (fieldValue == null || fieldValue.isEmpty()) {
                    nullFieldIds = CollectionUtils.safeAdd(nullFieldIds, fieldClassId);
                }
            }
            boolean isValid = CollectionUtils.isEmpty(nullFieldIds);
            var nullFieldIdsStr = StringUtils.join(nullFieldIds, ",");
            result.getTwinsResults().put(twinEntity.getId(), buildResult(
                    isValid,
                    invert,
                    twinEntity.logShort() + " fields[" + nullFieldIdsStr + "] are null",
                    twinEntity.logShort() + " there are no null fields found"));
        }
        return result;
    }
}
