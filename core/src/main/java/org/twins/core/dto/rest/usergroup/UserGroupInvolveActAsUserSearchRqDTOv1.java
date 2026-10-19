package org.twins.core.dto.rest.usergroup;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "UserGroupInvolveActAsUserSearchRqV1")
public class UserGroupInvolveActAsUserSearchRqDTOv1 extends Request {
    @Schema(description = "search params")
    public UserGroupInvolveActAsUserSearchDTOv1 search;
}
