package org.twins.core.featurer.fieldtyper;

import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.twins.core.dao.specifications.twin.TwinSpecification;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.domain.TwinField;
import org.twins.core.domain.search.TwinFieldSearchId;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorLinkHead;
import org.twins.core.featurer.fieldtyper.value.FieldValueLinkSingle;
import org.twins.core.service.history.ChangesRecorder;

import java.util.Properties;

@Component
@Featurer(id = FeaturerTwins.ID_1323,
        name = "BaseHead",
        description = "Field typer for base head twin field")
public class FieldTyperBaseHeadField extends FieldTyper<FieldDescriptorLinkHead, FieldValueLinkSingle, TwinEntity, TwinFieldSearchId> {

    @Override
    public FieldDescriptorLinkHead getFieldDescriptor(TwinClassFieldEntity twinClassFieldEntity, Properties properties) {
        return new FieldDescriptorLinkHead();
    }

    @Override
    protected void serializeValue(Properties properties, TwinEntity twin, FieldValueLinkSingle value, TwinChangesCollector twinChangesCollector) throws ServiceException {
        ChangesRecorder<TwinEntity, TwinEntity> changesRecorder = new ChangesRecorder<>(
                twin,
                new TwinEntity(),
                twin, //todo fix me for draft, it should be another recorder
                twinChangesCollector.getHistoryCollector(twin));

        changesRecorder.getUpdateEntity()
                .setHeadTwin(value.getDstTwin())
                .setHeadTwinId(value.getDstTwin().getId());
        twinService.updateTwinHead(changesRecorder);
    }

    @Override
    protected FieldValueLinkSingle deserializeValue(Properties properties, TwinField twinField) throws ServiceException {
        TwinEntity twin = twinField.getTwin();
        return new FieldValueLinkSingle(twinField.getTwinClassField()).setDstTwin(twin.getHeadTwin());
    }

    @Override
    public Specification<TwinEntity> searchBy(TwinFieldSearchId search) throws ServiceException {
        return Specification.where(TwinSpecification.checkFieldUuidIn(search, TwinEntity.Fields.headTwinId));
    }
}
