package org.twins.core.domain.datalist;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class DataListUpdate extends DataListSave {
    private UUID id;
    private UUID defaultOptionId;
}
