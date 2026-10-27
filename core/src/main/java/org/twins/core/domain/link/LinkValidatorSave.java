package org.twins.core.domain.link;

import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dao.link.LinkValidatorEntity;

@Data
@Accessors(chain = true)
public class LinkValidatorSave {
    public LinkValidatorEntity linkValidator;
}
