package org.twins.core.featurer.fieldtyper;

import lombok.RequiredArgsConstructor;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Component;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dao.datalist.DataListOptionRepository;
import org.twins.core.dao.twin.TwinFieldEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

@Component
@Featurer(id = 1306,
        name = "FieldTyperCheckbox",
        description = "")
@RequiredArgsConstructor
public class FieldTyperCheckbox extends FieldTyperChecks<FieldValueSelect> {
    final DataListOptionRepository dataListOptionRepository;

    @Override
    public FieldTypeUIDescriptor getUiDescriptor(Properties properties) {
        UUID listId = listUUID.extract(properties);
        List<String> options = new ArrayList<>();
        for (DataListOptionEntity optionEntity : dataListOptionRepository.findByDataListId(listId))
            options.add(optionEntity.option());
        return new FieldTypeUIDescriptor()
                .type("checkbox")
                .addParam("options", options)
                .addParam("inline", inline.extract(properties));
    }

    @Override
    protected String serializeValue(Properties properties, TwinFieldEntity twinFieldEntity, FieldValueSelect value) {
        return null;
    }

    @Override
    protected FieldValueSelect deserializeValue(Properties properties, Object value) {
        return null;
    }
}
