package org.twins.core.dto.rest.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

import java.util.List;
import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name =  "DomainBusinessAccountSearchRqV1")
public class DomainBusinessAccountSearchRqDTOv1 extends Request {

    @Schema(description = "business account id list")
    public List<UUID> businessAccountIdList;

    @Schema(description = "business account id exclude list")
    public List<UUID> businessAccountIdExcludeList;

    @Schema(description = "business account name like keyword list(AND)")
    public List<String> businessAccountNameLikeList;

    @Schema(description = "business account name exclude like keyword list(OR)")
    public List<String> businessAccountNameNotLikeList;

    @Schema(description = "business account permission schema id list")
    public List<UUID> permissionSchemaIdList;

    @Schema(description = "business account permission schema id exclude list")
    public List<UUID> permissionSchemaIdExcludeList;

    @Schema(description = "business account twinflow schema id list")
    public List<UUID> twinflowSchemaIdList;

    @Schema(description = "business account twinflows chema id exclude list")
    public List<UUID> twinflowSchemaIdExcludeList;

    @Schema(description = "business account twin class schema id list")
    public List<UUID> twinClassSchemaIdList;

    @Schema(description = "business account twin class schema id exclude list")
    public List<UUID> twinClassSchemaIdExcludeList;

}
