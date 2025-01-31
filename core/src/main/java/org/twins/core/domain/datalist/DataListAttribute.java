package org.twins.core.domain.datalist;

import lombok.Data;
import lombok.experimental.Accessors;
import org.cambium.i18n.dao.I18nEntity;

import java.util.UUID;

@Data
@Accessors(chain = true)
public class DataListAttribute {
    private String key;
    private I18nEntity attributeI18n;
}
