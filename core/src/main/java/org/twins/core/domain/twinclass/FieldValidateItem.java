package org.twins.core.domain.twinclass;

import lombok.Data;
import lombok.experimental.Accessors;
import org.cambium.common.ValidationResult;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.validator.TwinClassFieldValidatorEntity;
import org.twins.core.featurer.fieldtyper.value.FieldValue;

import java.util.Map;
import java.util.UUID;

/**
 * One field-validator invocation unit for {@link org.twins.core.featurer.fieldvalidator.FieldValidator}
 * batch processing. The caller owns the list; the featurer writes {@link #result}.
 */
@Data
@Accessors(chain = true)
public class FieldValidateItem {
    private TwinClassFieldValidatorEntity validatorEntity;
    private TwinEntity twinEntity;
    private FieldValue value;
    /** Payload fields of the current create/update — win over DB values during cross-field checks. */
    private Map<UUID, FieldValue> contextFields;
    private ValidationResult result;
}
