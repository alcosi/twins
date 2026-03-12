package org.twins.core.featurer.twin.validator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.ValidationResult;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamBoolean;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.cambium.featurer.params.FeaturerParamUUIDSet;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.search.BasicSearch;
import org.twins.core.domain.TwinFieldClause;
import org.twins.core.domain.TwinFieldFilter;
import org.twins.core.domain.search.TwinFieldSearch;
import org.twins.core.domain.search.TwinFieldValueSearch;
import org.twins.core.domain.search.TwinFieldValueSearchBoolean;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.FieldTyper;
import org.twins.core.featurer.params.FeaturerParamUUIDSetTwinsClassId;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsTwinClassFieldId;
import org.twins.core.service.twin.TwinSearchService;
import org.twins.core.service.twinclass.TwinClassFieldService;

import java.util.*;

@Slf4j
@Component
@Featurer(id = FeaturerTwins.ID_1620,
        name = "Twin children has field value",
        description = "Validates that at least one child twin has a specific value in the specified field")
@RequiredArgsConstructor
public class TwinValidatorTwinChildrenBooleanFieldHasValue extends TwinValidator {
    @FeaturerParam(name = "Twin class field id", order = 1)
    public static final FeaturerParamUUID twinClassFieldId = new FeaturerParamUUIDTwinsTwinClassFieldId("twinClassFieldId");

    @FeaturerParam(name = "Value", order = 2)
    public static final FeaturerParamBoolean value = new FeaturerParamBoolean("value");

    @FeaturerParam(name = "Children twin class id", order = 3, optional = true)
    public static final FeaturerParamUUIDSet childrenTwinClassId = new FeaturerParamUUIDSetTwinsClassId("childrenTwinClassIds");

    private final TwinSearchService twinSearchService;
    private final TwinClassFieldService twinClassFieldService;

    @Override
    protected ValidationResult isValid(Properties properties, TwinEntity twinEntity, boolean invert) throws ServiceException {
        UUID fieldId = twinClassFieldId.extract(properties);
        boolean expectedValue = value.extract(properties);

        TwinClassFieldEntity fieldEntity = twinClassFieldService.findEntitySafe(fieldId);

        TwinFieldSearch fieldSearch = new TwinFieldValueSearchBoolean()
                .setValue(expectedValue)
                .setTwinClassFieldEntity(fieldEntity);
        ((TwinFieldValueSearch) fieldSearch).setFieldTyper(featurerService.getFeaturer(fieldEntity.getFieldTyperFeaturerId(), FieldTyper.class));

        BasicSearch basicSearch = new BasicSearch();
        basicSearch
                .addHeadTwinId(twinEntity.getId())
                .setTwinClassExtendsHierarchyContainsIdList(childrenTwinClassId.extract(properties))
                .setFieldsFilter(new TwinFieldFilter().addClause(new TwinFieldClause().addCondition(fieldSearch)));

        boolean isValid = twinSearchService.exists(basicSearch);

        return buildResult(
                isValid,
                invert,
                twinEntity.logShort() + " has no children twin field of class[" + fieldId + "] with value [" + expectedValue + "]",
                twinEntity.logShort() + " has children twin field of class[" + fieldId + "] with value [" + expectedValue + "]");
    }

    @Override
    protected CollectionValidationResult isValid(Properties properties, Collection<TwinEntity> twinEntityCollection, boolean invert) throws ServiceException {
        UUID fieldId = twinClassFieldId.extract(properties);
        boolean expectedValue = value.extract(properties);

        TwinClassFieldEntity fieldEntity = twinClassFieldService.findEntitySafe(fieldId);

        TwinFieldSearch fieldSearch = new TwinFieldValueSearchBoolean()
                .setValue(expectedValue)
                .setTwinClassFieldEntity(fieldEntity);
        ((TwinFieldValueSearch) fieldSearch).setFieldTyper(featurerService.getFeaturer(fieldEntity.getFieldTyperFeaturerId(), FieldTyper.class));

        BasicSearch basicSearch = new BasicSearch();
        basicSearch
                .addHeadTwinId(twinEntityCollection.stream().map(TwinEntity::getId).toList())
                .setTwinClassExtendsHierarchyContainsIdList(childrenTwinClassId.extract(properties))
                .setFieldsFilter(new TwinFieldFilter().addClause(new TwinFieldClause().addCondition(fieldSearch)));

        Map<UUID, Long> headTwinIdToChildrenCount = twinSearchService.countGroupBy(
                basicSearch,
                TwinEntity.Fields.headTwinId
        );

        CollectionValidationResult collectionValidationResult = new CollectionValidationResult();
        for (TwinEntity twinEntity : twinEntityCollection) {
            boolean isValid = headTwinIdToChildrenCount.getOrDefault(twinEntity.getId(), 0L) > 0;
            ValidationResult result = buildResult(
                    isValid,
                    invert,
                    twinEntity.logShort() + " has no children twin field of class[" + fieldId + "] with value [" + expectedValue + "]",
                    twinEntity.logShort() + " has children twin field of class[" + fieldId + "] with value [" + expectedValue + "]");
            collectionValidationResult.getTwinsResults().put(twinEntity.getId(), result);
        }
        return collectionValidationResult;
    }
}
