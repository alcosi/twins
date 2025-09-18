package org.twins.core.domain.datalist;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.domain.enum_.datalist.Status;

import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class DataListOptionUpdate extends DataListOptionSave {
    private UUID id;
    private Status status;
}
