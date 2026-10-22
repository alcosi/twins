package org.twins.core.domain.link;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class LinkValidatorUpdate extends LinkValidatorSave {
    private UUID id;
}
