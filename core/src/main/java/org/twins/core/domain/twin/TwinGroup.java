package org.twins.core.domain.twin;

import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.enums.twin.TwinGroupValueType;

import java.util.List;

@Data
@Accessors(chain = true)
public class TwinGroup {
    private Object key;
    private List<TwinEntity> twins;
    private List<TwinGroup> groups;
    private int count;
    private TwinGroupKeyKind keyKind;
    private TwinGroupValueType valueType;
    private boolean dataListOption;
}
