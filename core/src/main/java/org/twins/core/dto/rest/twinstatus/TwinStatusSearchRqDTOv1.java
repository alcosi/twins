package org.twins.core.dto.rest.twinstatus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "TwinStatusSearchRqV1")
public class TwinStatusSearchRqDTOv1 extends Request {
    @Schema(description = "id list")
    public Set<UUID> idList;
    @Schema(description = "id exclude list")
    public Set<UUID> idExcludeList;
    @Schema(description = "twin class id list")
    public Set<UUID> twinClassIdList;
    @Schema(description = "twin class id exclude list")
    public Set<UUID> twinClassIdExcludeList;
    @Schema(description = "key like list")
    public Set<String> keyLikeList;
    @Schema(description = "key not like list")
    public Set<String> keyNotLikeList;
    @Schema(description = "name i18n like list")
    public Set<String> nameI18nLikeList;
    @Schema(description = "name i18n not like list")
    public Set<String> nameI18nNotLikeList;
    @Schema(description = "description i18n like list")
    public Set<String> descriptionI18nLikeList;
    @Schema(description = "description i18n not like list")
    public Set<String> descriptionI18nNotLikeList;
}
