package org.twins.core.featurer.factory.filler;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.factory.lookuper.FieldLookuperNearest;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;
import org.twins.core.featurer.params.FeaturerParamStringTwinsFactoryFieldLookuper;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsTwinClassFieldId;

import java.util.Properties;
import java.util.UUID;

@Component
@Featurer(id = FeaturerTwins.ID_2346,
        name = "Comment field",
        description = "")
@Slf4j
public class FillerComment extends FillerFieldLookup {

    @FeaturerParam(name = "Field id", description = "", order = 1)
    public static final FeaturerParamUUID fieldId = new FeaturerParamUUIDTwinsTwinClassFieldId("fieldId");

    @FeaturerParam(name = "Field lookuper", description = "Source of the field value", order = 99, optional = true, defaultValue = "fromContextTwinDbFields")
    public static final FeaturerParamStringTwinsFactoryFieldLookuper fieldLookuperParam = new FeaturerParamStringTwinsFactoryFieldLookuper("fieldLookuper");

    @Override
    protected FieldLookuperNearest lookuper(Properties properties) {
        return (FieldLookuperNearest) fieldLookupers.getByType(fieldLookuperParam.extract(properties));
    }

    @Override
    protected UUID lookupFieldId(Properties properties) throws ServiceException {
        return fieldId.extract(properties);
    }

    @Override
    public void fill(Properties properties, FactoryItem factoryItem, TwinEntity templateTwin, FieldValue commentField) throws ServiceException {
        if (commentField instanceof FieldValueText fieldValueText) {
            factoryItem.getOutput().addComment(fieldValueText.getValue());
        } else {
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "targetTwinClassField[" + commentField.getTwinClassFieldId() + "] is not instance of text field");
        }
    }
}
