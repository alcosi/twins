package org.twins.core.domain.twinclass;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.domain.EntityRelinkOperation;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class TwinClassUpdate extends TwinClassSave {
    private EntityRelinkOperation markerDataListUpdate;
    private EntityRelinkOperation tagDataListUpdate;
    private EntityRelinkOperation extendsTwinClassUpdate;
    private EntityRelinkOperation headTwinClassUpdate;
}
