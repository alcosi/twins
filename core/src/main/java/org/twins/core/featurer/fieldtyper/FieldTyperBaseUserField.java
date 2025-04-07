package org.twins.core.featurer.fieldtyper;

import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.twins.core.dao.history.HistoryType;
import org.twins.core.dao.specifications.twin.TwinSpecification;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.domain.TwinField;
import org.twins.core.domain.search.TwinFieldSearchBaseUuid;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorBaseUser;
import org.twins.core.featurer.fieldtyper.value.FieldValueBaseUser;

import java.util.Properties;
import java.util.UUID;

import static org.twins.core.service.SystemEntityService.*;

@Component
@Featurer(id = FeaturerTwins.ID_1322,
        name = "BaseUser",
        description = "Field typer for base user twin fields (creator, assignee. owner)")
public class FieldTyperBaseUserField extends FieldTyper<FieldDescriptorBaseUser, FieldValueBaseUser, TwinEntity, TwinFieldSearchBaseUuid> {

    @Override
    public FieldDescriptorBaseUser getFieldDescriptor(TwinClassFieldEntity twinClassFieldEntity, Properties properties) {
        return new FieldDescriptorBaseUser();
    }

    @Override
    protected void serializeValue(Properties properties, TwinEntity twin, FieldValueBaseUser value, TwinChangesCollector twinChangesCollector) throws ServiceException {
        UUID fieldId = value.getTwinClassField().getId();
        UserEntity user = value.getUser();

        if (fieldId.equals(TWIN_CLASS_FIELD_TWIN_ASSIGNEE_USER)) {
            if (twinChangesCollector.collectIfChanged(twin, TwinEntity.Fields.assignerUserId, twin.getAssignerUserId(), user.getId())) {
                if (twinChangesCollector.isHistoryCollectorEnabled()) {
                    twinChangesCollector.getHistoryCollector(twin).add(
                            historyService.userChanged(twin.getAssignerUser(), user, HistoryType.assigneeChanged));
                }
                twin.setAssignerUser(user);
                twin.setAssignerUserId(user.getId());
            }
        } else if (fieldId.equals(TWIN_CLASS_FIELD_TWIN_CREATOR_USER)) {
            if (twinChangesCollector.collectIfChanged(twin, TwinEntity.Fields.createdByUserId, twin.getCreatedByUserId(), user.getId())) {
                if (twinChangesCollector.isHistoryCollectorEnabled()) {
                    twinChangesCollector.getHistoryCollector(twin).add(
                            historyService.userChanged(twin.getCreatedByUser(), user, HistoryType.createdByChanged));
                }
                twin.setCreatedByUser(user);
                twin.setCreatedByUserId(user.getId());
            }
        } else if (fieldId.equals(TWIN_CLASS_FIELD_TWIN_OWNER_USER)) {
            if (twinChangesCollector.collectIfChanged(twin, TwinEntity.Fields.ownerUserId, twin.getOwnerUserId(), user.getId())) {
                if (twinChangesCollector.isHistoryCollectorEnabled()) {
                    twinChangesCollector.getHistoryCollector(twin).add(
                            historyService.userChanged(twin.getOwnerUser(), user, HistoryType.ownerChanged));
                }
                twin.setOwnerUser(user);
                twin.setOwnerUserId(user.getId());
            }
        } else {
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_INCORRECT,
                    value.getTwinClassField().logShort() + " is not a supported base field for " + twin.logNormal());
        }
        if (value.getTwinClassField().getRequired() && ObjectUtils.isEmpty(user)) {
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_REQUIRED,
                    value.getTwinClassField().logShort() + " is required");
        }
    }

    @Override
    protected FieldValueBaseUser deserializeValue(Properties properties, TwinField twinField) throws ServiceException {
        TwinEntity twin = twinField.getTwin();
        UUID fieldId = twinField.getTwinClassField().getId();
        if (fieldId.equals(TWIN_CLASS_FIELD_TWIN_ASSIGNEE_USER)) {
            return new FieldValueBaseUser(twinField.getTwinClassField()).setUser(twin.getAssignerUser());
        } else if (fieldId.equals(TWIN_CLASS_FIELD_TWIN_CREATOR_USER)) {
            return new FieldValueBaseUser(twinField.getTwinClassField()).setUser(twin.getCreatedByUser());
        } else if (fieldId.equals(TWIN_CLASS_FIELD_TWIN_OWNER_USER)) {
            return new FieldValueBaseUser(twinField.getTwinClassField()).setUser(twin.getOwnerUser());
        }
        throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_INCORRECT,
                "Field [" + twinField.getTwinClassField().logShort() + "] is not a supported base field for " + twin.logNormal());
    }

    @Override
    public Specification<TwinEntity> searchBy(TwinFieldSearchBaseUuid search) {
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
