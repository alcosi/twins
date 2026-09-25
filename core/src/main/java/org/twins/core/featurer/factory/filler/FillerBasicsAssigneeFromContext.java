package org.twins.core.featurer.factory.filler;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.factory.lookuper.FieldLookuperNearest;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.fieldtyper.value.FieldValueUser;
import org.twins.core.featurer.params.FeaturerParamStringTwinsFactoryFieldLookuper;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsTwinClassFieldId;

import java.util.Properties;
import java.util.UUID;

@Component
@Featurer(id = FeaturerTwins.ID_2324,
        name = "Basics assignee from context",
        description = "")
@Slf4j
public class FillerBasicsAssigneeFromContext extends FillerFieldLookup {
    @FeaturerParam(name = "Assignee field", description = "", order = 1)
    public static final FeaturerParamUUID assigneeField = new FeaturerParamUUIDTwinsTwinClassFieldId("assigneeField");

    @FeaturerParam(name = "Field lookuper", description = "Source of the field value", order = 99, optional = true, defaultValue = "fromContextFieldsAndContextTwinDbFields")
    public static final FeaturerParamStringTwinsFactoryFieldLookuper fieldLookuperParam = new FeaturerParamStringTwinsFactoryFieldLookuper("fieldLookuper");

    @Override
    protected FieldLookuperNearest lookuper(Properties properties) {
        return (FieldLookuperNearest) fieldLookupers.getByType(fieldLookuperParam.extract(properties));
    }

    @Override
    protected UUID lookupFieldId(Properties properties) throws ServiceException {
        return assigneeField.extract(properties);
    }

    @Override
    public void fill(Properties properties, FactoryItem factoryItem, TwinEntity templateTwin, FieldValue fieldValue) throws ServiceException {
        fieldValue.assertIsDefined(fieldValue.getTwinClassField().logNormal() + " is not present in context fields and in context twins");
        TwinEntity outputTwinEntity = factoryItem.getOutput().getTwinEntity();
        UserEntity assignee = FieldValueUser.getSingleUserSafe(fieldValue);
        log.info("{} [assignee] will be filled from {}", outputTwinEntity.logShort(), fieldValue.getTwinClassField().logShort());
        outputTwinEntity
                .setAssignerUser(assignee)
                .setAssignerUserId(assignee.getId());
    }
}
