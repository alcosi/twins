package org.twins.core.featurer.fieldtyper;

import lombok.RequiredArgsConstructor;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamBoolean;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.twins.core.dao.specifications.twin.TwinSpecification;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldBooleanEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.domain.TwinField;
import org.twins.core.domain.search.TwinFieldSearchBoolean;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorBoolean;
import org.twins.core.featurer.fieldtyper.value.FieldValueBoolean;
import org.twins.core.featurer.params.FeaturerParamStringTwinsCheckboxType;

import java.util.Properties;

@Component
@Featurer(id = FeaturerTwins.ID_1306,
        name = "Boolean",
        description = "")
@RequiredArgsConstructor
public class FieldTyperBooleanV1 extends FieldTyperBoolean<FieldDescriptorBoolean, FieldValueBoolean, TwinFieldSearchBoolean> {

    @FeaturerParam(name = "CheckboxType", description = "", order = 1, optional = true, defaultValue = "TOGGLE")
    public static final FeaturerParamStringTwinsCheckboxType checkboxType = new FeaturerParamStringTwinsCheckboxType("checkboxType");

    /*
        We need to implement logic which allows us to save Boolean field as non-nullable boolean field with default value of false.
        So, basically, when front send to us bool_field=null or doesn't send it at all we should save it as false.
        The problem is that in mapper we only process those fields which are explicitly specified in json, so if bool_field is missing in json,
        then it would not go through serializeValue method and wouldn't be saved in twin_field_boolean.
        To solve this we implemented logic in deserializeValue that help us return default value(false) even when we don't have a record for this value in db.
        With this logic after creating a new twin with boolean nullable=false field we're basically returning phantom value (not presented in db),
        but after an update of this field record for value will be created in db and all things will be clear.
        So, because of this logic phantom values can only store false.
        We can find twins with phantom value records (and fields with false value) like this:

        select t.*
        from twin t
        inner join twin_class_field tcf
            on tcf.id = 'f6472a21-b1b6-4d3f-91ed-e7ce06fa0fe1'
                and tcf.twin_class_id = t.twin_class_id
        left join twin_field_boolean tfb
            on tfb.twin_id = t.id
                and tfb.twin_class_field_id = 'f6472a21-b1b6-4d3f-91ed-e7ce06fa0fe1'
        where tfb.twin_id is null or tfb.value = false;
    */

    @FeaturerParam(name = "Nullable", description = "", order = 2, optional = true, defaultValue = "true")
    public static final FeaturerParamBoolean nullable = new FeaturerParamBoolean("nullable");

    @Override
    protected FieldDescriptorBoolean getFieldDescriptor(TwinClassFieldEntity twinClassFieldEntity, Properties properties) throws ServiceException {
        return new FieldDescriptorBoolean().checkboxType(checkboxType.extract(properties));
    }

    @Override
    protected void serializeValue(Properties properties, TwinFieldBooleanEntity twinFieldBooleanEntity, FieldValueBoolean value, TwinChangesCollector twinChangesCollector) throws ServiceException {
        if (!nullable.extract(properties) && value.getValue() == null) {
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_INCORRECT, twinFieldBooleanEntity.getTwinClassField().logNormal() + " can't be null");
        }

        detectValueChange(twinFieldBooleanEntity, twinChangesCollector, value.getValue());
    }

    @Override
    protected FieldValueBoolean deserializeValue(Properties properties, TwinField twinField, TwinFieldBooleanEntity twinFieldBooleanEntity) throws ServiceException {
        if (twinFieldBooleanEntity != null) {
            return new FieldValueBoolean(twinField.getTwinClassField())
                    .setValue(twinFieldBooleanEntity.getValue());
        }

        if (!nullable.extract(properties)) {
            return new FieldValueBoolean(twinField.getTwinClassField())
                    .setValue(Boolean.FALSE);
        }

        return new FieldValueBoolean(twinField.getTwinClassField())
                .setValue(null);
    }

    @Override
    public Specification<TwinEntity> searchBy(TwinFieldSearchBoolean twinFieldSearchBoolean) {
        return Specification.where(TwinSpecification.checkFieldBoolean(twinFieldSearchBoolean));
    }
}
