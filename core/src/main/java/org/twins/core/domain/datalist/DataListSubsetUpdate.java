package org.twins.core.domain.datalist;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class DataListSubsetUpdate extends DataListSubsetSave {
    private UUID id;
}
