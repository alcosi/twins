package org.twins.core.dto.rest.twinclass;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;

@Data
@AllArgsConstructor
@Accessors(chain = true)
public class TwinClassFieldEditable {
    private TwinClassFieldEntity field;
    private boolean editable;
}
