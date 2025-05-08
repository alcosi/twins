package org.twins.core.featurer.fieldtyper;

import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.domain.TwinField;
import org.twins.core.domain.search.BasicSearch;
import org.twins.core.domain.search.TwinFieldSearchNotImplemented;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorImmutable;
import org.twins.core.featurer.fieldtyper.value.FieldValueStatusSingle;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsTwinClassId;
import org.twins.core.service.twin.TwinSearchService;

import java.util.List;
import java.util.Properties;

@Component
@Featurer(id = FeaturerTwins.ID_1326, name = "ChildStatus", description = "Field typer for status of single child twin")
public class FieldTyperSingleChildStatus extends FieldTyper<FieldDescriptorImmutable, FieldValueStatusSingle, TwinEntity, TwinFieldSearchNotImplemented> {

    @FeaturerParam(name = "Child of twin class id", description = "", order = 1)
    public static final FeaturerParamUUID childTwinClassId = new FeaturerParamUUIDTwinsTwinClassId("childTwinClassId");
    @Lazy
    @Autowired
    private TwinSearchService twinSearchService;

    @Override
    public FieldDescriptorImmutable getFieldDescriptor(TwinClassFieldEntity twinClassFieldEntity, Properties properties) {
        return new FieldDescriptorImmutable();
    }

    @Override
    protected void serializeValue(Properties properties, TwinEntity twin, FieldValueStatusSingle value, TwinChangesCollector twinChangesCollector) throws ServiceException {
        throw new ServiceException(ErrorCodeTwins.TWIN_FIELD_IMMUTABLE, "direct status change is not allowed. Use transition instead");
    }

    @Override
    protected FieldValueStatusSingle deserializeValue(Properties properties, TwinField twinField) throws ServiceException {
        TwinEntity twin = twinField.getTwin();
        BasicSearch search = new BasicSearch();
        search
                .addHeadTwinId(twin.getId())
                .addTwinClassId(childTwinClassId.extract(properties), false);
        List<TwinEntity> childList = twinSearchService.findTwins(search); //should be single
        if (childList.size() != 1) {
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_CHILD_STATUS_INCORRECT, "Incorrect child status");
        }
        return new FieldValueStatusSingle(twinField.getTwinClassField()).setStatus(childList.getFirst().getTwinStatus());
    }
}
