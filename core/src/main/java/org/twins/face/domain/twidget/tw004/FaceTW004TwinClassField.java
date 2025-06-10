package org.twins.face.domain.twidget.tw004;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;

@Data
@AllArgsConstructor
@Accessors(chain = true)
public class FaceTW004TwinClassField {
    private TwinClassFieldEntity field;
    private boolean editable;
}
