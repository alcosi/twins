package org.twins.core.featurer.fieldtyper;

import lombok.RequiredArgsConstructor;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Component;
import org.twins.core.dao.datalist.DataListOptionRepository;
import org.twins.core.dao.twin.TwinFieldEntity;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorChecks;
import org.twins.core.featurer.fieldtyper.value.FieldValueSelect;

import java.util.Properties;
import java.util.UUID;

@Component
@Featurer(id = 1306,
        name = "FieldTyperCheckbox",
        description = "")
@RequiredArgsConstructor
public class FieldTyperCheckbox extends FieldTyperChecks<FieldDescriptorChecks, FieldValueSelect> {
    final DataListOptionRepository dataListOptionRepository;

    @Override
    public FieldDescriptorChecks getFieldDescriptor(Properties properties) {
        UUID listId = listUUID.extract(properties);
        return new FieldDescriptorChecks()
                .inline(inline.extract(properties))
                .options(dataListOptionRepository.findByDataListId(listId));
    }

    @Override
    protected String serializeValue(Properties properties, TwinFieldEntity twinFieldEntity, FieldValueSelect value) {
        return null;
    }

    @Override
    protected FieldValueSelect deserializeValue(Properties properties, TwinFieldEntity twinFieldEntity, Object value) {
        return null;
    }
}
