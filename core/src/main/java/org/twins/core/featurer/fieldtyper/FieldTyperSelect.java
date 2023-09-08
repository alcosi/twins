package org.twins.core.featurer.fieldtyper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamBoolean;
import org.cambium.featurer.params.FeaturerParamInt;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.springframework.stereotype.Component;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dao.datalist.DataListOptionRepository;

import java.util.*;

@Component
@Featurer(id = 1305,
        name = "FieldTyperSelect",
        description = "")
@RequiredArgsConstructor
@Slf4j
public class FieldTyperSelect extends FieldTyper<FieldValueSelect> {
    final DataListOptionRepository dataListOptionRepository;

    @FeaturerParam(name = "listUUID", description = "")
    public static final FeaturerParamUUID listUUID = new FeaturerParamUUID("listUUID");

    @FeaturerParam(name = "multiple", description = "If true, then multiple select available")
    public static final FeaturerParamBoolean multiple = new FeaturerParamBoolean("multiple");

    @FeaturerParam(name = "supportCustom", description = "If true, then user can enter custom value")
    public static final FeaturerParamBoolean supportCustom = new FeaturerParamBoolean("supportCustom");

    @FeaturerParam(name = "longListThreshold", description = "If options count is bigger then given threshold longList type will be used")
    public static final FeaturerParamInt longListThreshold = new FeaturerParamInt("longListThreshold");

    @Override
    public FieldTypeUIDescriptor getUiDescriptor(Properties properties) {
        UUID listId = listUUID.extract(properties);
        int listSize = dataListOptionRepository.countByDataListId(listId);
        FieldTypeUIDescriptor fieldTypeUIDescriptor = new FieldTypeUIDescriptor()
                .addParam("supportCustom", supportCustom.extract(properties).toString())
                .addParam("multiple", multiple.extract(properties).toString());
        if (listSize > longListThreshold.extract(properties))
            return fieldTypeUIDescriptor
                    .type("selectLongList")
                    .addParam("listId", listId.toString());
        else {
            List<String> options = new ArrayList<>();
            for (DataListOptionEntity optionEntity : dataListOptionRepository.findByDataListId(listId))
                options.add(optionEntity.option());
            return fieldTypeUIDescriptor
                    .type("select")
                    .addParam("options", options);
        }

    }

    @Override
    protected String serializeValue(Properties properties, FieldValueSelect value) {
        return null;
    }

    public static final String LIST_SPLITTER = "<@2@>";

    @Override
    protected FieldValueSelect deserializeValue(Properties properties, Object value) {
        FieldValueSelect ret = new FieldValueSelect();
        if (value != null)
            for (String dataListOptionUUID : value.toString().split(LIST_SPLITTER)) {
                try {
                    Optional<DataListOptionEntity> dataListOption = dataListOptionRepository.findById(UUID.fromString(dataListOptionUUID));
                    dataListOption.ifPresent(ret::add);
                } catch (Exception e) {
                    log.error("Can not parse dataListOption uuid[" + dataListOptionUUID + "]");
                }
            }
        return ret;
    }
}
