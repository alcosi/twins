package org.twins.core.domain.datalist;

import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.i18n.dao.I18nEntity;

@Data
@Accessors(chain = true)
public class DataListAttribute {
    private String key;
    private I18nEntity attributeI18n;
}
