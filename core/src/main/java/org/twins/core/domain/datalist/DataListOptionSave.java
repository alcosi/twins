package org.twins.core.domain.datalist;

import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.i18n.dao.I18nEntity;

import java.util.Map;

@Data
@Accessors(chain = true)
public class DataListOptionSave {
    private String icon;
    private I18nEntity nameI18n;
    private Map<String, String> attributes;
}
