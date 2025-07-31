package org.twins.core.domain.twinoperation;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.twin.TwinClassUpdateLine;

import java.util.List;
import java.util.UUID;

@Data
@EqualsAndHashCode
@Accessors(chain = true)
public class TwinUpdateClass {

    private UUID twinId;
    private UUID twinClassId;
    public List<TwinClassUpdateLine> behavior;


}
