package org.twins.core.featurer.fieldtyper;

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
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.util.Properties;

@Component
@Featurer(
        id = FeaturerTwins.ID_1354,
        name = "Sum fields by link with twin flavor",
        description = "Sum of different fields from related twins based on twin flavor. The twin flavor (flavor_data_list_option_id) determines which field to sum for each twin."
)
@RequiredArgsConstructor
public class FieldTyperCalcSumByLinkWithTwinType extends FieldTyperImmutable<FieldDescriptorText, FieldValueText, TwinFieldStorageCalcSumByLinkWithTwinType, TwinFieldSearchNotImplemented> implements FieldTyperCalcByLink {

    private static final ObjectMapper OBJECT_MAPPER = JsonMapper.builder().build();

    private final TwinFieldDecimalRepository twinFieldDecimalRepository;

    @FeaturerParam(name = "fieldIdByTwinFlavorId", description = "JSON map: twin flavor (flavor_data_list_option_id) -> fieldId to sum. Example: {\"twin-flavor-uuid-1\": \"field-uuid-1\", \"twin-flavor-uuid-2\": \"field-uuid-2\"}", order = 10)
    public static final FeaturerParamMap fieldIdByTwinFlavorId = new FeaturerParamMap("fieldIdByTwinFlavorId");

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
            String fieldIdByTwinFlavorIdJson = OBJECT_MAPPER.writeValueAsString(fieldIdByTwinFlavorId.extract(properties));
            return new TwinFieldStorageCalcSumByLinkWithTwinType(
                    twinClassFieldEntity.getId(),
                    twinFieldDecimalRepository,
                    linkIds.extract(properties),
                    srcElseDst.extract(properties),
                    linkedTwinInStatusIdSet.extract(properties),
                    linkedTwinOfClassIds.extract(properties),
                    statusExclude.extract(properties),
                    fieldIdByTwinFlavorIdJson,
                    skipIfNotFound.extract(properties)
            );
        } catch (Exception e) {
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_INCORRECT, "Error serializing fieldIdByTwinFlavorId map: " + e.getMessage(), e);
        }
    }
}
