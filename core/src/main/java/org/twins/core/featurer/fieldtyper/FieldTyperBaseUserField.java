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
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorUser;
import org.twins.core.featurer.fieldtyper.value.FieldValueUserSingle;
import org.twins.core.service.history.ChangesRecorder;

import java.util.Properties;
import java.util.UUID;

import static org.twins.core.service.SystemEntityService.*;

@Component
@Featurer(id = FeaturerTwins.ID_1322,
        name = "BaseUser",
        description = "Field typer for base user twin fields (creator, assignee. owner)")
public class FieldTyperBaseUserField extends FieldTyper<FieldDescriptorUser, FieldValueUserSingle, TwinEntity, TwinFieldSearchId> {

    @Override
    public FieldDescriptorUser getFieldDescriptor(TwinClassFieldEntity twinClassFieldEntity, Properties properties) {
        return new FieldDescriptorUser()
                .multiple(false);
    }

    @Override
    protected void serializeValue(Properties properties, TwinEntity twin, FieldValueUserSingle value, TwinChangesCollector twinChangesCollector) throws ServiceException {
        ChangesRecorder<TwinEntity, TwinEntity> changesRecorder = new ChangesRecorder<>(
                twin,
                new TwinEntity(),
                twin, //todo fix me for draft, it should be another recorder
                twinChangesCollector.getHistoryCollector(twin));

        UUID fieldId = value.getTwinClassField().getId();

        if (fieldId.equals(TWIN_CLASS_FIELD_TWIN_ASSIGNEE_USER)) {
            changesRecorder.getUpdateEntity()
                    .setAssignerUser(value.getUser())
                    .setAssignerUserId(value.getUser().getId());
            twinService.updateTwinAssignee(changesRecorder);
        } else if (fieldId.equals(TWIN_CLASS_FIELD_TWIN_CREATOR_USER)) {
            throw new ServiceException(ErrorCodeTwins.TWIN_FIELD_IMMUTABLE,
                    value.getTwinClassField().logShort() + " currently immutable");
        } else if (fieldId.equals(TWIN_CLASS_FIELD_TWIN_OWNER_USER)) {
            throw new ServiceException(ErrorCodeTwins.TWIN_FIELD_IMMUTABLE,
                    value.getTwinClassField().logShort() + " currently immutable");
        } else {
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_INCORRECT,
                    value.getTwinClassField().logShort() + " is not a supported base field for " + twin.logNormal());
        }
    }

    @Override
    protected FieldValueUserSingle deserializeValue(Properties properties, TwinField twinField) throws ServiceException {
        TwinEntity twin = twinField.getTwin();
        UUID fieldId = twinField.getTwinClassField().getId();
        if (fieldId.equals(TWIN_CLASS_FIELD_TWIN_ASSIGNEE_USER)) {
            return new FieldValueUserSingle(twinField.getTwinClassField()).setUser(twin.getAssignerUser());
        } else if (fieldId.equals(TWIN_CLASS_FIELD_TWIN_CREATOR_USER)) {
            return new FieldValueUserSingle(twinField.getTwinClassField()).setUser(twin.getCreatedByUser());
        } else if (fieldId.equals(TWIN_CLASS_FIELD_TWIN_OWNER_USER)) {
            return new FieldValueUserSingle(twinField.getTwinClassField()).setUser(twin.getOwnerUser());
        }
        throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_INCORRECT,
                "Field [" + twinField.getTwinClassField().logShort() + "] is not a supported base field for " + twin.logNormal());
    }

    @Override
    public Specification<TwinEntity> searchBy(TwinFieldSearchId search) {
        UUID fieldId = search.getTwinClassFieldEntity().getId();
        if (fieldId.equals(TWIN_CLASS_FIELD_TWIN_ASSIGNEE_USER)) {
            return Specification.where(TwinSpecification.checkFieldUuidIn(search, TwinEntity.Fields.assignerUserId));
        } else if (fieldId.equals(TWIN_CLASS_FIELD_TWIN_CREATOR_USER)) {
            return Specification.where(TwinSpecification.checkFieldUuidIn(search, TwinEntity.Fields.createdByUserId));
        } else if (fieldId.equals(TWIN_CLASS_FIELD_TWIN_OWNER_USER)) {
            return Specification.where(TwinSpecification.checkFieldUuidIn(search, TwinEntity.Fields.ownerUserId));
        } else {
            return null;
        }
    }
}
