package org.twins.core.dto.rest.twinclass;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "TagSearchRqV1")
public class TagSearchRqDTOv1 {

    @Schema(description = "id list")
    public Set<UUID> idList;

    @Schema(description = "id exclude list")
    public Set<UUID> idExcludeList;

    @Schema(description = "option like list")
    public Set<String> optionLikeList;

    @Schema(description = "option not like list")
    public Set<String> optionNotLikeList;

    @Schema(description = "option i18n like list")
    public Set<String> optionI18nLikeList;

    @Schema(description = "option i18n not like list")
    public Set<String> optionI18nNotLikeList;

    @JsonIgnore
    public UUID twinClassId;
}
