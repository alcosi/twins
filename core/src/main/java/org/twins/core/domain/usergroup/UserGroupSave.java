package org.twins.core.domain.usergroup;

import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dao.i18n.I18nEntity;

import java.util.UUID;

@Data
@Accessors(chain = true)
public class UserGroupSave {
    private I18nEntity nameI18n;
    private I18nEntity descriptionI18n;
    private UUID businessAccountId;
}