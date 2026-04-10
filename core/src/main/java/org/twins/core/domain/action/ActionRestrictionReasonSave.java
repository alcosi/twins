package org.twins.core.domain.action;

import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dao.action.ActionRestrictionReasonEntity;
import org.twins.core.dao.i18n.I18nEntity;

@Data
@Accessors(chain = true)
public class ActionRestrictionReasonSave {
    public ActionRestrictionReasonEntity entity;
    public I18nEntity descriptionI18n;
}
