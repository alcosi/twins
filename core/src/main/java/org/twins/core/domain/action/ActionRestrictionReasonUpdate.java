package org.twins.core.domain.action;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dao.i18n.I18nEntity;

import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class ActionRestrictionReasonUpdate extends ActionRestrictionReasonSave {
    private UUID id;
}
