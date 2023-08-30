package org.twins.core.featurer.fieldtyper;

import lombok.RequiredArgsConstructor;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamBoolean;
import org.cambium.featurer.params.FeaturerParamInt;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.springframework.stereotype.Component;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dao.datalist.DataListOptionRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

@Component
@Featurer(id = 1306,
        name = "FieldTyperCheckbox",
        description = "")
@RequiredArgsConstructor
public class FieldTyperCheckbox extends FieldTyperChecks {
    final DataListOptionRepository dataListOptionRepository;

    @Override
    public FieldTypeUIDescriptor getUiDescriptor(Properties properties) {
        UUID listId = listUUID.extract(properties);
        List<String> options = new ArrayList<>();
        for (DataListOptionEntity optionEntity : dataListOptionRepository.findByDataListId(listId))
            options.add(optionEntity.getOption());
        return new FieldTypeUIDescriptor()
                .type("checkbox")
                .addParam("options", options)
                .addParam("inline", inline.extract(properties));

    }
}
