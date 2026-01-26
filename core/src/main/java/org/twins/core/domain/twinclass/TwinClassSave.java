package org.twins.core.domain.twinclass;

import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dao.i18n.I18nEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;

@Data
@Accessors(chain = true)
public class TwinClassSave {
    private TwinClassEntity twinClass;
    private I18nEntity nameI18n;
    private I18nEntity descriptionI18n;
}
