package org.twins.core.featurer.fieldtyper;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamBoolean;
import org.cambium.featurer.params.FeaturerParamMap;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinFieldDecimalRepository;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinField;
import org.twins.core.domain.search.TwinFieldSearchNotImplemented;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorText;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageCalcSumByLinkWithTwinType;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;

import java.util.Properties;

@Component
@Featurer(
        id = FeaturerTwins.ID_1352,
        name = "Sum fields by link with twin type",
        description = "Sum of different fields from related twins based on twin type. The twin type (type_option_id) determines which field to sum for each twin."
)
@RequiredArgsConstructor
public class FieldTyperCalcSumByLinkWithTwinType extends FieldTyperImmutable<FieldDescriptorText, FieldValueText, TwinFieldStorageCalcSumByLinkWithTwinType, TwinFieldSearchNotImplemented> implements FieldTyperCalcByLink {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final TwinFieldDecimalRepository twinFieldDecimalRepository;

    @FeaturerParam(name = "fieldIdByTwinTypeId", description = "JSON map: twin type (type_option_id) -> fieldId to sum. Example: {\"twin-type-uuid-1\": \"field-uuid-1\", \"twin-type-uuid-2\": \"field-uuid-2\"}", order = 10)
    public static final FeaturerParamMap fieldIdByTwinTypeId = new FeaturerParamMap("fieldIdByTwinTypeId");

    @FeaturerParam(name = "skipIfNotFound", description = "Skip twin if type option not found in map (true) or throw error (false)",optional = true, defaultValue = "true", order = 11)
    public static final FeaturerParamBoolean skipIfNotFound = new FeaturerParamBoolean("skipIfNotFound");

    @Override
    protected FieldDescriptorText getFieldDescriptor(TwinClassFieldEntity twinClassFieldEntity, Properties properties) throws ServiceException {
        return new FieldDescriptorText();
    }

    @Override
    protected FieldValueText deserializeValue(Properties properties, TwinField twinField) throws ServiceException {
        return new FieldValueText(twinField.getTwinClassField())
                .setValue(String.valueOf(twinField.getTwin().getTwinFieldCalculated().get(twinField.getTwinClassFieldId())));
    }

    @Override
    public TwinFieldStorageCalcSumByLinkWithTwinType getStorage(TwinClassFieldEntity twinClassFieldEntity, Properties properties) throws ServiceException {
        try {
            String fieldIdByTwinTypeIdJson = OBJECT_MAPPER.writeValueAsString(fieldIdByTwinTypeId.extract(properties));
            return new TwinFieldStorageCalcSumByLinkWithTwinType(
                    twinClassFieldEntity.getId(),
                    twinFieldDecimalRepository,
                    linkIds.extract(properties),
                    srcElseDst.extract(properties),
                    linkedTwinInStatusIdSet.extract(properties),
                    linkedTwinOfClassIds.extract(properties),
                    statusExclude.extract(properties),
                    fieldIdByTwinTypeIdJson,
                    skipIfNotFound.extract(properties)
            );
        } catch (Exception e) {
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_INCORRECT, "Error serializing fieldIdByTwinTypeId map: " + e.getMessage(), e);
        }
    }
}
