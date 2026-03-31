package org.twins.core.featurer.classfield.filter;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.featurer.annotations.FeaturerType;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.featurer.FeaturerTwins;

import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

@FeaturerType(id = FeaturerTwins.TYPE_36,
        name = "FieldFilter",
        description = "Filter class fields")
@Slf4j
public abstract class FieldFilter extends FeaturerTwins {
    public Kit<TwinClassFieldEntity, UUID> filterFields(HashMap<String, String> fieldFilterParams, List<TwinClassFieldEntity> fields, TwinEntity twin) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, fieldFilterParams);
        log.info("Running featurer[{}].filterFields with params: {}", this.getClass().getSimpleName(), properties.toString());
        Kit<TwinClassFieldEntity, UUID> fieldsKit = new Kit<>(TwinClassFieldEntity::getId);
        filterFields(properties, fieldsKit, twin, fields);
        return fieldsKit;
    }

    public abstract void filterFields(Properties properties, Kit<TwinClassFieldEntity, UUID> unfilteredFieldsKit, TwinEntity twin, List<TwinClassFieldEntity> fields) throws ServiceException;
}
