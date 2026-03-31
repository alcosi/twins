package org.twins.core.domain.twinclass;

import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dao.i18n.I18nEntity;

import java.util.UUID;

@Data
@Accessors(chain = true)
public class TwinClassFreezeSave {
    private String key;
    private UUID statusId;
    private I18nEntity name;
    private I18nEntity description;
}
