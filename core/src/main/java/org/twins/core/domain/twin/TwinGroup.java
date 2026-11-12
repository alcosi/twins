package org.twins.core.domain.twin;

import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dao.twin.TwinEntity;

import java.util.List;
import java.util.UUID;

@Data
@Accessors(chain = true)
public class TwinGroup {
    private Object key;
    private List<TwinEntity> twins;
    private List<TwinGroup> groups;
    private int count;
    private TwinGroupKeyKind keyKind;
    private UUID groupFieldId;
    private UUID linkId;
    private boolean dataListOption;
}
