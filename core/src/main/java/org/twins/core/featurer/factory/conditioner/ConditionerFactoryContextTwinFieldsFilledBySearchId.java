package org.twins.core.featurer.factory.conditioner;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.util.CollectionUtils;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.dao.twinclass.TwinClassFieldSearchEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.search.TwinClassFieldSearch;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsTwinClassFieldSearchId;
import org.twins.core.service.twinclass.TwinClassFieldSearchService;

import java.util.List;
import java.util.Properties;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Featurer(id = FeaturerTwins.ID_2440,
        name = "Factory context twin fields by search id are filled",
        description = "")
@Slf4j
public class ConditionerFactoryContextTwinFieldsFilledBySearchId extends Conditioner {

    @FeaturerParam(name = "Search id")
    public static final FeaturerParamUUID searchId = new FeaturerParamUUIDTwinsTwinClassFieldSearchId("searchId");

    private final TwinClassFieldSearchService twinClassFieldSearchService;

    @Override
    public boolean check(Properties properties, FactoryItem factoryItem) throws ServiceException {
        TwinClassFieldSearch search = new TwinClassFieldSearch().setConfiguredSearch(twinClassFieldSearchService.findEntitySafe(searchId.extract(properties)));
        List<TwinClassFieldEntity> requiredFields = twinClassFieldSearchService.findTwinClassField(search);

        if (CollectionUtils.isEmpty(requiredFields)) {
            return true;
        }

        TwinEntity twin = factoryItem.getOutput().getTwinEntity();
        Kit<FieldValue, UUID> fieldValuesKit = twin.getFieldValuesKit();

        for (TwinClassFieldEntity field : requiredFields) {
            FieldValue fieldValue = fieldValuesKit.get(field.getId());

            if (fieldValue == null || fieldValue.isEmpty()) {
                return false;
            }

        }
        return true;
    }
}
