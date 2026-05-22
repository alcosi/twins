package org.twins.core.featurer.factory.filler;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.TwinBasicFields;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.value.FieldValueUser;
import org.twins.core.featurer.params.FeaturerParamBasicsTwinBasicField;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsTwinClassFieldId;
import org.twins.core.service.twinclass.TwinClassFieldService;

import java.util.Properties;


@Component
@Featurer(id = FeaturerTwins.ID_2353,
        name = "Field user from output twin basic field",
        description = "Fill the user field with assignee-or-creator of the same factory item output twin")
@Slf4j
public class FillerFieldUserFromOutputTwinBasicField extends Filler {

    @FeaturerParam(name = "Twin class field id", description = "TwinClassFieldId for filling", order = 1)
    public static final FeaturerParamUUID twinClassFieldId = new FeaturerParamUUIDTwinsTwinClassFieldId("twinClassFieldId");

    @FeaturerParam(name = "Field", description = "Basic field to check", order = 2)
    public static final FeaturerParamBasicsTwinBasicField field = new FeaturerParamBasicsTwinBasicField("field");

    @Lazy
    @Autowired
    private TwinClassFieldService twinClassFieldService;

    @Override
    public void fill(Properties properties, FactoryItem factoryItem, TwinEntity templateTwin) throws ServiceException {
        TwinEntity outputTwin = factoryItem.getOutput().getTwinEntity();
        TwinBasicFields.Basics fieldName = field.extract(properties);
        FieldValueUser fieldValue = new FieldValueUser(twinClassFieldService.findEntitySafe(twinClassFieldId.extract(properties)));
        switch (fieldName) {
            case createdByUserId -> {
                if (outputTwin.getCreatedByUserId() == null) {
                    throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "No creator detected for twin: " + outputTwin.logDetailed());
                }
                fieldValue.add(outputTwin.getCreatedByUser());
            }
            case assigneeUserId -> {
                if (outputTwin.getAssignerUserId() == null) {
                    throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "No assignee detected for twin: " + outputTwin.logDetailed());
                }
                fieldValue.add(outputTwin.getAssignerUser());
            }
            default ->
                    throw new ServiceException(ErrorCodeTwins.TWIN_BASIC_FIELD_UNKNOWN, "Unknown/Unsupported in featurer twin basic field: " + fieldName);
        }
        factoryItem.getOutput().addField(fieldValue);
    }
}
