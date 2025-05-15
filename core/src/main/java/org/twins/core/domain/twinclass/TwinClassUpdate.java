package org.twins.core.domain.twinclass;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dao.i18n.I18nEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.domain.EntityRelinkOperation;
import org.twins.core.dto.rest.twinclass.TwinClassSave;

import java.util.HashMap;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class TwinClassUpdate extends TwinClassSave {
    private EntityRelinkOperation markerDataListUpdate;
    private EntityRelinkOperation tagDataListUpdate;
    private EntityRelinkOperation extendsTwinClassUpdate;
    private EntityRelinkOperation headTwinClassUpdate;
}
