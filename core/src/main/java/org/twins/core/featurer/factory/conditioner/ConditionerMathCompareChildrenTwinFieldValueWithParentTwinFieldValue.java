package org.twins.core.featurer.factory.conditioner;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamBoolean;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.cambium.featurer.params.FeaturerParamUUIDSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldSimpleNoRelationsProjection;
import org.twins.core.dao.twin.TwinIdNoRelationsProjection;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.search.BasicSearch;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.factory.filler.FieldLookupMode;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;
import org.twins.core.featurer.params.FeaturerParamUUIDSetTwinsStatusId;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsTwinClassFieldId;
import org.twins.core.service.twin.TwinFieldSimpleSearchService;
import org.twins.core.service.twin.TwinSearchService;

import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@Featurer(id = FeaturerTwins.ID_2429,
        name = "ConditionerMathCompareChildrenTwinFieldValueWithParentTwinFieldValue",
        description = "Is there NOT at least one twin in statuses that satisfies the condition? children-comparison, parent-greater.")
@Slf4j
public class ConditionerMathCompareChildrenTwinFieldValueWithParentTwinFieldValue extends Conditioner {

    @FeaturerParam(name = "statusIds", description = "")
    public static final FeaturerParamUUIDSet statusIds = new FeaturerParamUUIDSetTwinsStatusId("statusIds");

    @FeaturerParam(name = "greaterTwinClassField", description = "")
    public static final FeaturerParamUUID greaterTwinClassField = new FeaturerParamUUIDTwinsTwinClassFieldId("greaterTwinClassField");

    @FeaturerParam(name = "comparisonTwinClassField", description = "")
    public static final FeaturerParamUUID comparisonTwinClassField = new FeaturerParamUUIDTwinsTwinClassFieldId("comparisonTwinClassField");

    @FeaturerParam(name = "equals", description = "")
    public static final FeaturerParamBoolean equals = new FeaturerParamBoolean("equals");

    @Lazy
    @Autowired
    private TwinFieldSimpleSearchService twinFieldSimpleSearchService;

    @Override
    public boolean check(Properties properties, FactoryItem factoryItem) throws ServiceException {
        BasicSearch search = new BasicSearch();
        search
                .addHeaderTwinId(factoryItem.getOutput().getTwinEntity().getId())
                .setTwinIdExcludeList(factoryItem.getFactoryContext().getInputTwinList().stream().map(TwinEntity::getId).collect(Collectors.toSet()))
                .addStatusId(statusIds.extract(properties), false);
        FieldValue greaterValue = factoryService.lookupFieldValue(factoryItem, greaterTwinClassField.extract(properties), FieldLookupMode.fromItemOutputUncommitedFields);
        double comparison, greater;
        List<TwinFieldSimpleNoRelationsProjection> twinFieldSimpleValues = twinFieldSimpleSearchService.findTwinFieldSimpleValuesByTwinIds(search);
        for(TwinFieldSimpleNoRelationsProjection field : twinFieldSimpleValues) {
            if (field.twinClassFieldId().equals(comparisonTwinClassField.extract(properties))) {
                try {
                    Number greaterNumber = NumberUtils.createNumber(field.value());
                    comparison = greaterNumber.doubleValue();
                } catch (Exception e) {
                    throw new ServiceException(ErrorCodeTwins.FACTORY_MULTIPLIER_ERROR, "greaterTwinClassField[" + greaterTwinClassField + "] can not be converted to number");
                }
                if (greaterValue instanceof FieldValueText comparisonValueText) {
                    Number comparisonNumber = NumberUtils.createNumber(comparisonValueText.getValue());
                    greater = comparisonNumber.doubleValue();
                } else
                    throw new ServiceException(ErrorCodeTwins.FACTORY_MULTIPLIER_ERROR, "comparisonTwinClassField[" + comparisonTwinClassField + "] is not instance of text field and can not be converted to number");
                if (equals.extract(properties) ? greater >= comparison : greater > comparison)
                    return false;
            }
        }
        return true;
    }
}
