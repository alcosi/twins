package org.twins.core.featurer.fieldtyper;

import lombok.extern.log4j.Log4j2;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamString;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinFieldSimpleNonIndexedEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.domain.TwinField;
import org.twins.core.domain.search.TwinFieldSearchNotImplemented;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorSecret;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;

import java.util.Properties;

@Log4j2
@Component
@Featurer(id = FeaturerTwins.ID_1326,
        name = "Secret",
        description = "Secret field")
public class FieldTyperSecret
        extends FieldTyperSimpleNonIndexed<FieldDescriptorSecret, FieldValueText, TwinFieldSearchNotImplemented> {

    @FeaturerParam(name = "Regexp", description = "")
    public static final FeaturerParamString regexp = new FeaturerParamString("regexp");

    @Autowired
    private StandardPBEStringEncryptor secretEncryptor;

    @Override
    protected void serializeValue(Properties properties, TwinFieldSimpleNonIndexedEntity twinFieldSimpleNonIndexedEntity,
                                  FieldValueText value, TwinChangesCollector twinChangesCollector)
            throws ServiceException {
        String pattern = regexp.extract(properties);

        if (!value.getValue().matches(pattern)) {
            throw new ServiceException(
                    ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_INCORRECT,
                    twinFieldSimpleNonIndexedEntity.getTwinClassField().easyLog(EasyLoggable.Level.NORMAL)
                            + " value["
                            + value.getValue()
                            + "] does not match pattern["
                            + pattern + "]"
            );
        }

        value.setValue(secretEncryptor.encrypt(value.getValue()));
        detectValueChange(twinFieldSimpleNonIndexedEntity, twinChangesCollector, value.getValue());
    }

    @Override
    protected FieldValueText deserializeValue(Properties properties, TwinField twinField,
                                              TwinFieldSimpleNonIndexedEntity twinFieldSimpleNonIndexedEntity) throws ServiceException {
        return new FieldValueText(twinField.getTwinClassField())
                .setValue(
                        twinFieldSimpleNonIndexedEntity != null
                                ? secretEncryptor.decrypt(twinFieldSimpleNonIndexedEntity.getValue())
                                : null
                );
    }

    @Override
    protected FieldDescriptorSecret getFieldDescriptor(TwinClassFieldEntity twinClassFieldEntity,
                                                       Properties properties) throws ServiceException {
        return new FieldDescriptorSecret()
                .regExp(regexp.extract(properties));
    }
}
