package org.twins.core.featurer.classfield.filter;

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
        name = "Filter fields by boolean field",
        description = "Filter depending on given boolean field value")
public class FieldFilterDependOnBooleanField extends FieldFilter{
    private final TwinService twinService;

    @FeaturerParam(name = "Boolean field id", description = "Given boolean field by witch fields filtering", order = 1)
    public static final FeaturerParamUUID booleanFieldId = new FeaturerParamUUIDTwinsTwinClassFieldId("booleanFieldId");

    @FeaturerParam(name = "Exclude on true", description = "If true, filter fields by boolean field value true, if false - by field value false", order = 2, optional = true, defaultValue = "true")
    public static final FeaturerParamBoolean excludeOnTrue = new FeaturerParamBoolean("excludeOnTrue");

    @Override
    public void filterFields(Properties properties, Kit<TwinClassFieldEntity, UUID> unfilteredFieldsKit, TwinEntity twin, List<TwinClassFieldEntity> fields) throws ServiceException {
        twinService.loadTwinFields(twin);
        TwinFieldBooleanEntity booleanField = twin.getTwinFieldBooleanKit().get(booleanFieldId.extract(properties));

        boolean booleanFieldValue = booleanField != null && Boolean.TRUE.equals(booleanField.getValue());

        if (booleanFieldValue == excludeOnTrue.extract(properties)) {
            unfilteredFieldsKit.addAll(fields);
        }
    }
}
