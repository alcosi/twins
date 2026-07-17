package org.twins.core.domain.twin;

import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dao.twin.TwinPointerEntity;

@Data
@Accessors(chain = true)
public class TwinPointerSave {
    private TwinPointerEntity twinPointer;
}
