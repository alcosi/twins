package org.twins.face.domain.widget.wt006;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;

@Data
@AllArgsConstructor
@Accessors(chain = true)
public class FaceWT006TwinClassField {
    private TwinClassFieldEntity field;
    private boolean editable;
    private int order;
}
