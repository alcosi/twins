package org.twins.core.featurer.fieldtyper;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
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
import org.twins.core.domain.search.TwinFieldValueSearchBoolean;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorBoolean;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageBoolean;
import org.twins.core.featurer.fieldtyper.value.FieldValueBoolean;
import org.twins.core.featurer.params.FeaturerParamStringTwinsCheckboxType;
import org.twins.core.service.history.HistoryItem;

import java.util.Properties;
import java.util.UUID;

@Component
@Featurer(id = FeaturerTwins.ID_1306,
        name = "Boolean",
        description = "")
@RequiredArgsConstructor
public class FieldTyperBoolean extends FieldTyperSingleValue<
        FieldDescriptorBoolean,
        FieldValueBoolean,
        TwinFieldBooleanEntity,
        Boolean,
        TwinFieldStorageBoolean,
        TwinFieldValueSearchBoolean> {

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

    @Deprecated //better to user FieldInitializer
    @FeaturerParam(name = "DefaultValue", description = "", order = 2, optional = true, defaultValue = "false")
    public static final FeaturerParamBoolean defaultValue = new FeaturerParamBoolean("defaultValue");

    @Override
    public FieldDescriptorBoolean getFieldDescriptor(TwinClassFieldEntity twinClassFieldEntity, Properties properties) throws ServiceException {
        return new FieldDescriptorBoolean().checkboxType(checkboxType.extract(properties));
    }

    @Override
    protected void setEntityValue(TwinFieldBooleanEntity twinFieldEntity, Boolean newValue) {
        twinFieldEntity.setValue(newValue);
    }

    @Override
    protected Boolean getEntityValue(TwinFieldBooleanEntity twinFieldEntity) {
        return twinFieldEntity.getValue();
    }

    @Override
    protected Kit<TwinFieldBooleanEntity, UUID> getFieldKit(TwinEntity twinEntity) {
        return twinEntity.getTwinFieldBooleanKit();
    }

    @Override
    protected TwinFieldBooleanEntity createTwinFieldEntity(TwinEntity twin, TwinClassFieldEntity twinClassField) {
        return TwinFieldBooleanEntity.of(twin, twinClassField);
    }

    @Override
    protected Boolean processValue(Properties properties, TwinFieldBooleanEntity twinFieldBooleanEntity, FieldValueBoolean value) {
        // if field_value=null in json and field is not required we use defaultValue and save it in db
        return value.isNotEmpty() ? value.getValue() : defaultValue.extract(properties);
    }

    @Override
    protected void onCleared(Properties properties, TwinFieldBooleanEntity twinFieldBooleanEntity, TwinChangesCollector twinChangesCollector) {
        // Boolean clears to its configured default value (not delete / not null) — keeps a concrete value
        detectValueChange(twinFieldBooleanEntity, twinChangesCollector, defaultValue.extract(properties));
    }

    @Override
    protected HistoryItem<?> createHistoryItem(TwinFieldBooleanEntity twinFieldBooleanEntity, Boolean newValue) {
        return historyService.fieldChangeSimple(
                twinFieldBooleanEntity.getTwinClassField(),
                twinFieldBooleanEntity.getValue() != null ? twinFieldBooleanEntity.getValue().toString() : null,
                newValue != null ? newValue.toString() : null);
    }

    @Override
    protected FieldValueBoolean deserializeValue(Properties properties, TwinField twinField) throws ServiceException {
        var twinFieldBooleanEntity = resolveTwinFieldEntity(twinField.getTwin(), twinField.getTwinClassField());
        return new FieldValueBoolean(twinField.getTwinClassField())
                .setValue(twinFieldBooleanEntity != null ? twinFieldBooleanEntity.getValue() : defaultValue.extract(properties));
    }

    @Override
    public Specification<TwinEntity> searchBy(TwinFieldValueSearchBoolean twinFieldSearchBoolean) throws ServiceException {
        TwinClassFieldEntity fieldEntity = twinFieldSearchBoolean.getTwinClassFieldEntity();
        Properties properties = featurerService.extractProperties(this, fieldEntity.getFieldTyperParams());
        Boolean isRequired = fieldEntity.getRequired();

        return isRequired
                ? TwinSpecification.checkFieldBoolean(twinFieldSearchBoolean)
                : TwinSpecification.checkFieldBooleanWithPhantoms(twinFieldSearchBoolean, defaultValue.extract(properties));
    }
}
