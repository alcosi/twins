package org.twins.face.service.twidget;

import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dao.twin.TwinEntity;

@Data
@Accessors(chain = true)
public class TwidgetTwinPointer <T> {
    private TwinEntity twinEntity;
    private T widgetConfigEntity;
}
