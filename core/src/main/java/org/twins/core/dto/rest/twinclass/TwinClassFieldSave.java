package org.twins.core.dto.rest.twinclass;

import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dao.i18n.I18nEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;

@Data
@Accessors(chain = true)
public class TwinClassFieldSave {
    private TwinClassFieldEntity field;
    private I18nEntity nameI18n;
    private I18nEntity descriptionI18n;
}
