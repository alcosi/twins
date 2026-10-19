package org.twins.core.domain.twinclass;

import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dao.i18n.I18nEntity;
import org.twins.core.dao.validator.TwinClassFieldValidatorEntity;

@Data
@Accessors(chain = true)
public class TwinClassFieldValidatorSave {
    public TwinClassFieldValidatorEntity twinClassFieldValidator;
    public I18nEntity beValidationErrorI18n;
}
