package org.twins.core.domain.twinflow;

import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dao.i18n.I18nEntity;
import org.twins.core.dao.twinflow.TwinflowTransitionEntity;

@Data
@Accessors(chain = true)
public class TransitionSave {
    public TwinflowTransitionEntity entity;
    public I18nEntity nameI18n;
    public I18nEntity descriptionI18n;
}
