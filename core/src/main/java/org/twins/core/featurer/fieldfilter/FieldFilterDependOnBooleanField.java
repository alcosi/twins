package org.twins.core.featurer.fieldfilter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamBoolean;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldBooleanEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsTwinClassFieldId;
import org.twins.core.service.twin.TwinService;

import java.util.List;
import java.util.Properties;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
@Featurer(id = FeaturerTwins.ID_3604,
        name = "Filter given fields by boolean field",
        description = "")
public class FieldFilterDependOnBooleanField extends FieldFilter{
    private final TwinService twinService;

    @FeaturerParam(name = "Boolean field id", description = "", order = 1)
    public static final FeaturerParamUUID booleanFieldId = new FeaturerParamUUIDTwinsTwinClassFieldId("booleanFieldId");

    @FeaturerParam(name = "Exclude on true", description = "", optional = true, defaultValue = "true")
    public static final FeaturerParamBoolean excludeOnTrue = new FeaturerParamBoolean("excludeOnTrue");

    @Override
    public void filterFields(Properties properties, Kit<TwinClassFieldEntity, UUID> unfilteredFieldsKit, TwinEntity twin, List<TwinClassFieldEntity> fields) throws ServiceException {
        twinService.loadTwinFields(twin);
        TwinFieldBooleanEntity booleanField = twin.getTwinFieldBooleanKit().get(booleanFieldId.extract(properties));

        if (booleanField.getValue() == excludeOnTrue.extract(properties)) {
            unfilteredFieldsKit.addAll(fields);
        }
    }
}
