package org.twins.core.featurer.fieldtyper;

import lombok.RequiredArgsConstructor;
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
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorBoolean;
import org.twins.core.featurer.fieldtyper.value.FieldValueBoolean;
import org.twins.core.featurer.params.FeaturerParamStringTwinsCheckboxType;

import java.util.HashMap;
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
        We need to implement logic which allows us to save Boolean field as non-nullable boolean field with some default value.
        So, basically, when front send to us bool_field=null or doesn't send it at all we should save it with default value.
        The problem is that in mapper we only process those fields which are explicitly specified in json, so if bool_field is missing in json,
        then it would not go through serializeValue method and wouldn't be saved in twin_field_boolean.
        To solve this we implemented logic in deserializeValue that help us return default value even when we don't have a record for this value in db.
        With this logic after creating a new twin with boolean field we're basically returning phantom value (not presented in db),
        but after an update of this field record for value will be created in db and all things will be clear.
        When we want to find twins by values of fields which allows phantom values we need to use script like that:

        select t.*
        from twin t
        inner join twin_class_field tcf
            on tcf.id = 'f6472a21-b1b6-4d3f-91ed-e7ce06fa0fe1'
                and tcf.twin_class_id = t.twin_class_id
        left join twin_field_boolean tfb
            on tfb.twin_id = t.id
                and tfb.twin_class_field_id = 'f6472a21-b1b6-4d3f-91ed-e7ce06fa0fe1'
        where tfb.twin_id is null or tfb.value = false|true;
    */

    @FeaturerParam(name = "DefaultValue", description = "", order = 2, optional = true, defaultValue = "false")
    public static final FeaturerParamBoolean defaultValue = new FeaturerParamBoolean("defaultValue");

    @Override
    protected FieldDescriptorBoolean getFieldDescriptor(TwinClassFieldEntity twinClassFieldEntity, Properties properties) throws ServiceException {
        return new FieldDescriptorBoolean().checkboxType(checkboxType.extract(properties));
    }

    @Override
    protected void serializeValue(Properties properties, TwinFieldBooleanEntity twinFieldBooleanEntity, FieldValueBoolean value, TwinChangesCollector twinChangesCollector) throws ServiceException {
        detectValueChange(
                twinFieldBooleanEntity,
                twinChangesCollector,
                value.isFilled() ? value.getValue() : defaultValue.extract(properties) // if field_value=null in json and field is not required we use defaultValue and save it in db
        );
    }

    @Override
    protected FieldValueBoolean deserializeValue(Properties properties, TwinField twinField, TwinFieldBooleanEntity twinFieldBooleanEntity) throws ServiceException {
        return new FieldValueBoolean(twinField.getTwinClassField())
                .setValue(twinFieldBooleanEntity != null ? twinFieldBooleanEntity.getValue() : defaultValue.extract(properties));
    }

    @Override
    public Specification<TwinEntity> searchBy(TwinFieldSearchBoolean twinFieldSearchBoolean) throws ServiceException {
        TwinClassFieldEntity fieldEntity = twinFieldSearchBoolean.getTwinClassFieldEntity();
        Properties properties = featurerService.extractProperties(this, fieldEntity.getFieldTyperParams(), new HashMap<>());
        Boolean isRequired = fieldEntity.getRequired();

        return isRequired
                ? TwinSpecification.checkFieldBoolean(twinFieldSearchBoolean)
                : TwinSpecification.checkFieldBooleanWithPhantoms(twinFieldSearchBoolean, defaultValue.extract(properties));
    }
}
