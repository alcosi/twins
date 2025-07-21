package org.twins.core.featurer.fieldtyper;

import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldSimpleRepository;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.domain.TwinField;
import org.twins.core.domain.search.TwinFieldSearchNotImplemented;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorStatistic;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageCalcSumField;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsTwinClassFieldId;
import org.twins.core.service.twin.TwinSearchService;

import java.util.Properties;

@Component
@Featurer(id = FeaturerTwins.ID_3804,
        name = "Statistic",
        description = "Statistic field")
public class FieldTyperStatistic extends FieldTyper<FieldDescriptorStatistic, FieldValueText, TwinFieldStorageCalcSumField, TwinFieldSearchNotImplemented> {
    @FeaturerParam(name = "Twin statistic id", description = "", order = 1)
    public static final FeaturerParamUUID twinStatisticId = new FeaturerParamUUIDTwinsTwinClassFieldId("twinStatisticId");
    @Lazy
    @Autowired
    TwinSearchService twinSearchService;
    @Autowired
    private TwinFieldSimpleRepository twinFieldSimpleRepository;

    protected FieldDescriptorStatistic getFieldDescriptor(TwinClassFieldEntity twinClassFieldEntity, Properties properties) throws ServiceException {
        //todo impl me
        return new FieldDescriptorStatistic();
    }

    @Override
    protected void serializeValue(Properties properties, TwinEntity twin, FieldValueText value, TwinChangesCollector twinChangesCollector) throws ServiceException {
    }

    @Override
    protected FieldValueText deserializeValue(Properties properties, TwinField twinField) throws ServiceException {
        //id statistic
        //todo impl me
        return new FieldValueText(twinField.getTwinClassField()).setValue(twinField.getTwin().getAssignerUser().getEmail());
    }
}