package org.twins.core.domain.datalist;

import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dao.i18n.I18nEntity;

@Data
@Accessors(chain = true)
public class DataListSave {
    private String key;
    private I18nEntity nameI18n;
    private I18nEntity descriptionI18n;
    private DataListAttribute attribute1;
    private DataListAttribute attribute2;
    private DataListAttribute attribute3;
    private DataListAttribute attribute4;
}
