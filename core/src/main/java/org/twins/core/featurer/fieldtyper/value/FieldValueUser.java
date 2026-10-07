package org.twins.core.featurer.fieldtyper.value;

import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.cambium.common.exception.ServiceException;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.exception.ErrorCodeTwins;

import java.util.UUID;
import java.util.function.Function;

@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class FieldValueUser extends FieldValueCollectionImmutable<UserEntity> {
    public FieldValueUser(TwinClassFieldEntity twinClassField) {
        super(twinClassField);
    }

    @Override
    protected Function<UserEntity, UUID> itemGetIdFunction() {
        return UserEntity::getId;
    }

    @Override
    public FieldValueUser newInstance(TwinClassFieldEntity newTwinClassFieldEntity) {
        return new FieldValueUser(newTwinClassFieldEntity);
    }

    /**
     * Mirrors {@link FieldValueLink#getSingleLinkedTwinSafe}: extracts the single user of a
     * user-typed value (collection or single variant), throwing on empty / multiple / wrong type.
     */
    public static UserEntity getSingleUserSafe(FieldValue fieldValue) throws ServiceException {
        if (fieldValue == null)
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_TYPE_INCORRECT, "TwinClassField value is empty");
        if (fieldValue instanceof FieldValueUser fieldValueUser) {
            if (fieldValueUser.isEmpty()) {
                throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_REQUIRED, fieldValue.getTwinClassField().logShort() + " is not filled");
            }
            if (fieldValueUser.size() > 1) {
                throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_MULTIPLY_OPTIONS_ARE_NOT_ALLOWED, fieldValue.getTwinClassField().logShort() + " is filled by multiply users");
            }
            return fieldValueUser.getItems().getFirst();
        }
        if (fieldValue instanceof FieldValueUserSingle single) {
            if (single.isEmpty() || single.getValue() == null) {
                throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_REQUIRED, fieldValue.getTwinClassField().logShort() + " is not filled");
            }
            return single.getValue();
        }
        throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_INCORRECT_TYPE, fieldValue.getTwinClassField().logShort() + " is not for user");
    }
}
