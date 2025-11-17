package org.twins.core.domain.datalist;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.enums.datalist.DataListStatus;

import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class DataListOptionUpdate extends DataListOptionSave {
    private UUID id;
    private DataListStatus status;
}
