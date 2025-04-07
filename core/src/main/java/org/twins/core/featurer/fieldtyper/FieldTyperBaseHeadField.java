package org.twins.core.featurer.fieldtyper;

import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.twins.core.dao.specifications.twin.TwinSpecification;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.domain.TwinField;
import org.twins.core.domain.search.TwinFieldSearchBaseUuid;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorBaseHead;
import org.twins.core.featurer.fieldtyper.value.FieldValueBaseHead;

import java.util.Properties;

@Component
@Featurer(id = FeaturerTwins.ID_1323,
        name = "BaseHead",
        description = "Field typer for base head twin field")
public class FieldTyperBaseHeadField extends FieldTyper<FieldDescriptorBaseHead, FieldValueBaseHead, TwinEntity, TwinFieldSearchBaseUuid> {

    @Override
    public FieldDescriptorBaseHead getFieldDescriptor(TwinClassFieldEntity twinClassFieldEntity, Properties properties) {
        return new FieldDescriptorBaseHead();
    }

    @Override
    protected void serializeValue(Properties properties, TwinEntity twin, FieldValueBaseHead value, TwinChangesCollector twinChangesCollector) throws ServiceException {
        TwinEntity head = value.getHead();
        if (twinChangesCollector.collectIfChanged(twin, TwinEntity.Fields.headTwinId, twin.getHeadTwinId(), head.getId())) {
            if (twinChangesCollector.isHistoryCollectorEnabled()) {
                twinChangesCollector.getHistoryCollector(twin).add(
                        historyService.headChanged(twin.getHeadTwin(), head));
            }
            twin.setHeadTwin(head);
            twin.setHeadTwinId(head.getId());
        }
        if (value.getTwinClassField().getRequired() && ObjectUtils.isEmpty(head)) {
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_REQUIRED,
                    value.getTwinClassField().logShort() + " is required");
        }
    }

    @Override
    protected FieldValueBaseHead deserializeValue(Properties properties, TwinField twinField) throws ServiceException {
        TwinEntity twin = twinField.getTwin();
        return new FieldValueBaseHead(twinField.getTwinClassField()).setHead(twin.getHeadTwin());
    }

    @Override
    public Specification<TwinEntity> searchBy(TwinFieldSearchBaseUuid search) throws ServiceException {
        return Specification.where(TwinSpecification.checkFieldUuidIn(search, TwinEntity.Fields.headTwinId));
    }
}
